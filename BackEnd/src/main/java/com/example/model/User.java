package com.example.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.sun.istack.internal.NotNull;
import lombok.Data;
import org.hibernate.type.descriptor.sql.VarcharTypeDescriptor;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import javax.validation.constraints.Email;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(nullable = false, length = 50)
    private String name;
    @Column(nullable = false, unique = true, length = 32)
    @Email
    private String email;
    @Column(nullable = false)
    private String password;
    private int balance = 0;
    private boolean isActive = false;
    private String roles = "ROLE_USER";
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user", fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Transactions> transactionsList = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user", fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<RecurringPayments> recurringPayments = new ArrayList<>();

    public void addTransaction(Transactions transaction) {
        this.transactionsList.add(transaction);
    }

    public void addRecurring(RecurringPayments recurringPayment) {
        this.recurringPayments.add(recurringPayment);
    }

    public User(int id, String name, String email, String password, int balance, boolean isActive, String roles) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.balance = balance;
        this.isActive = isActive;
        this.roles = roles;
    }

    public User(){

    }

    //Jpa Transactions
    //Application Constants
}