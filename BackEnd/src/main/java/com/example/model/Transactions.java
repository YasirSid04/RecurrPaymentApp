package com.example.model;


import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import org.hibernate.annotations.Fetch;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class Transactions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int transactionId;
    @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JoinColumn(name="user_id")
    @JsonBackReference
    private User user;
    private String type;
    private Date transactionDate=new Date();
    private String description;
    private int amount;
    private int openingBal;
    private int closingBal;
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "recc_id")
    @JsonBackReference
    private RecurringPayments recc;

    public User getUser(){
        return user;
    }

    public RecurringPayments getRecc(){
        return recc;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getOpeningBal() {
        return openingBal;
    }

    public void setOpeningBal(int openingBal) {
        this.openingBal = openingBal;
    }

    public int getClosingBal() {
        return closingBal;
    }

    public void setClosingBal(int closingBal) {
        this.closingBal = closingBal;
    }

    public void setRecc(RecurringPayments recc) {
        this.recc = recc;
    }

    public Transactions(int transactionId, User user, String type, Date transactionDate, String description, int amount, int openingBal, int closingBal, RecurringPayments recc) {
        this.transactionId = transactionId;
        this.user = user;
        this.type = type;
        this.transactionDate = transactionDate;
        this.description = description;
        this.amount = amount;
        this.openingBal = openingBal;
        this.closingBal = closingBal;
        this.recc = recc;
    }

    public Transactions(){

    }

    public Transactions(User user, String type, String description, int amount, int openingBal, int closingBal, RecurringPayments recc){
        this.user = user;
        this.type = type;
        this.transactionDate = new Date();
        this.description = description;
        this.amount = amount;
        this.openingBal = openingBal;
        this.closingBal = closingBal;
        this.recc = recc;
    }

    public Transactions(String type,
                        String description,
                        int amount,
                        int openingBal,
                        int closingBal,
                        User user) {
        this.type = type;
        this.description = description;
        this.amount = amount;
        this.openingBal = openingBal;
        this.closingBal = closingBal;
        this.user = user;
    }
}