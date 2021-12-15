package com.ventulus95.springbatch.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Slf4j
@RequiredArgsConstructor //나는 분할하는 역할일세 허허
public class MoviePartitioner implements Partitioner {

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        int total = 7000;//new Random().nextInt(10, 80);
        Map<String, ExecutionContext> map = new HashMap<>(gridSize);
        int start = 1;
        int page = 10;
        int range = total/gridSize;
        while (start < total){
            ExecutionContext context = new ExecutionContext();
            map.put("partition"+start, context);
            context.putInt("start", start/page);
            start+=range;
            log.info("end >>>> {} ", start);
            context.putInt("end", start/page);
        }
        log.info("total >>>> {} ", total);

        return map;
    }
}
