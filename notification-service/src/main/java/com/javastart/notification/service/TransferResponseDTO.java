package com.javastart.notification.service;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TransferResponseDTO {
    private BigDecimal amount;
    private String senderEmail;
    private String receiverEmail;
}
