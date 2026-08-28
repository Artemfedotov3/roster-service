package com.spring.springboot.roster_practice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.springboot.roster_practice.client.UnitServiceClient;
import com.spring.springboot.roster_practice.dto.RosterRequestDto;
import com.spring.springboot.roster_practice.dto.RosterResponseDto;
import com.spring.springboot.roster_practice.entity.RosterEntity;
import com.spring.springboot.roster_practice.repository.RosterRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@Tag("roster-test")
@Tag("service")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@TestMethodOrder(MethodOrderer.DisplayName.class)
@ExtendWith(MockitoExtension.class)
public class RosterServiceImplTest {

    private static final RosterRequestDto TEST_REQUEST = new RosterRequestDto();

    @Mock
    private UnitServiceClient unitServiceClient;

    @Mock
    private RosterRepository rosterRepository;

    @Mock
    private ObjectMapper objectMapper;  // ← ДОБАВЛЕНО

    @InjectMocks
    private RosterServiceImpl rosterServiceImpl;

    RosterServiceImplTest(TestInfo testInfo) {
        System.out.println("Constructor: " + testInfo.getDisplayName());
    }

    @BeforeAll
    static void init() {
        System.out.println("Before all: ");
        TEST_REQUEST.setName("мои кочевники");
        TEST_REQUEST.setDescription("кочевники пустошей");
        TEST_REQUEST.setUnitIds(List.of(1L, 2L));
    }

    @BeforeEach
    void prepare() {
        System.out.println("Before each: ");
    }

    @Test
    void rostersEmptyIfNoRosterAdded() {
        System.out.println("Test rosterEmptyIfNoRosterAdded " + this);

        when(rosterRepository.findAll()).thenReturn(List.of());

        var rosters = rosterServiceImpl.getAllRosters();
        Assertions.assertTrue(rosters.isEmpty(), () -> "Roster list should be empty");
    }

    @Test
    @DisplayName("create-roster-test")
    void createRosterTest() throws JsonProcessingException {

        RosterEntity entity = new RosterEntity();
        entity.setId(1L);
        entity.setName("мои кочевники");
        entity.setDescription("кочевники пустошей");
        entity.setTotalCost(500);
        entity.setUnitIds("[1, 2]");

        RosterResponseDto response = new RosterResponseDto();
        response.setId(1L);
        response.setName("мои кочевники");
        response.setDescription("кочевники пустошей");
        response.setTotalCost(500);
        response.setUnitIds(List.of(1L, 2L));

        when(objectMapper.writeValueAsString(anyList())).thenReturn("[1, 2]");
        when(objectMapper.readValue(anyString(), any(TypeReference.class))).thenReturn(List.of(1L, 2L));
        when(unitServiceClient.calculateTotalCost(anyList())).thenReturn(500);
        when(rosterRepository.save(any(RosterEntity.class))).thenReturn(entity);

        RosterResponseDto result = rosterServiceImpl.createRoster(TEST_REQUEST);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("мои кочевники");
        assertThat(result.getDescription()).isEqualTo("кочевники пустошей");
        assertThat(result.getTotalCost()).isEqualTo(500);

        verify(unitServiceClient, times(1)).calculateTotalCost(anyList());
        verify(unitServiceClient, times(1)).getUnitsByIds(anyList());
        verify(rosterRepository, times(1)).save(any(RosterEntity.class));
        verify(objectMapper, times(1)).writeValueAsString(anyList());
    }

    @Test
    @DisplayName("update-roster-test")
    void updateRosterTest() throws JsonProcessingException {

        Long rosterId = 1L;

        RosterEntity existingEntity = new RosterEntity();
        existingEntity.setId(rosterId);
        existingEntity.setName("мои кочевники");
        existingEntity.setDescription("кочевники пустошей");
        existingEntity.setTotalCost(500);
        existingEntity.setUnitIds("[1, 2]");

        RosterRequestDto updateRequest = new RosterRequestDto();
        updateRequest.setName("кочевники бури");
        updateRequest.setDescription("кочевники подулья");
        updateRequest.setUnitIds(List.of(3L, 4L));

        RosterEntity updatedEntity = new RosterEntity();
        updatedEntity.setId(rosterId);
        updatedEntity.setName("кочевники бури");
        updatedEntity.setDescription("кочевники подулья");
        updatedEntity.setTotalCost(700);
        updatedEntity.setUnitIds("[3, 4]");

        when(rosterRepository.findById(rosterId)).thenReturn(Optional.of(existingEntity));
        when(unitServiceClient.calculateTotalCost(anyList())).thenReturn(700);
        when(objectMapper.writeValueAsString(anyList())).thenReturn("[3, 4]");
        when(objectMapper.readValue(anyString(), any(TypeReference.class))).thenReturn(List.of(3L, 4L));
        when(rosterRepository.save(any(RosterEntity.class))).thenReturn(updatedEntity);

        RosterResponseDto result = rosterServiceImpl.updateRoster(rosterId, updateRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(rosterId);
        assertThat(result.getName()).isEqualTo("кочевники бури");
        assertThat(result.getDescription()).isEqualTo("кочевники подулья");
        assertThat(result.getTotalCost()).isEqualTo(700);
        assertThat(result.getUnitIds()).containsExactly(3L, 4L);

        verify(rosterRepository, times(1)).findById(rosterId);
        verify(unitServiceClient, times(1)).calculateTotalCost(anyList());
        verify(unitServiceClient, times(1)).getUnitsByIds(anyList());
        verify(objectMapper, times(1)).writeValueAsString(anyList());
        verify(rosterRepository, times(1)).save(any(RosterEntity.class));
    }

    @Test
    @DisplayName("delete-roster-test")
    void deleteRosterTest(){

        Long rosterId = 1L;

        RosterEntity entity = new RosterEntity();
        entity.setId(rosterId);

        when(rosterRepository.findById(rosterId)).thenReturn(Optional.of(entity));
        doNothing().when(rosterRepository).delete(any(RosterEntity.class));

        rosterServiceImpl.deleteRoster(rosterId);

        verify(rosterRepository, times(1)).findById(rosterId);
        verify(rosterRepository, times(1)).delete(any(RosterEntity.class));
    }

    @AfterEach
    void deleteDataFromDatabase() {
        System.out.println("After each: " + this);
    }

    @AfterAll
    static void closeConnectionPool() {
        System.out.println("After all: ");
    }
}
