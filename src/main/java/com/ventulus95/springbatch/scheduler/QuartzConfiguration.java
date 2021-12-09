package com.ventulus95.springbatch.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.slf4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.support.CronTrigger;

import java.util.TimeZone;

@Slf4j
@Configuration
public class QuartzConfiguration {

    @Bean
    public JobDetail quartzJobDetail() {
        log.info("quartzJobDetail START");
        return JobBuilder.newJob(ScheduledJob.class)
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger jobTrigger() {
        log.info("jobTrigger START");
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule("0 0/10 * 1/1 * ? * ")
                .inTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

        return TriggerBuilder.newTrigger()
                .forJob(quartzJobDetail())
                .withSchedule(scheduleBuilder)
                .build();
    }
}
