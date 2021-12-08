package com.ventulus95.springbatch.batch;

import com.ventulus95.springbatch.MovieCmm;
import com.ventulus95.springbatch.MovieCmmRepository;
import com.ventulus95.springbatch.dto.MovieCompany;
import com.ventulus95.springbatch.dto.kobisResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class MovieConfig {

    private static final Logger logger = LoggerFactory.getLogger(MovieConfig.class);
    private final MovieCmmRepository movieCmmRepository;
    private final WebClient client;
    private final EntityManagerFactory managerFactory;

    @Bean
    public Job movieScrappingJob(JobBuilderFactory jobBuilderFactory, Step movieScrappingStep){
        return jobBuilderFactory.get("movieScrappingJob")
                .preventRestart()
                .start(movieScrappingStep)
                .build();
    }

    @Bean
    public Step movieScrappingJobStep(StepBuilderFactory stepBuilderFactory){
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
