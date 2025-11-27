package com.goody.nus.se.gomoku.ranking.impl;

import com.goody.nus.se.gomoku.ranking.model.dao.ScoreMapper;
import com.goody.nus.se.gomoku.ranking.model.dao.customer.CustomerScoreMapper;
import com.goody.nus.se.gomoku.ranking.model.dto.ScoreDTO;
import com.goody.nus.se.gomoku.ranking.model.entity.Score;
import com.goody.nus.se.gomoku.ranking.service.impl.ScoreServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mybatis.dynamic.sql.select.CountDSLCompleter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyCollection;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link ScoreServiceImpl}
 *
 * @author Claude
 * @version 1.0
 */
class ScoreServiceImplTest {

    @Mock
    private ScoreMapper scoreMapper;

    @Mock
    private CustomerScoreMapper customerScoreMapper;

    @InjectMocks
    private ScoreServiceImpl scoreService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void save_withValidDTO_shouldReturnId() {
        // Given
        ScoreDTO dto = ScoreDTO.builder()
                .userId(123L)
                .build();

        when(scoreMapper.insertSelective(any(Score.class))).thenAnswer(invocation -> {
            Score entity = invocation.getArgument(0);
            entity.setId(999L);
            return 1;
        });

        // When
        Long result = scoreService.save(dto);

        // Then
        assertNotNull(result);
        verify(scoreMapper, times(1)).insertSelective(any(Score.class));
    }

    @Test
    void save_withNullDTO_shouldReturnNull() {
        // When
        Long result = scoreService.save(null);

        // Then
        assertNull(result);
        verify(scoreMapper, never()).insertSelective(any());
    }

    @Test
    void saveBatch_withValidList_shouldInsertAll() {
        // Given
        List<ScoreDTO> dtoList = Arrays.asList(
                ScoreDTO.builder().userId(1L).build(),
                ScoreDTO.builder().userId(2L).build()
        );

        when(scoreMapper.insertMultiple(anyList())).thenReturn(2);

        // When
        int result = scoreService.saveBatch(dtoList);

        // Then
        assertEquals(2, result);
        verify(scoreMapper, times(1)).insertMultiple(anyList());
    }

    @Test
    void saveBatch_withEmptyList_shouldReturnZero() {
        // When
        int result = scoreService.saveBatch(Collections.emptyList());

        // Then
        assertEquals(0, result);
        verify(scoreMapper, never()).insertMultiple(anyList());
    }

    @Test
    void update_withValidDTO_shouldReturnOne() {
        // Given
        ScoreDTO dto = ScoreDTO.builder()
                .id(123L)
                .userId(456L)
                .build();

        when(scoreMapper.updateByPrimaryKeySelective(any(Score.class))).thenReturn(1);

        // When
        int result = scoreService.update(dto);

        // Then
        assertEquals(1, result);
        verify(scoreMapper, times(1)).updateByPrimaryKeySelective(any(Score.class));
    }

    @Test
    void update_withNullDTO_shouldReturnZero() {
        // When
        int result = scoreService.update(null);

        // Then
        assertEquals(0, result);
        verify(scoreMapper, never()).updateByPrimaryKeySelective(any());
    }

    @Test
    void deleteById_withValidId_shouldReturnOne() {
        // Given
        Long id = 123L;
        when(scoreMapper.deleteByPrimaryKey(id)).thenReturn(1);

        // When
        int result = scoreService.deleteById(id);

        // Then
        assertEquals(1, result);
        verify(scoreMapper, times(1)).deleteByPrimaryKey(id);
    }

    @Test
    void deleteById_withNullId_shouldReturnZero() {
        // When
        int result = scoreService.deleteById(null);

        // Then
        assertEquals(0, result);
        verify(scoreMapper, never()).deleteByPrimaryKey(any());
    }

    @Test
    void findById_withExistingId_shouldReturnDTO() {
        // Given
        Long id = 123L;
        Score entity = new Score();
        entity.setId(id);
        entity.setUserId(456L);

        when(scoreMapper.selectByPrimaryKey(id)).thenReturn(Optional.of(entity));

        // When
        ScoreDTO result = scoreService.findById(id);

        // Then
        assertNotNull(result);
        assertEquals(id, result.getId());
        verify(scoreMapper, times(1)).selectByPrimaryKey(id);
    }

    @Test
    void findById_withNonExistingId_shouldReturnNull() {
        // Given
        Long id = 999L;
        when(scoreMapper.selectByPrimaryKey(id)).thenReturn(Optional.empty());

        // When
        ScoreDTO result = scoreService.findById(id);

        // Then
        assertNull(result);
        verify(scoreMapper, times(1)).selectByPrimaryKey(id);
    }

    @Test
    void findById_withNullId_shouldReturnNull() {
        // When
        ScoreDTO result = scoreService.findById(null);

        // Then
        assertNull(result);
        verify(scoreMapper, never()).selectByPrimaryKey(any());
    }

    @Test
    void save_withExistingId_shouldUseProvidedId() {
        // Given
        ScoreDTO dto = ScoreDTO.builder()
                .id(123L)
                .userId(456L)
                .build();

        when(scoreMapper.insertSelective(any(Score.class))).thenAnswer(invocation -> {
            Score entity = invocation.getArgument(0);
            return 1;
        });

        // When
        Long result = scoreService.save(dto);

        // Then
        assertNotNull(result);
        verify(scoreMapper, times(1)).insertSelective(any(Score.class));
    }

    @Test
    void save_whenInsertFails_shouldReturnNull() {
        // Given
        ScoreDTO dto = ScoreDTO.builder().userId(123L).build();
        when(scoreMapper.insertSelective(any(Score.class))).thenReturn(0);

        // When
        Long result = scoreService.save(dto);

        // Then
        assertNull(result);
        verify(scoreMapper, times(1)).insertSelective(any(Score.class));
    }

    @Test
    void saveBatch_withNullList_shouldReturnZero() {
        // When
        int result = scoreService.saveBatch(null);

        // Then
        assertEquals(0, result);
        verify(scoreMapper, never()).insertMultiple(anyCollection());
    }

    @Test
    void saveBatch_withNullDTOsInList_shouldFilterThem() {
        // Given
        ScoreDTO dto = ScoreDTO.builder().userId(1L).build();
        List<ScoreDTO> dtoList = Arrays.asList(dto, null, null);

        when(scoreMapper.insertMultiple(anyList())).thenReturn(1);

        // When
        int result = scoreService.saveBatch(dtoList);

        // Then
        assertEquals(1, result);
        verify(scoreMapper, times(1)).insertMultiple(anyList());
    }

    @Test
    void saveBatch_withAllNullDTOs_shouldReturnZero() {
        // Given
        List<ScoreDTO> dtoList = Arrays.asList(null, null);

        // When
        int result = scoreService.saveBatch(dtoList);

        // Then
        assertEquals(0, result);
        verify(scoreMapper, never()).insertMultiple(anyList());
    }

    @Test
    void findAll_shouldReturnAllScores() {
        // Given
        Score entity1 = new Score();
        entity1.setId(1L);
        Score entity2 = new Score();
        entity2.setId(2L);

        when(scoreMapper.select(any())).thenReturn(List.of(entity1, entity2));

        // When
        List<ScoreDTO> result = scoreService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(scoreMapper, times(1)).select(any());
    }

    @Test
    void findByUserIdAndLeaderboardRuleId_withValidParams_shouldReturnList() {
        // Given
        Long userId = 123L;
        Long leaderboardRuleId = 456L;
        Score entity = new Score();
        entity.setId(1L);
        entity.setUserId(userId);
        entity.setLeaderboardRuleId(leaderboardRuleId);

        when(scoreMapper.select(any())).thenReturn(List.of(entity));

        // When
        List<ScoreDTO> result = scoreService.findByUserIdAndLeaderboardRuleId(userId, leaderboardRuleId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(scoreMapper, times(1)).select(any());
    }

    @Test
    void findByUserIdAndLeaderboardRuleId_withNullUserId_shouldReturnEmptyList() {
        // When
        List<ScoreDTO> result = scoreService.findByUserIdAndLeaderboardRuleId(null, 456L);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(scoreMapper, never()).select(any());
    }

    @Test
    void findByUserIdAndLeaderboardRuleId_withNullLeaderboardRuleId_shouldReturnEmptyList() {
        // When
        List<ScoreDTO> result = scoreService.findByUserIdAndLeaderboardRuleId(123L, null);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(scoreMapper, never()).select(any());
    }

    @Test
    void findByUserIdAndLeaderboardRuleId_withBothNull_shouldReturnEmptyList() {
        // When
        List<ScoreDTO> result = scoreService.findByUserIdAndLeaderboardRuleId(null, null);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(scoreMapper, never()).select(any());
    }

    @Test
    void countByMatchId_withValidId_shouldReturnCount() {
        // Given
        Long matchId = 123L;
        when(scoreMapper.count(any(CountDSLCompleter.class))).thenReturn(5L);

        // When
        long result = scoreService.countByMatchId(matchId);

        // Then
        assertEquals(5L, result);
        verify(scoreMapper, times(1)).count(any(CountDSLCompleter.class));
    }

    @Test
    void countByMatchId_withNullId_shouldReturnZero() {
        // When
        long result = scoreService.countByMatchId(null);

        // Then
        assertEquals(0L, result);
        verify(scoreMapper, never()).count(any(CountDSLCompleter.class));
    }
}
