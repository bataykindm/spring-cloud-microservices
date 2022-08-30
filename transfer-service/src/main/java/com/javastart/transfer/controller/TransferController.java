package com.javastart.transfer.controller;

import com.javastart.transfer.controller.dto.TransferRequestDTO;
import com.javastart.transfer.controller.dto.TransferResponseDTO;
import com.javastart.transfer.service.TransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TransferController {

    private final TransferService transferService;

    @Autowired
    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping("/")
    public TransferResponseDTO transfer(@RequestBody TransferRequestDTO transferRequestDTO){
        return transferService.transfer(transferRequestDTO.getSenderAccountId(), transferRequestDTO.getSenderBillId(),
                transferRequestDTO.getReceiverAccountId(), transferRequestDTO.getReceiverBillId(), transferRequestDTO.getAmount());
    }
}
