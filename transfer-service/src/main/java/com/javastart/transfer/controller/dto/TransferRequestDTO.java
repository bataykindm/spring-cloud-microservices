package com.javastart.transfer.controller.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TransferRequestDTO {

    private Long senderAccountId;
    private Long senderBillId;
    private Long receiverAccountId;
    private Long receiverBillId;
    private BigDecimal amount;
}
