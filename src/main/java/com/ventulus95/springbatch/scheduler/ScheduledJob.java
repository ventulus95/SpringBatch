package com.ventulus95.springbatch.scheduler;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.scheduling.quartz.QuartzJobBean;

@Slf4j
@RequiredArgsConstructor
public class ScheduledJob extends QuartzJobBean {

    private final JobLocator jobLocator;
    private final JobLauncher jobLauncher;


    @Override //excuteInternal는 이벤트 발생시마다 한번씩 호출되니까 스케쥴할 무엇인가만 여기에 얹어주면 된다.
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.info("excueteInternal!!! START!!");
        JobDataMap map = context.getMergedJobDataMap();
        JobParameters parameters = null;
        try {
            parameters = new JobParametersBuilder()
                    .addString("InstanceId", context.getScheduler().getSchedulerInstanceId())
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters(); //파라미터를 instanceId, timestamp으로 설정

            log.info("job은 뭐고... :{}, ",(String) map.get("job"));
            jobLauncher.run(jobLocator.getJob((String) map.get("job")), parameters);
        } catch (JobExecutionAlreadyRunningException | JobInstanceAlreadyCompleteException | JobParametersInvalidException| NoSuchJobException| JobRestartException | SchedulerException  e) {
            log.error(e.toString());
            throw new JobExecutionException();
        }
    }
}
