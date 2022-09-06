package com.javastart.notification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javastart.notification.config.RabbitMQConfig;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class TransferMessageHandler {
    private final JavaMailSender javaMailSender;

    @Autowired
    public TransferMessageHandler(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_TRANSFER)
    public void receive(Message message) throws JsonProcessingException {
        System.out.println(message);
        byte[] body = message.getBody();
        String jsonBody = new String(body);
        ObjectMapper objectMapper = new ObjectMapper();
        TransferResponseDTO transferResponseDTO = objectMapper.readValue(jsonBody, TransferResponseDTO.class);
        System.out.println(transferResponseDTO);

        SimpleMailMessage mailMessageForSender = new SimpleMailMessage();
//        mailMessageForSender.setTo(transferResponseDTO.getSenderEmail());      Need to activate for real work
        mailMessageForSender.setTo("bataykinjava@gmail.com");  //                Need to delete for real work
        mailMessageForSender.setFrom("bataykinjava@yandex.ru");
        mailMessageForSender.setSubject("Transfer to " + transferResponseDTO.getReceiverEmail());
        mailMessageForSender.setText("Transfer of " + transferResponseDTO.getAmount() + " to " + transferResponseDTO.getReceiverEmail() + " is done");
        javaMailSender.send(mailMessageForSender);

        SimpleMailMessage mailMessageForReceiver = new SimpleMailMessage();
//        mailMessageForReceiver.setTo(transferResponseDTO.getReceiverEmail());      Need to activate for real work
        mailMessageForReceiver.setTo("bataykinjava@gmail.com");  //                  Need to delete for real work
        mailMessageForReceiver.setFrom("bataykinjava@yandex.ru");
        mailMessageForReceiver.setSubject("Transfer from " + transferResponseDTO.getSenderEmail());
        mailMessageForReceiver.setText("Transfer of " + transferResponseDTO.getAmount() + " from " + transferResponseDTO.getSenderEmail() + " is done");
        javaMailSender.send(mailMessageForReceiver);

    }
}
