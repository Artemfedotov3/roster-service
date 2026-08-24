package com.spring.springboot.roster_practice.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.spring.springboot.roster_practice.dto.UnitInfoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class UnitServiceClient {

    private final RestTemplate restTemplate;

    @Value("${unit.service.url}")
    private String unitServiceUrl;

    // ===== ПОЛУЧЕНИЕ ЮНИТОВ =====

    public List<UnitInfoDto> getAllUnits() {
        String url = unitServiceUrl + "/api/v1/units_models";
        log.info("📡 Calling Unit Service: {}", url);

        try {
            ResponseEntity<List<UnitInfoDto>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<UnitInfoDto>>() {}
            );
            log.info("✅ Received {} units", response.getBody().size());
            return response.getBody();
        } catch (Exception e) {
            log.error("❌ Error calling Unit Service: {}", e.getMessage());
            return List.of();
        }
    }

    public List<UnitInfoDto> getUnitsByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        List<UnitInfoDto> allUnits = getAllUnits();
        return allUnits.stream()
                .filter(unit -> ids.contains(unit.getId()))
                .collect(Collectors.toList());
    }

    // ===== РАСЧЕТ ПОЛНОЙ СТОИМОСТИ =====

    public int calculateTotalCost(List<Long> unitIds) {
        log.info("💰 Calculating total cost for unit IDs: {}", unitIds);

        if (unitIds == null || unitIds.isEmpty()) {
            return 0;
        }

        int total = 0;

        for (Long unitId : unitIds) {
            int unitTotal = calculateUnitTotalCost(unitId);
            log.info("✅ Unit {} total cost: {}", unitId, unitTotal);
            total += unitTotal;
        }

        log.info("💰 TOTAL COST FOR ALL UNITS: {}", total);
        return total;
    }

    // ===== РАСЧЕТ СТОИМОСТИ ОДНОГО ЮНИТА =====

    private int calculateUnitTotalCost(Long unitId) {
        // 1. Получаем стоимость юнита
        int unitCost = getUnitCostById(unitId);

        // 2. Получаем стоимости снаряжения (используем существующие эндпоинты)
        int upgradesCost = getTotalCostFromEndpoint("/api/v1/upgrade/by-unit/" + unitId);
        int exoticBeastsCost = getTotalCostFromEndpoint("/exotic_beast_unit/" + unitId);
        int equipmentsCost = getTotalCostFromEndpoint("/api/v1/equipment/unit/" + unitId);
        int armoursCost = getTotalCostFromEndpoint("/api/v1/armour/by-unit/" + unitId);

        int total = unitCost + upgradesCost + exoticBeastsCost + equipmentsCost + armoursCost;

        log.info("📊 Unit {}: unit={}, upgrades={}, exotic={}, equipment={}, armour={} → TOTAL={}",
                unitId, unitCost, upgradesCost, exoticBeastsCost, equipmentsCost, armoursCost, total);

        return total;
    }

    private int getUnitCostById(Long unitId) {
        List<UnitInfoDto> units = getUnitsByIds(List.of(unitId));
        if (units.isEmpty()) {
            log.warn("⚠️ Unit not found: {}", unitId);
            return 0;
        }
        return units.get(0).getCost();
    }

    // ===== УНИВЕРСАЛЬНЫЙ МЕТОД ДЛЯ ПОЛУЧЕНИЯ СУММЫ ИЗ ЛЮБОГО ЭНДПОИНТА =====

    private int getTotalCostFromEndpoint(String path) {
        String url = unitServiceUrl + path;
        log.info("📡 Getting cost from: {}", url);

        try {
            // Получаем ответ как JsonNode (не зависит от DTO)
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);

            JsonNode body = response.getBody();
            if (body == null || !body.isArray()) {
                return 0;
            }

            // Суммируем все cost в массиве
            int total = 0;
            for (JsonNode item : body) {
                JsonNode costNode = item.get("cost");
                if (costNode != null && costNode.isInt()) {
                    total += costNode.asInt();
                }
            }

            log.info("✅ Total from {}: {}", path, total);
            return total;

        } catch (Exception e) {
            log.error("❌ Error getting cost from {}: {}", path, e.getMessage());
            return 0;
        }
    }
}