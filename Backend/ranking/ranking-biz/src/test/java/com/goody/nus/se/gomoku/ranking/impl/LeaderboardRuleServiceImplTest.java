package com.goody.nus.se.gomoku.ranking.impl;

import com.goody.nus.se.gomoku.ranking.model.dao.LeaderboardRuleMapper;
import com.goody.nus.se.gomoku.ranking.model.dto.LeaderboardRuleDTO;
import com.goody.nus.se.gomoku.ranking.model.entity.LeaderboardRule;
import com.goody.nus.se.gomoku.ranking.service.impl.LeaderboardRuleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mybatis.dynamic.sql.select.SelectDSLCompleter;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link LeaderboardRuleServiceImpl}
 *
 * @author Claude
 * @version 1.0
 */
class LeaderboardRuleServiceImplTest {

    @Mock
    private LeaderboardRuleMapper leaderboardRuleMapper;

    @InjectMocks
    private LeaderboardRuleServiceImpl leaderboardRuleService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void save_withValidDTO_shouldReturnId() {
        // Given
        LeaderboardRuleDTO dto = LeaderboardRuleDTO.builder()
                .build();

        when(leaderboardRuleMapper.insertSelective(any(LeaderboardRule.class))).thenAnswer(invocation -> {
            LeaderboardRule entity = invocation.getArgument(0);
            entity.setId(999L);
            return 1;
        });

        // When
        Long result = leaderboardRuleService.save(dto);

        // Then
        assertNotNull(result);
        verify(leaderboardRuleMapper, times(1)).insertSelective(any(LeaderboardRule.class));
    }

    @Test
    void save_withNullDTO_shouldReturnNull() {
        // When
        Long result = leaderboardRuleService.save(null);

        // Then
        assertNull(result);
        verify(leaderboardRuleMapper, never()).insertSelective(any());
    }

    @Test
    void update_withValidDTO_shouldReturnOne() {
        // Given
        LeaderboardRuleDTO dto = LeaderboardRuleDTO.builder()
                .id(123L)
                .build();

        when(leaderboardRuleMapper.updateByPrimaryKeySelective(any(LeaderboardRule.class))).thenReturn(1);

        // When
        int result = leaderboardRuleService.update(dto);

        // Then
        assertEquals(1, result);
        verify(leaderboardRuleMapper, times(1)).updateByPrimaryKeySelective(any(LeaderboardRule.class));
    }

    @Test
    void deleteById_withValidId_shouldReturnOne() {
        // Given
        Long id = 123L;
        when(leaderboardRuleMapper.deleteByPrimaryKey(id)).thenReturn(1);

        // When
        int result = leaderboardRuleService.deleteById(id);

        // Then
        assertEquals(1, result);
        verify(leaderboardRuleMapper, times(1)).deleteByPrimaryKey(id);
    }

    @Test
    void findById_withExistingId_shouldReturnDTO() {
        // Given
        Long id = 123L;
        LeaderboardRule entity = new LeaderboardRule();
        entity.setId(id);

        when(leaderboardRuleMapper.selectByPrimaryKey(id)).thenReturn(Optional.of(entity));

        // When
        LeaderboardRuleDTO result = leaderboardRuleService.findById(id);

        // Then
        assertNotNull(result);
        assertEquals(id, result.getId());
        verify(leaderboardRuleMapper, times(1)).selectByPrimaryKey(id);
    }

    @Test
    void findById_withNullId_shouldReturnNull() {
        // When
        LeaderboardRuleDTO result = leaderboardRuleService.findById(null);

        // Then
        assertNull(result);
        verify(leaderboardRuleMapper, never()).selectByPrimaryKey(any());
    }

    @Test
    void findById_withNonExistingId_shouldReturnNull() {
        // Given
        Long id = 999L;
        when(leaderboardRuleMapper.selectByPrimaryKey(id)).thenReturn(Optional.empty());

        // When
        LeaderboardRuleDTO result = leaderboardRuleService.findById(id);

        // Then
        assertNull(result);
        verify(leaderboardRuleMapper, times(1)).selectByPrimaryKey(id);
    }

    @Test
    void save_withExistingId_shouldUseProvidedId() {
        // Given
        LeaderboardRuleDTO dto = LeaderboardRuleDTO.builder()
                .id(123L)
                .build();

        when(leaderboardRuleMapper.insertSelective(any(LeaderboardRule.class))).thenAnswer(invocation -> {
            LeaderboardRule entity = invocation.getArgument(0);
            return 1;
        });

        // When
        Long result = leaderboardRuleService.save(dto);

        // Then
        assertNotNull(result);
        verify(leaderboardRuleMapper, times(1)).insertSelective(any(LeaderboardRule.class));
    }

    @Test
    void save_whenInsertFails_shouldReturnNull() {
        // Given
        LeaderboardRuleDTO dto = LeaderboardRuleDTO.builder().build();
        when(leaderboardRuleMapper.insertSelective(any(LeaderboardRule.class))).thenReturn(0);

        // When
        Long result = leaderboardRuleService.save(dto);

        // Then
        assertNull(result);
        verify(leaderboardRuleMapper, times(1)).insertSelective(any(LeaderboardRule.class));
    }

    @Test
    void saveBatch_withNullList_shouldReturnZero() {
        // When
        int result = leaderboardRuleService.saveBatch(null);

        // Then
        assertEquals(0, result);
        verify(leaderboardRuleMapper, never()).insertMultiple(anyCollection());
    }

    @Test
    void saveBatch_withEmptyList_shouldReturnZero() {
        // Given
        List<LeaderboardRuleDTO> emptyList = List.of();

        // When
        int result = leaderboardRuleService.saveBatch(emptyList);

        // Then
        assertEquals(0, result);
        verify(leaderboardRuleMapper, never()).insertMultiple(anyCollection());
    }

    @Test
    void saveBatch_withValidDTOs_shouldGenerateIdsAndTimestamps() {
        // Given
        LeaderboardRuleDTO dto1 = LeaderboardRuleDTO.builder().build();
        LeaderboardRuleDTO dto2 = LeaderboardRuleDTO.builder().id(100L).build();
        List<LeaderboardRuleDTO> dtoList = List.of(dto1, dto2);

        when(leaderboardRuleMapper.insertMultiple(anyCollection())).thenReturn(2);

        // When
        int result = leaderboardRuleService.saveBatch(dtoList);

        // Then
        assertEquals(2, result);
        verify(leaderboardRuleMapper, times(1)).insertMultiple(anyCollection());
    }

    @Test
    void saveBatch_withNullDTOsInList_shouldFilterThem() {
        // Given
        LeaderboardRuleDTO dto = LeaderboardRuleDTO.builder().build();
        List<LeaderboardRuleDTO> dtoList = Arrays.asList(dto, null, null);

        when(leaderboardRuleMapper.insertMultiple(anyCollection())).thenReturn(1);

        // When
        int result = leaderboardRuleService.saveBatch(dtoList);

        // Then
        assertEquals(1, result);
        verify(leaderboardRuleMapper, times(1)).insertMultiple(anyCollection());
    }

    @Test
    void saveBatch_withAllNullDTOs_shouldReturnZero() {
        // Given
        List<LeaderboardRuleDTO> dtoList = Arrays.asList(null, null);

        // When
        int result = leaderboardRuleService.saveBatch(dtoList);

        // Then
        assertEquals(0, result);
        verify(leaderboardRuleMapper, never()).insertMultiple(anyCollection());
    }

    @Test
    void update_withNullDTO_shouldReturnZero() {
        // When
        int result = leaderboardRuleService.update(null);

        // Then
        assertEquals(0, result);
        verify(leaderboardRuleMapper, never()).updateByPrimaryKeySelective(any());
    }

    @Test
    void deleteById_withNullId_shouldReturnZero() {
        // When
        int result = leaderboardRuleService.deleteById(null);

        // Then
        assertEquals(0, result);
        verify(leaderboardRuleMapper, never()).deleteByPrimaryKey(any());
    }

    @Test
    void findAll_shouldReturnAllRules() {
        // Given
        LeaderboardRule entity1 = new LeaderboardRule();
        entity1.setId(1L);
        LeaderboardRule entity2 = new LeaderboardRule();
        entity2.setId(2L);

        when(leaderboardRuleMapper.select(any())).thenReturn(List.of(entity1, entity2));

        // When
        List<LeaderboardRuleDTO> result = leaderboardRuleService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(leaderboardRuleMapper, times(1)).select(any());
    }

    @Test
    void findActiveRules_shouldReturnActiveRulesWithinTimeRange() {
        // Given
        int currentTime = 100;
        LeaderboardRule entity = new LeaderboardRule();
        entity.setId(1L);
        entity.setStartTime(50);
        entity.setEndTime(150);

        when(leaderboardRuleMapper.select(any())).thenReturn(List.of(entity));

        // When
        List<LeaderboardRuleDTO> result = leaderboardRuleService.findActiveRules(currentTime);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(leaderboardRuleMapper, times(1)).select(any());
    }

    @Test
    void findByType_withValidType_shouldReturnDTO() {
        // Given
        String type = "WEEKLY";
        LeaderboardRule entity = new LeaderboardRule();
        entity.setId(1L);
        entity.setType(type);

        when(leaderboardRuleMapper.selectOne(any(SelectDSLCompleter.class))).thenReturn(Optional.of(entity));

        // When
        LeaderboardRuleDTO result = leaderboardRuleService.findByType(type);

        // Then
        assertNotNull(result);
        verify(leaderboardRuleMapper, times(1)).selectOne(any(SelectDSLCompleter.class));
    }

    @Test
    void findByType_withNullType_shouldReturnNull() {
        // When
        LeaderboardRuleDTO result = leaderboardRuleService.findByType(null);

        // Then
        assertNull(result);
        verify(leaderboardRuleMapper, never()).selectOne(any(SelectDSLCompleter.class));
    }

    @Test
    void findByType_withNoMatch_shouldReturnNull() {
        // Given
        String type = "MONTHLY";
        when(leaderboardRuleMapper.selectOne(any(SelectDSLCompleter.class))).thenReturn(Optional.empty());

        // When
        LeaderboardRuleDTO result = leaderboardRuleService.findByType(type);

        // Then
        assertNull(result);
        verify(leaderboardRuleMapper, times(1)).selectOne(any(SelectDSLCompleter.class));
    }

    @Test
    void findActiveRuleByType_withValidParams_shouldReturnDTO() {
        // Given
        String type = "DAILY";
        int currentTime = 100;
        LeaderboardRule entity = new LeaderboardRule();
        entity.setId(1L);

        when(leaderboardRuleMapper.selectOne(any(SelectDSLCompleter.class))).thenReturn(Optional.of(entity));

        // When
        LeaderboardRuleDTO result = leaderboardRuleService.findActiveRuleByType(type, currentTime);

        // Then
        assertNotNull(result);
        verify(leaderboardRuleMapper, times(1)).selectOne(any(SelectDSLCompleter.class));
    }

    @Test
    void findActiveRuleByType_withNullType_shouldReturnNull() {
        // When
        LeaderboardRuleDTO result = leaderboardRuleService.findActiveRuleByType(null, 100);

        // Then
        assertNull(result);
        verify(leaderboardRuleMapper, never()).selectOne(any(SelectDSLCompleter.class));
    }

    @Test
    void findActiveRuleByType_withNoMatch_shouldReturnNull() {
        // Given
        String type = "SEASONAL";
        int currentTime = 100;

        when(leaderboardRuleMapper.selectOne(any(SelectDSLCompleter.class))).thenReturn(Optional.empty());

        // When
        LeaderboardRuleDTO result = leaderboardRuleService.findActiveRuleByType(type, currentTime);

        // Then
        assertNull(result);
        verify(leaderboardRuleMapper, times(1)).selectOne(any(SelectDSLCompleter.class));
    }
}
