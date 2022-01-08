package com.ventulus95.springbatch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@RestController
public class TestController {

    private final JobLauncher jobLauncher;
    private final JobExplorer jobExplorer;
    private final Job movieScrappingJob;
    private final Job TEST_JOB;

    @GetMapping("/batch")
    public ResponseEntity<String> batch() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException, NoSuchJobException {
        Set<JobExecution> set = jobExplorer.findRunningJobExecutions("movieScrappingJob"); // 잡이 실행했는지를 판단.
        if (set.size()>0) {
            log.warn(">>>> Batch is running~~ wait a minutes !!! ");
            return ResponseEntity.badRequest().body("배치잡 실행중");
        }
        log.info(">>>> Batch FOUND ");
        JobParameters parameters = new JobParametersBuilder().addString("JobDate", LocalDateTime.now().toString()).toJobParameters(); //파라미터 생성후 => 파라미터를 거는게 더 안전하다. 파라미터가 키값이 되서 작동하기때문
//        JobParameters parameters = new JobParametersBuilder().toJobParameters();
        jobLauncher.run(movieScrappingJob, parameters); // 잡런처를 통해서 특정 잡 실행.
        return ResponseEntity.ok("배치가 작동합니다. 실행중...");
    }

    @GetMapping("/batch1")
    public ResponseEntity<String> batch1() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException, NoSuchJobException {
        Set<JobExecution> set = jobExplorer.findRunningJobExecutions("TEST_JOB"); // 잡이 실행했는지를 판단.
        if (set.size()>0) {
            log.warn(">>>> Batch is running~~ wait a minutes !!! ");
            return ResponseEntity.badRequest().body("배치잡 실행중");
        }
        log.info(">>>> Batch FOUND ");
        JobParameters parameters = new JobParametersBuilder().addString("JobDate", LocalDateTime.now().toString()).toJobParameters(); //파라미터 생성후 => 파라미터를 거는게 더 안전하다. 파라미터가 키값이 되서 작동하기때문
        //        JobParameters parameters = new JobParametersBuilder().toJobParameters();
        jobLauncher.run(TEST_JOB, parameters); // 잡런처를 통해서 특정 잡 실행.
        return ResponseEntity.ok("배치가 작동합니다. 실행중...");
    }
}
