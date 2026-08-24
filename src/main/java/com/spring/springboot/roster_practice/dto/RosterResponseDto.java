package com.spring.springboot.roster_practice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RosterResponseDto {

    private Long id;

    private String name;

    private String description;

    private List<Long> unitIds;

    private int totalCost;

    private List<UnitInfoDto> units;
}