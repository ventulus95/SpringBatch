package com.ventulus95.springbatch.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.slf4j.Logger;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.support.CronTrigger;

import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

@Slf4j
@Configuration
public class QuartzConfiguration {

//    @Bean
//    public JobDetail quartzJobDetail() {
//        log.info("Quartz Job Detail START");
//        return JobBuilder.newJob(ScheduledJob.class)
//                .storeDurably()
//                .build();
//    }
//
//    @Bean
//    public Trigger jobTrigger() {
//        log.info("job Trigger START");
//        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule("0 0/10 * 1/1 * ? * ")
//                .inTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

//        return TriggerBuilder.newTrigger()
//                .forJob(quartzJobDetail())
//                .withSchedule(scheduleBuilder)
//                .build();
//    }



    @Bean
    public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor(JobRegistry jobRegistry) {
        JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor = new JobRegistryBeanPostProcessor();
        jobRegistryBeanPostProcessor.setJobRegistry(jobRegistry);
        return jobRegistryBeanPostProcessor;
    }



}
