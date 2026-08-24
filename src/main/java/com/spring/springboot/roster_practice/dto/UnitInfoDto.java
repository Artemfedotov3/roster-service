package com.spring.springboot.roster_practice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnitInfoDto {

    private Long id;

    private String name;

    private String type;

    private int totalCost;

    private int cost;

    private List<ItemDto> equipment;

    private List<ItemDto> armour;

    private List<ItemDto> upgrade;

    private List<ItemDto> exoticBeasts;
}