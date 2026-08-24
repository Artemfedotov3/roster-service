package com.spring.springboot.roster_practice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.springboot.roster_practice.client.UnitServiceClient;
import com.spring.springboot.roster_practice.dto.RosterRequestDto;
import com.spring.springboot.roster_practice.dto.RosterResponseDto;
import com.spring.springboot.roster_practice.dto.UnitInfoDto;
import com.spring.springboot.roster_practice.entity.RosterEntity;
import com.spring.springboot.roster_practice.repository.RosterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RosterServiceImpl implements RosterService {

    private static final int MAX_COST = 1000;

    private final RosterRepository rosterRepository;
    private final UnitServiceClient unitServiceClient;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public List<RosterResponseDto> getAllRosters() {
        return rosterRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RosterResponseDto getRosterById(Long id) {
        RosterEntity roster = rosterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Roster not found with id: " + id));
        return toDto(roster);
    }

    @Override
    @Transactional
    public RosterResponseDto createRoster(RosterRequestDto request) {
        // Проверка стоимости при создании
        int totalCost = unitServiceClient.calculateTotalCost(request.getUnitIds());
        if (totalCost > MAX_COST) {
            throw new RuntimeException("Total cost (" + totalCost + ") exceeds maximum allowed (" + MAX_COST + ")");
        }

        // Сохранение
        RosterEntity entity = RosterEntity.builder()
                .name(request.getName())
                .description(request.getDescription())
                .unitIds(convertToJson(request.getUnitIds()))
                .totalCost(totalCost)
                .build();

        RosterEntity saved = rosterRepository.save(entity);
        return toDto(saved);
    }

    @Override
    @Transactional
    public RosterResponseDto updateRoster(Long id, RosterRequestDto request) {
        RosterEntity entity = rosterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Roster not found with id: " + id));

        // Проверка стоимости при обновлении
        int totalCost = unitServiceClient.calculateTotalCost(request.getUnitIds());
        if (totalCost > MAX_COST) {
            throw new RuntimeException("Total cost (" + totalCost + ") exceeds maximum allowed (" + MAX_COST + ")");
        }

        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setUnitIds(convertToJson(request.getUnitIds()));
        entity.setTotalCost(totalCost);

        RosterEntity updated = rosterRepository.save(entity);
        return toDto(updated);
    }

    @Override
    @Transactional
    public void deleteRoster(Long id) {
        RosterEntity entity = rosterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Roster not found with id: " + id));
        rosterRepository.delete(entity);
    }

    private RosterResponseDto toDto(RosterEntity entity) {
        List<Long> unitIds = parseUnitIds(entity.getUnitIds());
        List<UnitInfoDto> units = unitServiceClient.getUnitsByIds(unitIds);

        return RosterResponseDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .unitIds(unitIds)
                .totalCost(entity.getTotalCost())
                .units(units)
                .build();
    }

    private String convertToJson(List<Long> unitIds) {
        try {
            return objectMapper.writeValueAsString(unitIds);
        } catch (JsonProcessingException e) {
            log.error("Error converting unitIds to JSON", e);
            return "[]";
        }
    }

    private List<Long> parseUnitIds(String json) {
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            // Явно указываем тип List<Long>
            return objectMapper.readValue(json, new TypeReference<List<Long>>() {});
        } catch (JsonProcessingException e) {
            log.error("Error parsing unitIds from JSON", e);
            return new ArrayList<>();
        }
    }
}
