package com.example.customeronboarding.config;

import com.example.customeronboarding.dto.CustomerCsvDto;
import com.example.customeronboarding.entity.CustomerDetailsEntity;
import com.example.customeronboarding.repository.CustomerDetailsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.batch.core.configuration.annotation.StepScope;

import java.sql.Timestamp;
import java.time.Instant;

@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    private final CustomerDetailsRepository customerDetailsRepository;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    @StepScope
    public FlatFileItemReader<CustomerCsvDto> reader(@Value("#{jobParameters['fileName']}") String fileName) {
        return new FlatFileItemReaderBuilder<CustomerCsvDto>()
                .name("customerItemReader")
                .resource(new FileSystemResource(fileName))
                .delimited()
                .names("custId")
                .fieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
                    setTargetType(CustomerCsvDto.class);
                }})
                .linesToSkip(1) // Skip header
                .build();
    }

    @Bean
    public ItemProcessor<CustomerCsvDto, CustomerDetailsEntity> processor() {
        return item -> {
            CustomerDetailsEntity entity = new CustomerDetailsEntity();
            entity.setCustId(item.getCustId());
            entity.setStatus("ACTIVE");
            entity.setMobNo("7982948797"); // Default mobile number
            entity.setCreatedBy("admin");
            entity.setUpdatedBy("admin");
            entity.setCreatedOn(Timestamp.from(Instant.now()));
            entity.setUpdatedOn(Timestamp.from(Instant.now()));
            return entity;
        };
    }

    @Bean
    public RepositoryItemWriter<CustomerDetailsEntity> writer() {
        return new RepositoryItemWriterBuilder<CustomerDetailsEntity>()
                .repository(customerDetailsRepository)
                .methodName("save")
                .build();
    }

    @Bean
    public Step step1(FlatFileItemReader<CustomerCsvDto> reader) {
        return new StepBuilder("step1", jobRepository)
                .<CustomerCsvDto, CustomerDetailsEntity>chunk(100, transactionManager)
                .reader(reader)
                .processor(processor())
                .writer(writer())
                .build();
    }

    @Bean
    public Job customerOnboardingJob(Step step1) {
        return new JobBuilder("customerOnboardingJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .flow(step1)
                .end()
                .build();
    }
}
