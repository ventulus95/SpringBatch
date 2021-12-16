package com.ventulus95.springbatch.batch;

import com.ventulus95.springbatch.model.MovieCmm;
import com.ventulus95.springbatch.dto.MovieCompany;
import com.ventulus95.springbatch.dto.kobisResponse;
import com.ventulus95.springbatch.scheduler.ScheduledJob;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.web.reactive.function.client.WebClient;

import javax.persistence.EntityManagerFactory;
import java.util.*;

@Configuration
@RequiredArgsConstructor
public class MovieBatchConfig {

    @Value("${kobis.secretKey}")
    private String key;

    private static final Logger logger = LoggerFactory.getLogger(MovieBatchConfig.class);
    private final WebClient client;
    private final EntityManagerFactory managerFactory;
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job movieScrappingJob(){
        return jobBuilderFactory.get("movieScrappingJob")
                .preventRestart()
//                .start(movieScrappingJobStep())
                .start(stepManager())
                .build();
    }

    @Bean
    public PartitionHandler partitionHandler(){ //파티션 도와주는 핸들러
        TaskExecutorPartitionHandler handler = new TaskExecutorPartitionHandler();
        handler.setStep(movieScrappingJobStep()); // 이 스텝을 파티션 시켜버릴 예정
        handler.setGridSize(20);
        handler.setTaskExecutor(executor()); //그리고 그 쓰레드풀만든 executor 만들어서 넣어두기
        return handler;
    }

    @Bean
    public TaskExecutor executor(){ //쓰레드 풀 만들어서 비둥기적으로 병렬 처리해야하므로 여기서 생성
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20);
        executor.setMaxPoolSize(20);
        executor.setThreadNamePrefix("partition-thread-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        return executor;
    }

    @Bean
    @StepScope
    public MoviePartitioner partitioner(){
        return new MoviePartitioner();
    }

    @Bean
    public Step stepManager(){
        return stepBuilderFactory.get("step.manager")
                .partitioner("movieStep", partitioner())
                .step(movieScrappingJobStep())
                .partitionHandler(partitionHandler())
                .build();
    }

    @Bean
    public CronTriggerFactoryBean triggerFactory(){ //스케쥴 받아서, 이 트리거를 작동시킴.
        CronTriggerFactoryBean factoryBean = new CronTriggerFactoryBean();
        factoryBean.setJobDetail(jobDetailSchedule().getObject());
        factoryBean.setCronExpression("0 0/3 * 1/1 * ? * "); //3분마다 작동시킬거임
        return factoryBean;
    }

    @Bean
    public JobDetailFactoryBean jobDetailSchedule(){ //이번 job의 스케쥴 구성.
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(ScheduledJob.class);
        factoryBean.setDurability(true);
        factoryBean.setRequestsRecovery(true);
        Map<String, Object> map = new HashMap<>();
        map.put("job", movieScrappingJob().getName());
        factoryBean.setJobDataAsMap(map);
        return factoryBean;
    }


    @Bean
    public Step movieScrappingJobStep(){ //실제로 Step이 구성되는 부분
        return stepBuilderFactory.get("movieScrapStep")
                .<MovieCompany, MovieCmm>chunk(20)
//                .reader(blockingReader())
                .reader(reader(null, null))
                .processor(processor())
                .writer(writer())
                .build();
    }

    @Bean
    @StepScope
    public ListCustomItemReader reader(
            @Value("#{stepExecutionContext[start]}") Integer start,
            @Value("#{stepExecutionContext[end]}") Integer end
    ){
        ListCustomItemReader itemReader = new ListCustomItemReader(client,10, start, end);
        return itemReader;
    }

    public ItemProcessor<MovieCompany, MovieCmm> processor(){
        return item -> item.toEntity();
    }

    public JpaItemWriter<MovieCmm> writer(){
        JpaItemWriter<MovieCmm> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(managerFactory);
        return writer;
    }

    @Bean
    @StepScope // 동기식 방법 적용시 사용하는 Reader
    public ListItemReader<MovieCompany> blockingReader(){
        int page = 1;
        List<MovieCompany> list = new ArrayList<>();
        for (;page<=700; page++){
            ResponseEntity<kobisResponse> res =client.get().uri("/company/searchCompanyList.json?key="+key+"&itemPerPage=10&curPage="+page)
                    .retrieve().toEntity(kobisResponse.class)
                    .block();
            list.addAll(res.getBody().companyListResult.getCompanyList());
        }
        return new ListItemReader<>(list);
    }



}
