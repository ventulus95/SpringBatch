package com.ventulus95.springbatch.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@ToString
public class BoxOfficeDto {

    private long rum;
    private int rank;
    private int rankInten;
    private String rankOldAndNew;
    private String movieCd;
    private String movieNm;
    private LocalDate openDt;
    private long salesAmt;
    private double salesShare;
    private long salesInten;
    private double salesChange;
    private long salesAcc;
    private long audiCnt;
    private long audiInten;
    private double audiChange;
    private long audiAcc;
    private long scrnCnt;
    private long showCnt;

}
