package com.ventulus95.springbatch;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final static Logger logger = LoggerFactory.getLogger(TestController.class);

    private final WebClient webClient;

    @GetMapping("/test")
    public String test(){
        ResponseEntity<Object> obj = webClient.get().uri("/searchDailyBoxOfficeList.json?key=0845e868204bfaa50d9b393c2dd5c499&&targetDt=20211206").retrieve().toEntity(Object.class).block();
        logger.info("이거 값은? >>>> {}", obj.getBody());
        return "Test";
    }
}
