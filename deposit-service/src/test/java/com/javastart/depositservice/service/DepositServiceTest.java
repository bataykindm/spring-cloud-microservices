package com.javastart.depositservice.service;

import com.javastart.deposit.controller.dto.DepositResponseDTO;
import com.javastart.deposit.exception.DepositServiceException;
import com.javastart.deposit.repositiry.DepositRepository;
import com.javastart.deposit.rest.AccountResponseDTO;
import com.javastart.deposit.rest.AccountServiceClient;
import com.javastart.deposit.rest.BillResponseDTO;
import com.javastart.deposit.rest.BillServiceClient;
import com.javastart.deposit.service.DepositService;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;

@RunWith(MockitoJUnitRunner.class)
public class DepositServiceTest {

    @Mock
    private DepositRepository depositRepository;

    @Mock
    private AccountServiceClient accountServiceClient;

    @Mock
    private BillServiceClient billServiceClient;

    @InjectMocks
    private DepositService depositService;

    @Test
    public void depositServiceTest_withBillId() {
        Mockito.when(billServiceClient.getBillById(ArgumentMatchers.anyLong())).thenReturn(createBillResponseDTO());
        Mockito.when(accountServiceClient.getAccountById(ArgumentMatchers.anyLong())).thenReturn(createAccountResponseDTO());
        DepositResponseDTO depositResponseDTO = depositService.deposit(null, 1L, BigDecimal.valueOf(1000));
        Assertions.assertEquals(depositResponseDTO.getEmail(), "Test@junit.com");
    }

    @Test(expected = DepositServiceException.class)
    public void depositServiceTest_exception(){
        depositService.deposit(null,null, BigDecimal.valueOf(1000));
    }

    private AccountResponseDTO createAccountResponseDTO() {
        AccountResponseDTO accountResponseDTO = new AccountResponseDTO();
        accountResponseDTO.setAccountId(1L);
        accountResponseDTO.setBills(Arrays.asList(1L, 2L, 3L));
        accountResponseDTO.setCreationDate(OffsetDateTime.now());
        accountResponseDTO.setEmail("Test@junit.com");
        accountResponseDTO.setName("Tester");
        accountResponseDTO.setPhone("88888");
        return accountResponseDTO;
    }

    private BillResponseDTO createBillResponseDTO() {
        BillResponseDTO billResponseDTO = new BillResponseDTO();
        billResponseDTO.setAccountId(1L);
        billResponseDTO.setAmount(BigDecimal.valueOf(1000));
        billResponseDTO.setBillId(1L);
        billResponseDTO.setCreationDate(OffsetDateTime.now());
        billResponseDTO.setIsDefault(true);
        billResponseDTO.setOverdraftEnabled(true);
        return billResponseDTO;
    }
}
