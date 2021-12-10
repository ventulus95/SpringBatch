package com.ventulus95.springbatch.batch;

import com.ventulus95.springbatch.MovieCmm;
import com.ventulus95.springbatch.dto.MovieCompany;
import com.ventulus95.springbatch.dto.kobisResponse;
import com.ventulus95.springbatch.scheduler.ScheduledJob;
import lombok.RequiredArgsConstructor;
import org.quartz.JobDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.web.reactive.function.client.WebClient;

import javax.persistence.EntityManagerFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class MovieConfig  {

    private static final Logger logger = LoggerFactory.getLogger(MovieConfig.class);
    private final WebClient client;
    private final EntityManagerFactory managerFactory;
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;


    @Bean
    public Job movieScrappingJob(){
        return jobBuilderFactory.get("movieScrappingJob")
                .preventRestart()
                .start(movieScrappingJobStep())
                .build();
    }

    @Bean
    public CronTriggerFactoryBean triggerFactory(){
        logger.info("트리거 팩토리 빈");
        CronTriggerFactoryBean factoryBean = new CronTriggerFactoryBean();
        factoryBean.setJobDetail(jobDetailSchedule().getObject());
        factoryBean.setCronExpression("0 0/5 * 1/1 * ? * ");
        return factoryBean;
    }

    @Bean
    public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor(JobRegistry jobRegistry) {
        JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor = new JobRegistryBeanPostProcessor();
        jobRegistryBeanPostProcessor.setJobRegistry(jobRegistry);
        return jobRegistryBeanPostProcessor;
    }

    @Bean
    public JobDetailFactoryBean jobDetailSchedule(){
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
    public Step movieScrappingJobStep(){
        return stepBuilderFactory.get("movieScrapStep").<MovieCompany, MovieCmm>chunk(30)
                .reader(movieScrapper())
                .processor(processor())
                .writer(insert())
                .build();
    }

    @Bean
    @StepScope
    public ListItemReader<MovieCompany> movieScrapper(){
        logger.info("$$$$$$$$$$$ 배치 읽어오기 시작");
        int page = 1;
        List<MovieCompany> list = new ArrayList<>();
        for (;page<4; page++){
            ResponseEntity<kobisResponse> res =client.get().uri("/company/searchCompanyList.json?key=f5eef3421c602c6cb7ea224104795888&itemPerPage=10&curPage="+page)
                    .retrieve().toEntity(kobisResponse.class)
                    .block();
            list.addAll(res.getBody().companyListResult.getCompanyList());
        }
        return new ListItemReader<>(list);
    }

    public ItemProcessor<MovieCompany, MovieCmm> processor(){
        return item -> {
            logger.info("~~~~~~~~~~~~~~~ 배치 프로세스 진행중!!!");
            return item.toEntity();
        };
    }

    public JpaItemWriter<MovieCmm> insert(){
        JpaItemWriter<MovieCmm> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(managerFactory);
        return writer;
    }
}
