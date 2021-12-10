package com.ventulus95.springbatch;

import com.ventulus95.springbatch.dto.kobisResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Set;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final static Logger logger = LoggerFactory.getLogger(TestController.class);

    private final WebClient webClient;
    private final JobLauncher jobLauncher;
    private final JobExplorer jobExplorer;
    private final Job movieScrappingJob;

    @GetMapping("/test")
    public ResponseEntity<kobisResponse> test(){
        ResponseEntity<kobisResponse> obj = webClient.get().uri("/boxoffice/searchDailyBoxOfficeList.json?key=0845e868204bfaa50d9b393c2dd5c499&&targetDt=20211206")
                .retrieve().toEntity(kobisResponse.class)
                .block();
        logger.info("이거 값은? >>>> {}", obj.getBody());
        return obj;
    }

    @GetMapping("/batch")
    public ResponseEntity<String> batch() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException, NoSuchJobException {
        Set<JobExecution> set = jobExplorer.findRunningJobExecutions("movieScrappingJob"); // 잡이 실행했는지를 판단.
        if (set.size()>0) {
            logger.warn("배치 실행중인데 뭐하는 짓~~~ ");
            return ResponseEntity.badRequest().body("배치잡 실행중");
        }
        logger.info(">>>> Batch FOUND ");
        JobParameters parameters = new JobParametersBuilder().addString("JobDate", LocalDate.now().toString()).toJobParameters(); //파라미터 생성후 => 파라미터를 거는게 더 안전하다. 파라미터가 키값이 되서 작동하기때문
//        JobParameters parameters = new JobParametersBuilder().toJobParameters();
        jobLauncher.run(movieScrappingJob, parameters); // 잡런처를 통해서 특정 잡 실행.
        return ResponseEntity.ok("배치가 작동합니다. 실행중...");
    }
}
