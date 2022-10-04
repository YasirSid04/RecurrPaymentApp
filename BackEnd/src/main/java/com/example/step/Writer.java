package com.example.step;

import com.example.model.RecurringPayments;
import com.example.repository.RecurringPaymentsRepository;
import lombok.NoArgsConstructor;
import org.springframework.batch.item.ItemWriter;

import java.util.List;
@NoArgsConstructor
public class Writer implements ItemWriter<RecurringPayments> {
    RecurringPaymentsRepository recurringPaymentsRepository;

    public Writer(RecurringPaymentsRepository recurringPaymentsRepository){
        this.recurringPaymentsRepository=recurringPaymentsRepository;
    }

    @Override
    public void write(List<? extends RecurringPayments> list) throws Exception {
        for(RecurringPayments r:list){
            recurringPaymentsRepository.save(r);
        }
    }
}
