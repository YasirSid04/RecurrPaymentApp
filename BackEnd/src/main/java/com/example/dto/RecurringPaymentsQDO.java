package com.example.dto;


import lombok.Data;

import java.util.Date;

@Data
public class RecurringPaymentsQDO {
    private int reccId;
    private String description;
    private int amount;
    private Date startDate;
    private int noOfTimes;
}
