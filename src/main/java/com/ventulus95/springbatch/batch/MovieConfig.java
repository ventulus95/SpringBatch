package com.ventulus95.springbatch.batch;

import com.ventulus95.springbatch.MovieCmm;
import com.ventulus95.springbatch.dto.MovieCompany;
import com.ventulus95.springbatch.dto.kobisResponse;
import com.ventulus95.springbatch.scheduler.ScheduledJob;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;
import javax.persistence.EntityManagerFactory;
import java.util.*;

@Configuration
@RequiredArgsConstructor
public class MovieConfig  {

    private static final Logger logger = LoggerFactory.getLogger(MovieConfig.class);
    private final WebClient client;
    private final EntityManagerFactory managerFactory;
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public TaskExecutor taskExecutor(){
        return new SimpleAsyncTaskExecutor("spring_batch");
    }

    @Bean
    public Job movieScrappingJob(){
        return jobBuilderFactory.get("movieScrappingJob")
                .preventRestart()
//                .start(movieScrappingJobStep())
                .start(stepManager())
                .build();
    }

    @Bean
    public CronTriggerFactoryBean triggerFactory(){
        logger.info("트리거 팩토리 빈");
        CronTriggerFactoryBean factoryBean = new CronTriggerFactoryBean();
        factoryBean.setJobDetail(jobDetailSchedule().getObject());
        factoryBean.setCronExpression("0 0/55 * 1/1 * ? * ");
        return factoryBean;
    }

    @Bean
    public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor(JobRegistry jobRegistry) {
        JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor = new JobRegistryBeanPostProcessor();
        jobRegistryBeanPostProcessor.setJobRegistry(jobRegistry);
        return jobRegistryBeanPostProcessor;
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
    public Step stepManager(){
        return stepBuilderFactory.get("step.manager")
                .partitioner("movieStep", partitioner())
                .step(movieScrappingJobStep())
                .partitionHandler(partitionHandler())
//                .taskExecutor(taskExecutor())
//                .gridSize(20)
                .build();
    }

    @Bean
    @StepScope
    public MoviePartitioner partitioner(){
        return new MoviePartitioner();
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
        return stepBuilderFactory.get("movieScrapStep")
                .<MovieCompany, MovieCmm>chunk(20)
//                .reader(movieScrapper())
                .reader(movieScrapper1(null, null))
                .processor(processor())
                .writer(insert())
                .build();
    }

    @Bean
    @StepScope
    public ListItemReader<MovieCompany> movieScrapper(){
        logger.info("$$$$$$$$$$$ 배치 읽어오기 시작");
        WebClient client = WebClient
                .builder()
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create().secure(t->{
                            try {
                                t.sslContext(SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build());
                            } catch (SSLException e) {
                                e.printStackTrace();
                            }
                        })
                ))
                .baseUrl("https://www.kobis.or.kr/kobisopenapi/webservice/rest")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        int page = 1;
        List<MovieCompany> list = new ArrayList<>();
        for (;page<=700; page++){
            ResponseEntity<kobisResponse> res =client.get().uri("/company/searchCompanyList.json?key=f5eef3421c602c6cb7ea224104795888&itemPerPage=10&curPage="+page)
                    .retrieve().toEntity(kobisResponse.class)
                    .block();
            list.addAll(res.getBody().companyListResult.getCompanyList());
        }
        return new ListItemReader<>(list);
    }

    @Bean
    @StepScope
    public ListCustomItemReader movieScrapper1(
            @Value("#{stepExecutionContext[start]}") Integer start,
            @Value("#{stepExecutionContext[end]}") Integer end
    ){
        logger.info("$$$$$$$$$$$ 배치 읽어오기 시작 start: [{}] end: [{}]", start, end);
//        int page = start;
//        List<MovieCompany> list = new LinkedList<>();
//        while(page<=end){
//
//            page++;
//        }
        ListCustomItemReader itemReader = new ListCustomItemReader(client,10, start, end);
//        SynchronizedItemStreamReader<MovieCompany> synchronizedItemStreamReader = new SynchronizedItemStreamReader<>();
//        synchronizedItemStreamReader.setDelegate(itemReader);
        return itemReader;
    }

    public ItemProcessor<MovieCompany, MovieCmm> processor(){
        return item -> {
//            logger.info("~~~~~~~~~~~~~~~ 배치 프로세스 진행중!!!");
            return item.toEntity();
        };
    }

    public JpaItemWriter<MovieCmm> insert(){
        JpaItemWriter<MovieCmm> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(managerFactory);
        return writer;
    }

    @Bean
    @StepScope
    public JpaItemWriter<MovieCmm> insert1(
            @Value("#{stepExecutionContext[start]}") Integer start,
            @Value("#{stepExecutionContext[end]}") Integer end
    ){
        JpaItemWriter<MovieCmm> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(managerFactory);
        return writer;
    }
}
