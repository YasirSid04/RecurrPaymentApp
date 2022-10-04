package com.example.step;
import com.example.model.RecurringPayments;
import com.example.repository.RecurringPaymentsRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import java.util.List;


@NoArgsConstructor
@AllArgsConstructor
public class Reader implements ItemReader<RecurringPayments> {
    private RecurringPaymentsRepository recurringPaymentsRepository;
    @Autowired
    private List<RecurringPayments> recurringPaymentsList;
    @Bean
    private List<RecurringPayments> getRecurringPayments(){
        this.recurringPaymentsList = recurringPaymentsRepository.findAll();
        return this.recurringPaymentsList;
    }

    public Reader(RecurringPaymentsRepository recurringPaymentsRepository){
        this.recurringPaymentsRepository=recurringPaymentsRepository;
//        recurringPaymentsList=recurringPaymentsRepository.findAll();
    }
    private int count=0;
    @Override
    public RecurringPayments read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
//        if(count<recurringPaymentsList.size()) {
        if(count<getRecurringPayments().size()) {
            return recurringPaymentsList.get(count++);
        }else{
            count=0;
        }
        return null;
    }
}