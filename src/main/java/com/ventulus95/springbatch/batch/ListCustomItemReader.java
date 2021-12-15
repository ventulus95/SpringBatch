/*
 * Copyright 2006-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ventulus95.springbatch.batch;

import com.ventulus95.springbatch.dto.MovieCompany;
import com.ventulus95.springbatch.dto.kobisResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.batch.item.database.AbstractPagingItemReader;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.LinkedList;
import java.util.List;

@Slf4j
public class ListCustomItemReader extends AbstractPagingItemReader<MovieCompany> {

	private WebClient client;
	private int start;
	private int end;

	public ListCustomItemReader(WebClient client, int pageSize, int start, int end) {
		this.client = client;
		this.start = start+1;
		this.end = end;
		log.info("리더 생성됨.");
		setPageSize(pageSize);
//		setMaxItemCount(end-start);
	}

//    @Nullable
//	@Override
//	public T read() {
//		if (!list.isEmpty()) {
//			log.info("읽기 진행중....");
//			return list.remove(0);
//		}
//		return null;
//	}

	@Override //페이지 직접 읽기.
	protected void doReadPage() {
		if (results == null) {
			results = new LinkedList<>();
		} else {
			results.clear();
		}
		int curr = this.start+getPage();
//		log.info("현재 쓰레드의 [{}] 현재 curr 값: {}, END: {}",Thread.currentThread().getName(), curr, this.end);
		if(curr<=this.end) {
			ResponseEntity<kobisResponse> res = client.get().uri("/company/searchCompanyList.json?key=e7512e393009207ffda32e42eec06112&itemPerPage=10&curPage="+curr)
					.retrieve().toEntity(kobisResponse.class)
					.block();
//			log.info("응답 콜좀 보자 body: {}", res.toString());
			results.addAll(res.getBody().companyListResult.getCompanyList());
		}
//		log.info("=>=>=>=> ListCustomItemReader DO READ PAGE");
//		log.info("~~~~~ ", getPage(), getPageSize());
	}

	@Override //페이지 점프
	protected void doJumpToPage(int itemIndex) {
		log.info("~~~~~~~~~~ ListCustomItemReader DO JUMP PAGE");
	}
}
