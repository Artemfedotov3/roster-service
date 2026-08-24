package com.spring.springboot.roster_practice.controller;

import com.spring.springboot.roster_practice.dto.RosterRequestDto;
import com.spring.springboot.roster_practice.dto.RosterResponseDto;
import com.spring.springboot.roster_practice.service.RosterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rosters")
@RequiredArgsConstructor
public class RosterController {

    private final RosterService rosterService;

    @GetMapping
    public ResponseEntity<List<RosterResponseDto>> getAllRosters() {
        return ResponseEntity.ok(rosterService.getAllRosters());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RosterResponseDto> getRosterById(@PathVariable Long id) {
        return ResponseEntity.ok(rosterService.getRosterById(id));
    }

    @PostMapping
    public ResponseEntity<RosterResponseDto> createRoster(@Valid @RequestBody RosterRequestDto request) {
        return ResponseEntity.ok(rosterService.createRoster(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RosterResponseDto> updateRoster(@PathVariable Long id,
                                                          @Valid @RequestBody RosterRequestDto request) {
        return ResponseEntity.ok(rosterService.updateRoster(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoster(@PathVariable Long id) {
        rosterService.deleteRoster(id);
        return ResponseEntity.noContent().build();
    }
}