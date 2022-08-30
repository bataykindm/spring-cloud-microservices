package com.javastart.transfer.repository;

import com.javastart.transfer.entity.Transfer;
import org.springframework.data.repository.CrudRepository;

public interface TransferRepository extends CrudRepository<Transfer, Long> {
}
