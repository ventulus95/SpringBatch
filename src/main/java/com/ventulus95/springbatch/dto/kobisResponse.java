package com.ventulus95.springbatch.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Getter
@NoArgsConstructor
@ToString
public class kobisResponse {

    private BoxOfficeResult boxOfficeResult;
    public CompanyResult companyListResult;

    @Getter
    @ToString
    private static class BoxOfficeResult {
        private String boxofficeType;
        private String showRange;
        private List<BoxOfficeDto> dailyBoxOfficeList;
    }

    @Getter
    @ToString
    public static class CompanyResult {
        private int totCnt;
        private List<MovieCompany> companyList;
    }

}


