package com.ventulus95.springbatch.batch;

import com.ventulus95.springbatch.dto.MovieCompany;
import com.ventulus95.springbatch.dto.kobisResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.database.AbstractPagingItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.LinkedList;

@Slf4j
public class ListCustomItemReader extends AbstractPagingItemReader<MovieCompany> {

	@Value("${kobis.secretKey}")
	private String key;

	private WebClient client;
	private int start;
	private int end;

	public ListCustomItemReader(WebClient client, int pageSize, int start, int end) {
		this.client = client;
		this.start = start+1;
		this.end = end;
		log.info("리더 생성됨.");
		setPageSize(pageSize);
		// 이거 설정해줘도 좋은데, 파라미터가 늘어남. start-end로만은 구현 불가.
//		setMaxItemCount(end-start);
	}


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
			ResponseEntity<kobisResponse> res = client.get().uri("/company/searchCompanyList.json?key="+key+"&itemPerPage=10&curPage="+curr)
					.retrieve().toEntity(kobisResponse.class)
					.block();
			results.addAll(res.getBody().companyListResult.getCompanyList());
		}
	}

	@Override // 중복 방지를 위한 페이지 점프
	protected void doJumpToPage(int itemIndex) {
	}
}
