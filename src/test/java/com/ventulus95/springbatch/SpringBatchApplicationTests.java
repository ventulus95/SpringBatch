package com.ventulus95.springbatch;

import com.ventulus95.springbatch.dto.kobisResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@SpringBootTest
class SpringBatchApplicationTests {

    private static final Logger logger = LoggerFactory.getLogger(SpringBatchApplicationTests.class);

    @Autowired
    private WebClient webClient;

    @Test
    @DisplayName("webclient 페이징 처리방식")
    void contextLoads() {
        int page = 1;

        int total = webClient.get().uri("/company/searchCompanyList.json?key=f5eef3421c602c6cb7ea224104795888")
                .retrieve().toEntity(kobisResponse.class)
                .map( m -> m.getBody())
                .map( r -> r.getCompanyListResult())
                .map(l -> l.getTotCnt())
                .block();
        logger.info("TOTAL VALUES!!!!!!!! -> {} ", total);

        for (;page<4; page++){
            System.out.println(page);
            ResponseEntity<kobisResponse> res =webClient.get().uri("/company/searchCompanyList.json?key=f5eef3421c602c6cb7ea224104795888&itemPerPage=1&curPage="+page)
                    .retrieve().toEntity(kobisResponse.class)
                    .block();
            System.out.println(res.getBody());

        }

//        list.forEach(System.out::println);
    }

}
