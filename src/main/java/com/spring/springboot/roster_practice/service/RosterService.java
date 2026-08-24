package com.spring.springboot.roster_practice.service;

import com.spring.springboot.roster_practice.dto.RosterRequestDto;
import com.spring.springboot.roster_practice.dto.RosterResponseDto;

import java.util.List;

public interface RosterService {

    List<RosterResponseDto> getAllRosters();

    RosterResponseDto getRosterById(Long id);

    RosterResponseDto createRoster(RosterRequestDto request);

    RosterResponseDto updateRoster(Long id, RosterRequestDto request);

    void deleteRoster(Long id);
}