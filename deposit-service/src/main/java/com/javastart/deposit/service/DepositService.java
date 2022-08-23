package com.javastart.deposit.service;

import com.javastart.deposit.controller.dto.DepositResponseDTO;
import com.javastart.deposit.entity.Deposit;
import com.javastart.deposit.exception.DepositServiceException;
import com.javastart.deposit.repositiry.DepositRepository;
import com.javastart.deposit.rest.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Service
public class DepositService {

    private static final String TOPIC_EXCHANGE_DEPOSIT = "js.deposit.notify.exchange";
    private static final String ROUTING_KEY_DEPOSIT = "js.key.deposit";

    private final DepositRepository depositRepository;
    private final AccountServiceClient accountServiceClient;
    private final BillServiceClient billServiceClient;
    private final RabbitTemplate rabbitTemplate;


    @Autowired
    public DepositService(DepositRepository depositRepository, AccountServiceClient accountServiceClient,
                          BillServiceClient billServiceClient, RabbitTemplate rabbitTemplate) {
        this.depositRepository = depositRepository;
        this.accountServiceClient = accountServiceClient;
        this.billServiceClient = billServiceClient;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional
    public DepositResponseDTO deposit(Long accountId, Long billId, BigDecimal amount){
        if (accountId == null && billId == null){
            throw new DepositServiceException("Id of account and bill are null");
        }
        if (billId != null){
            BillResponseDTO billResponseDTO = billServiceClient.getBillById(billId);
            BillRequestDTO billRequestDTO = createBillRequest(amount, billResponseDTO);
            billServiceClient.updateBill(billId, billRequestDTO);

            AccountResponseDTO accountResponseDTO = accountServiceClient.getAccountById(billResponseDTO.getAccountId());
            depositRepository.save(new Deposit(amount, billId, OffsetDateTime.now(), accountResponseDTO.getEmail()));

            return createResponse(amount, accountResponseDTO);
        }

        BillResponseDTO defaultBill = getDefaultBill(accountId);
        BillRequestDTO billRequestDTO = createBillRequest(amount, defaultBill);
        billServiceClient.updateBill(defaultBill.getBillId(), billRequestDTO);
        AccountResponseDTO account = accountServiceClient.getAccountById(accountId);
        depositRepository.save(new Deposit(amount, defaultBill.getBillId(), OffsetDateTime.now(), account.getEmail()));

        return createResponse(amount, account);
    }

    private DepositResponseDTO createResponse(BigDecimal amount, AccountResponseDTO accountResponseDTO) {
        DepositResponseDTO depositResponseDTO = new DepositResponseDTO(amount, accountResponseDTO.getEmail());

//        ObjectMapper objectMapper = new ObjectMapper();
//        try {
//            rabbitTemplate.convertAndSend(TOPIC_EXCHANGE_DEPOSIT, ROUTING_KEY_DEPOSIT,
//                    objectMapper.writeValueAsString(depositResponseDTO));
//        } catch (JsonProcessingException e) {
////            e.printStackTrace();
////            throw new DepositServiceException("Can't send message to RabbitMQ");
//        }
        return depositResponseDTO;
    }

    private BillRequestDTO createBillRequest(BigDecimal amount, BillResponseDTO billResponseDTO) {
        BillRequestDTO billRequestDTO = new BillRequestDTO();
        billRequestDTO.setAccountId(billResponseDTO.getAccountId());
        billRequestDTO.setCreationDate(billResponseDTO.getCreationDate());
        billRequestDTO.setIsDefault(billResponseDTO.getIsDefault());
        billRequestDTO.setOverdraftEnabled(billResponseDTO.getOverdraftEnabled());
        billRequestDTO.setAmount(billResponseDTO.getAmount().add(amount));
        return billRequestDTO;
    }

    private BillResponseDTO getDefaultBill(Long accountId){
        return billServiceClient.getBillsByAccountId(accountId).stream()
                .filter(BillResponseDTO::getIsDefault).findAny()
                .orElseThrow(()->new DepositServiceException("Unable find default bill or account with id: " + accountId));
    }
}
