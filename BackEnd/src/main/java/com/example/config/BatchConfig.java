package com.example.config;



import com.example.listener.JobCompletionListener;
import com.example.model.RecurringPayments;
import com.example.repository.RecurringPaymentsRepository;
import com.example.repository.TransactionsRepository;
import com.example.services.UserService;
import com.example.step.Reader;
import com.example.step.Process;
import com.example.step.Writer;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration
public class BatchConfig {
    @Autowired
    RecurringPaymentsRepository recurringPaymentsRepository;
    @Autowired
    UserService userService;
    @Autowired
    TransactionsRepository transactionRepository;
    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job processJob() {
        return jobBuilderFactory.get("processJob")
                .incrementer(new RunIdIncrementer()).listener(listener())
                .flow(orderStep1()).end().build();
    }

    @Bean
    public Step orderStep1() {
        return stepBuilderFactory.get("orderStep1").<RecurringPayments, RecurringPayments> chunk(1)
                .reader(new Reader(recurringPaymentsRepository))
                .processor(new Process(userService,recurringPaymentsRepository,transactionRepository))
                .writer(new Writer(recurringPaymentsRepository)).build();
    }

    @Bean
    public JobExecutionListener listener() {
        return new JobCompletionListener();
    }

}
