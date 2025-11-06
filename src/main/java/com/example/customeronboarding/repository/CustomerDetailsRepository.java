package com.example.customeronboarding.repository;

import com.example.customeronboarding.entity.CustomerDetailsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerDetailsRepository extends JpaRepository<CustomerDetailsEntity, String> {
}
