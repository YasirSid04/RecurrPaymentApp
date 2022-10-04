package com.example.step;

import com.example.model.RecurringPayments;
import com.example.model.Transactions;
import com.example.model.User;
import com.example.repository.RecurringPaymentsRepository;
import com.example.repository.TransactionsRepository;
import com.example.services.UserService;
import lombok.NoArgsConstructor;
import org.springframework.batch.item.ItemProcessor;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
@NoArgsConstructor
public class Process implements ItemProcessor<RecurringPayments,RecurringPayments> {

    private UserService userService;
    private RecurringPaymentsRepository recurringPaymentsRepository;
    private TransactionsRepository transactionRepository;
    public Process(UserService userService,RecurringPaymentsRepository recurringPaymentsRepository,TransactionsRepository transactionRepository){
        this.userService=userService;
        this.recurringPaymentsRepository=recurringPaymentsRepository;
        this.transactionRepository=transactionRepository;
    }
    @Override
    public RecurringPayments process(RecurringPayments recurringPayments) throws Exception {
        int userId=recurringPayments.getUser().getId();
        User user = userService.getUser(userId);
//        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
//        //Parsing the given String to Date object
//        Date date = formatter.parse(recurringPayments.getStartDate());
        while((new Date()).after(recurringPayments.getStartDate())
                && recurringPayments.isActive()
                && user.getBalance() >= recurringPayments.getAmount()
        ) {
            Transactions transaction = new Transactions(
                    "DB", recurringPayments.getDescription(),
                    recurringPayments.getAmount(), user.getBalance(), user.getBalance() - recurringPayments.getAmount(), user);
            transactionRepository.save(transaction);
            user.setBalance(user.getBalance() - recurringPayments.getAmount());
            userService.updateUser(user);
            recurringPayments.setNoOfTimes(recurringPayments.getNoOfTimes() - 1);
            Date date = recurringPayments.getStartDate();
            Calendar cal = Calendar.getInstance();
            cal.setTime(date); // don't forget this if date is arbitrary e.g. 01-01-2014
            int month = cal.get(Calendar.MONTH);
            int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
            int year = cal.get(Calendar.YEAR);
            if (month < 12)
                month++;
            else if (month == 12) {
                month = 1;
                year++;
            }
            cal.set(Calendar.MONTH, month);
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            date = cal.getTime();
            recurringPayments.setStartDate(date);
            if (recurringPayments.getNoOfTimes() == 0)
                recurringPayments.setActive(false);
        }
        return recurringPayments;
    }
}