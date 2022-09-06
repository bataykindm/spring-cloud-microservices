package com.javastart.payment.service;

import com.javastart.payment.controller.dto.PaymentResponseDTO;
import com.javastart.payment.entity.Payment;
import com.javastart.payment.exception.AmountNotEnoughException;
import com.javastart.payment.exception.PaymentServiceException;
import com.javastart.payment.repository.PaymentRepository;
import com.javastart.payment.rest.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Service
public class PaymentService {

    private static final String TOPIC_EXCHANGE_PAYMENT = "js.payment.notify.exchange";
    private static final String ROUTING_KEY_PAYMENT = "js.key.payment";

    private final PaymentRepository paymentRepository;
    private final AccountServiceClient accountServiceClient;
    private final BillServiceClient billServiceClient;
    private final RabbitTemplate rabbitTemplate;


    @Autowired
    public PaymentService(PaymentRepository paymentRepository, AccountServiceClient accountServiceClient, BillServiceClient billServiceClient, RabbitTemplate rabbitTemplate) {
        this.paymentRepository = paymentRepository;
        this.accountServiceClient = accountServiceClient;
        this.billServiceClient = billServiceClient;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional
    public PaymentResponseDTO pay(Long accountId, Long billId, BigDecimal amount){
        if (accountId == null && billId == null){
            throw new PaymentServiceException("Id of account and bill are null");
        }
        if (billId != null){
            BillResponseDTO billResponseDTO = billServiceClient.getBillById(billId);
            BillRequestDTO billRequestDTO = createBillRequest(amount, billResponseDTO);
            billServiceClient.updateBill(billId, billRequestDTO);

            AccountResponseDTO accountResponseDTO = accountServiceClient.getAccountById(billResponseDTO.getAccountId());
            paymentRepository.save(new Payment(amount, billId, OffsetDateTime.now(), accountResponseDTO.getEmail()));

            return createResponse(amount, accountResponseDTO);
        }

        BillResponseDTO defaultBill = getDefaultBill(accountId);
        BillRequestDTO billRequestDTO = createBillRequest(amount, defaultBill);
        billServiceClient.updateBill(defaultBill.getBillId(), billRequestDTO);
        AccountResponseDTO account = accountServiceClient.getAccountById(accountId);
        paymentRepository.save(new Payment(amount, defaultBill.getBillId(), OffsetDateTime.now(), account.getEmail()));

        return createResponse(amount, account);
    }

    private PaymentResponseDTO createResponse(BigDecimal amount, AccountResponseDTO accountResponseDTO) {
        PaymentResponseDTO paymentResponseDTO = new PaymentResponseDTO(amount, accountResponseDTO.getEmail());

//        ObjectMapper objectMapper = new ObjectMapper();
//        try {
//            rabbitTemplate.convertAndSend(TOPIC_EXCHANGE_DEPOSIT, ROUTING_KEY_DEPOSIT,
//                    objectMapper.writeValueAsString(depositResponseDTO));
//        } catch (JsonProcessingException e) {
////            e.printStackTrace();
////            throw new DepositServiceException("Can't send message to RabbitMQ");
//        }
        return paymentResponseDTO;
    }

    private BillRequestDTO createBillRequest(BigDecimal amount, BillResponseDTO billResponseDTO) {
        if (billResponseDTO.getAmount().compareTo(amount) < 0){
            throw new AmountNotEnoughException("Amount of bill with id: " + billResponseDTO.getBillId() + " is not enough for payment");
        }
        BillRequestDTO billRequestDTO = new BillRequestDTO();
        billRequestDTO.setAccountId(billResponseDTO.getAccountId());
        billRequestDTO.setCreationDate(billResponseDTO.getCreationDate());
        billRequestDTO.setIsDefault(billResponseDTO.getIsDefault());
        billRequestDTO.setOverdraftEnabled(billResponseDTO.getOverdraftEnabled());
        billRequestDTO.setAmount(billResponseDTO.getAmount().subtract(amount));
        return billRequestDTO;
    }

    private BillResponseDTO getDefaultBill(Long accountId){
        return billServiceClient.getBillsByAccountId(accountId).stream()
                .filter(BillResponseDTO::getIsDefault).findAny()
                .orElseThrow(()->new PaymentServiceException("Unable find default bill or account with id: " + accountId));
    }
}
