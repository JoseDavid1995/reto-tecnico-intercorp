package com.example.demo.config;

import com.example.demo.entity.RegistroEntity;
import com.example.demo.repository.RegistroRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchConfig {

    private final RegistroRepository registroRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public BatchConfig(RegistroRepository registroRepository, KafkaTemplate<String, String> kafkaTemplate) {
        this.registroRepository = registroRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Bean
    public FlatFileItemReader<String> reader() {
        return new FlatFileItemReaderBuilder<String>()
                .name("csvItemReader")
                .resource(new ClassPathResource("data/entrada.csv"))
                .linesToSkip(1) 
                .lineMapper((line, lineNumber) -> line)
                .build();
    }

    @Bean
    public ItemProcessor<String, RegistroEntity> processor() {
        return item -> {
            RegistroEntity entidad = new RegistroEntity();
            entidad.setCodigoUnico(item);
            entidad.setEstado("PENDING");
            entidad.setDetalle("Procesado por Batch");
            return entidad;
        };
    }

    @Bean
    public ItemWriter<RegistroEntity> writer() {
        return items -> {
            for (RegistroEntity entidad : items) {
                RegistroEntity saved = registroRepository.save(entidad);
                kafkaTemplate.send("registros-topic", saved.getId().toString(), saved.getCodigoUnico());
            }
        };
    }

    @Bean
    public Step step1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("step1", jobRepository)
                .<String, RegistroEntity>chunk(10, transactionManager)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build();
    }

    @Bean
    public Job importarRegistrosJob(JobRepository jobRepository, Step step1) {
        return new JobBuilder("importarRegistrosJob", jobRepository)
                .start(step1)
                .build();
    }
}