package com.example.repository;



import com.example.model.RecurringPayments;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecurringPaymentsRepository extends JpaRepository<RecurringPayments, Integer> {
}
