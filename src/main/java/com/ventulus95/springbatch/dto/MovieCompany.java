package com.ventulus95.springbatch.dto;

import com.ventulus95.springbatch.model.MovieCmm;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@ToString
public class MovieCompany {

    private long companyCd;
    private String companyNm;
    private String companyPartNames;

    public MovieCmm toEntity(){
        return MovieCmm.builder()
                .id(companyCd)
                .name(companyNm)
                .partName(companyPartNames)
                .build();
    }
}
