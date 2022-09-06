package com.javastart.transfer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javastart.transfer.controller.dto.TransferResponseDTO;
import com.javastart.transfer.entity.Transfer;
import com.javastart.transfer.exception.AmountNotEnoughException;
import com.javastart.transfer.exception.TransferServiceException;
import com.javastart.transfer.repository.TransferRepository;
import com.javastart.transfer.rest.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Service
public class TransferService {

    private static final String TOPIC_EXCHANGE_TRANSFER = "js.transfer.notify.exchange";
    private static final String ROUTING_KEY_TRANSFER = "js.key.transfer";

    private final TransferRepository transferRepository;
    private final AccountServiceClient accountServiceClient;
    private final BillServiceClient billServiceClient;
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public TransferService(TransferRepository transferRepository, AccountServiceClient accountServiceClient, BillServiceClient billServiceClient, RabbitTemplate rabbitTemplate) {
        this.transferRepository = transferRepository;
        this.accountServiceClient = accountServiceClient;
        this.billServiceClient = billServiceClient;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional
    public TransferResponseDTO transfer(Long senderAccountId, Long senderBillId, Long receiverAccountId, Long receiverBillId, BigDecimal amount) {
        if (senderAccountId == null && senderBillId == null) {
            throw new TransferServiceException("Account's and bill's ID of sender are null");
        }
        if (receiverAccountId == null && receiverBillId == null) {
            throw new TransferServiceException("Account's and bill's ID of receiver are null");
        }

        //SENDER
        //create necessary variables for working on sender's bills
        BillResponseDTO senderBillResponseDTO;
        BillRequestDTO senderBillRequestDTO;
        AccountResponseDTO senderAccountResponseDTO;
        //initialization variables
        if (senderBillId != null) {
            senderBillResponseDTO = billServiceClient.getBillById(senderBillId);
            senderBillRequestDTO = createSenderBillRequest(amount, senderBillResponseDTO);
            senderAccountResponseDTO = accountServiceClient.getAccountById(senderBillResponseDTO.getAccountId());
        } else {
            senderBillResponseDTO = getDefaultBill(senderAccountId);
            senderBillRequestDTO = createSenderBillRequest(amount, senderBillResponseDTO);
            senderBillId = senderBillResponseDTO.getBillId();
            senderAccountResponseDTO = accountServiceClient.getAccountById(senderAccountId);
        }

        //RECEIVER
        //create necessary variable for working on receiver's bills
        BillResponseDTO receiverBillResponseDTO;
        BillRequestDTO receiverBillRequestDTO;
        AccountResponseDTO receiverAccountResponseDTO;
        //initialization variables
        if (receiverBillId != null) {
            receiverBillResponseDTO = billServiceClient.getBillById(receiverBillId);
            receiverBillRequestDTO = createReceiverBillRequest(amount, receiverBillResponseDTO);
            receiverAccountResponseDTO = accountServiceClient.getAccountById(receiverBillResponseDTO.getAccountId());
        }else {
            receiverBillResponseDTO = getDefaultBill(receiverAccountId);
            receiverBillRequestDTO = createReceiverBillRequest(amount, receiverBillResponseDTO);
            receiverBillId = receiverBillResponseDTO.getBillId();
            receiverAccountResponseDTO = accountServiceClient.getAccountById(receiverAccountId);
        }

        //UPDATE
        //update sender's bill
        billServiceClient.updateBill(senderBillId, senderBillRequestDTO);
        //update receiver's bill
        billServiceClient.updateBill(receiverBillId, receiverBillRequestDTO);
        //save transfer data
        transferRepository.save(new Transfer(amount, senderBillId, receiverBillId, OffsetDateTime.now(),
                senderAccountResponseDTO.getEmail(), receiverAccountResponseDTO.getEmail()));
        return createResponse(amount, senderAccountResponseDTO, receiverAccountResponseDTO);
    }

    private TransferResponseDTO createResponse(BigDecimal amount, AccountResponseDTO senderAccountResponseDTO, AccountResponseDTO receiverAccountResponseDTO) {
        TransferResponseDTO transferResponseDTO = new TransferResponseDTO(amount, senderAccountResponseDTO.getEmail(), receiverAccountResponseDTO.getEmail());

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            rabbitTemplate.convertAndSend(TOPIC_EXCHANGE_TRANSFER, ROUTING_KEY_TRANSFER,
                    objectMapper.writeValueAsString(transferResponseDTO));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new TransferServiceException("Can't send message to RabbitMQ");
        }
        return transferResponseDTO;
    }

    private BillRequestDTO createSenderBillRequest(BigDecimal amount, BillResponseDTO billResponseDTO) {
        if (billResponseDTO.getAmount().compareTo(amount) < 0) {
            throw new AmountNotEnoughException("Amount of sender's bill with id: " + billResponseDTO.getBillId() + " is not enough for payment");
        }
        BillRequestDTO billRequestDTO = new BillRequestDTO();
        billRequestDTO.setAccountId(billResponseDTO.getAccountId());
        billRequestDTO.setCreationDate(billResponseDTO.getCreationDate());
        billRequestDTO.setIsDefault(billResponseDTO.getIsDefault());
        billRequestDTO.setOverdraftEnabled(billResponseDTO.getOverdraftEnabled());
        billRequestDTO.setAmount(billResponseDTO.getAmount().subtract(amount));
        return billRequestDTO;
    }

    private BillRequestDTO createReceiverBillRequest(BigDecimal amount, BillResponseDTO billResponseDTO) {
        BillRequestDTO billRequestDTO = new BillRequestDTO();
        billRequestDTO.setAccountId(billResponseDTO.getAccountId());
        billRequestDTO.setCreationDate(billResponseDTO.getCreationDate());
        billRequestDTO.setIsDefault(billResponseDTO.getIsDefault());
        billRequestDTO.setOverdraftEnabled(billResponseDTO.getOverdraftEnabled());
        billRequestDTO.setAmount(billResponseDTO.getAmount().add(amount));
        return billRequestDTO;
    }

    private BillResponseDTO getDefaultBill(Long accountId) {
        return billServiceClient.getBillsByAccountId(accountId).stream()
                .filter(BillResponseDTO::getIsDefault).findAny()
                .orElseThrow(() -> new TransferServiceException("Unable find default bill or account with id: " + accountId));
    }
}
