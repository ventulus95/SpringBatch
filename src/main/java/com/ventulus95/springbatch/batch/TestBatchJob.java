package com.ventulus95.springbatch.batch;

import java.util.LinkedList;
import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.item.support.ListItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class TestBatchJob {

	private static final String BATCH_JOB  = "TEST_JOB";

	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;

	@Bean(name = BATCH_JOB)
	public Job Job(){
		return jobBuilderFactory.get(BATCH_JOB)
			.start(step())
			.build();
	}

	@Bean(name = BATCH_JOB+"_step")
	public Step step(){
		return stepBuilderFactory.get(BATCH_JOB+"_step")
			.<String, String>chunk(10)
			.reader(reader())
			.processor(processor())
			.writer(writer())
			.build();
	}


	@Bean(name = BATCH_JOB+"_reader")
	@StepScope
	public ListItemReader<String> reader(){
		List<String> list = new LinkedList<>();
		for (int i = 0; i <30; i++) {
			list.add(String.valueOf(i));
		}
		return new ListItemReader<>(list);
	}

	@Bean(name = BATCH_JOB+"_processor")
	@StepScope
	public ItemProcessor<String, String> processor(){
		return item -> {
			log.info(item);
			return item;
		};
	}

	@Bean(name = BATCH_JOB+"_writer")
	@StepScope
	public ListItemWriter<String> writer(){
		log.info("asdf!");
		return new ListItemWriter<>();
	}
}
