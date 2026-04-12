package com.neelesh.paymentgateway.repository;

import com.neelesh.paymentgateway.model.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IdempotencyRepository extends JpaRepository<IdempotencyKey, String> {

}
