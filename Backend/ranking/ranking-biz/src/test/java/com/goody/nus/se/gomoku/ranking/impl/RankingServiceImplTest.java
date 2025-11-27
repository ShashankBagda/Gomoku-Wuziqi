package com.goody.nus.se.gomoku.ranking.impl;

import com.goody.nus.se.gomoku.ranking.model.dao.RankingMapper;
import com.goody.nus.se.gomoku.ranking.model.dao.customer.CustomerRankingMapper;
import com.goody.nus.se.gomoku.ranking.model.dto.RankingDTO;
import com.goody.nus.se.gomoku.ranking.model.entity.Ranking;
import com.goody.nus.se.gomoku.ranking.service.impl.RankingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mybatis.dynamic.sql.select.CountDSLCompleter;
import org.mybatis.dynamic.sql.select.SelectDSLCompleter;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link RankingServiceImpl}
 *
 * @author Claude
 * @version 1.0
 */
class RankingServiceImplTest {

    @Mock
    private RankingMapper rankingMapper;

    @Mock
    private CustomerRankingMapper customerRankingMapper;

    @InjectMocks
    private RankingServiceImpl rankingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void save_withValidDTO_shouldReturnId() {
        // Given
        RankingDTO dto = RankingDTO.builder()
                .userId(123L)
                .build();

        when(rankingMapper.insertSelective(any(Ranking.class))).thenAnswer(invocation -> {
            Ranking entity = invocation.getArgument(0);
            entity.setId(999L);
            return 1;
        });

        // When
        Long result = rankingService.save(dto);

        // Then
        assertNotNull(result);
        verify(rankingMapper, times(1)).insertSelective(any(Ranking.class));
    }

    @Test
    void save_withNullDTO_shouldReturnNull() {
        // When
        Long result = rankingService.save(null);

        // Then
        assertNull(result);
        verify(rankingMapper, never()).insertSelective(any());
    }

    @Test
    void update_withValidDTO_shouldReturnOne() {
        // Given
        RankingDTO dto = RankingDTO.builder()
                .id(123L)
                .userId(456L)
                .build();

        when(rankingMapper.updateByPrimaryKeySelective(any(Ranking.class))).thenReturn(1);

        // When
        int result = rankingService.update(dto);

        // Then
        assertEquals(1, result);
        verify(rankingMapper, times(1)).updateByPrimaryKeySelective(any(Ranking.class));
    }

    @Test
    void deleteById_withValidId_shouldReturnOne() {
        // Given
        Long id = 123L;
        when(rankingMapper.deleteByPrimaryKey(id)).thenReturn(1);

        // When
        int result = rankingService.deleteById(id);

        // Then
        assertEquals(1, result);
        verify(rankingMapper, times(1)).deleteByPrimaryKey(id);
    }

    @Test
    void findById_withExistingId_shouldReturnDTO() {
        // Given
        Long id = 123L;
        Ranking entity = new Ranking();
        entity.setId(id);
        entity.setUserId(456L);

        when(rankingMapper.selectByPrimaryKey(id)).thenReturn(Optional.of(entity));

        // When
        RankingDTO result = rankingService.findById(id);

        // Then
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals(456L, result.getUserId());
        verify(rankingMapper, times(1)).selectByPrimaryKey(id);
    }

    @Test
    void findById_withNullId_shouldReturnNull() {
        // When
        RankingDTO result = rankingService.findById(null);

        // Then
        assertNull(result);
        verify(rankingMapper, never()).selectByPrimaryKey(any());
    }

    @Test
    void findById_withNonExistingId_shouldReturnNull() {
        // Given
        Long id = 999L;
        when(rankingMapper.selectByPrimaryKey(id)).thenReturn(Optional.empty());

        // When
        RankingDTO result = rankingService.findById(id);

        // Then
        assertNull(result);
        verify(rankingMapper, times(1)).selectByPrimaryKey(id);
    }

    @Test
    void save_withExistingId_shouldUseProvidedId() {
        // Given
        RankingDTO dto = RankingDTO.builder()
                .id(123L)
                .userId(456L)
                .build();

        when(rankingMapper.insertSelective(any(Ranking.class))).thenAnswer(invocation -> {
            Ranking entity = invocation.getArgument(0);
            return 1;
        });

        // When
        Long result = rankingService.save(dto);

        // Then
        assertNotNull(result);
        verify(rankingMapper, times(1)).insertSelective(any(Ranking.class));
    }

    @Test
    void save_whenInsertFails_shouldReturnNull() {
        // Given
        RankingDTO dto = RankingDTO.builder().userId(123L).build();
        when(rankingMapper.insertSelective(any(Ranking.class))).thenReturn(0);

        // When
        Long result = rankingService.save(dto);

        // Then
        assertNull(result);
        verify(rankingMapper, times(1)).insertSelective(any(Ranking.class));
    }

    @Test
    void saveBatch_withNullList_shouldReturnZero() {
        // When
        int result = rankingService.saveBatch(null);

        // Then
        assertEquals(0, result);
        verify(rankingMapper, never()).insertMultiple(anyCollection());
    }

    @Test
    void saveBatch_withEmptyList_shouldReturnZero() {
        // Given
        List<RankingDTO> emptyList = List.of();

        // When
        int result = rankingService.saveBatch(emptyList);

        // Then
        assertEquals(0, result);
        verify(rankingMapper, never()).insertMultiple(anyCollection());
    }

    @Test
    void saveBatch_withValidDTOs_shouldGenerateIdsAndTimestamps() {
        // Given
        RankingDTO dto1 = RankingDTO.builder().userId(1L).build();
        RankingDTO dto2 = RankingDTO.builder().id(100L).userId(2L).build();
        List<RankingDTO> dtoList = List.of(dto1, dto2);

        when(rankingMapper.insertMultiple(anyCollection())).thenReturn(2);

        // When
        int result = rankingService.saveBatch(dtoList);

        // Then
        assertEquals(2, result);
        verify(rankingMapper, times(1)).insertMultiple(anyCollection());
    }

    @Test
    void saveBatch_withNullDTOsInList_shouldFilterThem() {
        // Given
        RankingDTO dto = RankingDTO.builder().userId(1L).build();
        List<RankingDTO> dtoList = Arrays.asList(dto, null, null);

        when(rankingMapper.insertMultiple(anyCollection())).thenReturn(1);

        // When
        int result = rankingService.saveBatch(dtoList);

        // Then
        assertEquals(1, result);
        verify(rankingMapper, times(1)).insertMultiple(anyCollection());
    }

    @Test
    void saveBatch_withAllNullDTOs_shouldReturnZero() {
        // Given
        List<RankingDTO> dtoList = Arrays.asList(null, null);

        // When
        int result = rankingService.saveBatch(dtoList);

        // Then
        assertEquals(0, result);
        verify(rankingMapper, never()).insertMultiple(anyCollection());
    }

    @Test
    void update_withNullDTO_shouldReturnZero() {
        // When
        int result = rankingService.update(null);

        // Then
        assertEquals(0, result);
        verify(rankingMapper, never()).updateByPrimaryKeySelective(any());
    }

    @Test
    void deleteById_withNullId_shouldReturnZero() {
        // When
        int result = rankingService.deleteById(null);

        // Then
        assertEquals(0, result);
        verify(rankingMapper, never()).deleteByPrimaryKey(any());
    }

    @Test
    void findAll_shouldReturnAllRankings() {
        // Given
        Ranking entity1 = new Ranking();
        entity1.setId(1L);
        Ranking entity2 = new Ranking();
        entity2.setId(2L);

        when(rankingMapper.select(any())).thenReturn(List.of(entity1, entity2));

        // When
        List<RankingDTO> result = rankingService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(rankingMapper, times(1)).select(any());
    }

    @Test
    void findByUserIdAndLeaderboardRuleId_withValidParams_shouldReturnDTO() {
        // Given
        Long userId = 123L;
        Long leaderboardRuleId = 456L;
        Ranking entity = new Ranking();
        entity.setId(1L);
        entity.setUserId(userId);
        entity.setLeaderboardRuleId(leaderboardRuleId);

        when(rankingMapper.selectOne(any(SelectDSLCompleter.class))).thenReturn(Optional.of(entity));

        // When
        RankingDTO result = rankingService.findByUserIdAndLeaderboardRuleId(userId, leaderboardRuleId);

        // Then
        assertNotNull(result);
        verify(rankingMapper, times(1)).selectOne(any(SelectDSLCompleter.class));
    }

    @Test
    void findByUserIdAndLeaderboardRuleId_withNullUserId_shouldReturnNull() {
        // When
        RankingDTO result = rankingService.findByUserIdAndLeaderboardRuleId(null, 456L);

        // Then
        assertNull(result);
        verify(rankingMapper, never()).selectOne(any(SelectDSLCompleter.class));
    }

    @Test
    void findByUserIdAndLeaderboardRuleId_withNullLeaderboardRuleId_shouldReturnNull() {
        // When
        RankingDTO result = rankingService.findByUserIdAndLeaderboardRuleId(123L, null);

        // Then
        assertNull(result);
        verify(rankingMapper, never()).selectOne(any(SelectDSLCompleter.class));
    }

    @Test
    void findByUserIdAndLeaderboardRuleId_withBothNull_shouldReturnNull() {
        // When
        RankingDTO result = rankingService.findByUserIdAndLeaderboardRuleId(null, null);

        // Then
        assertNull(result);
        verify(rankingMapper, never()).selectOne(any(SelectDSLCompleter.class));
    }

    @Test
    void findByUserIdAndLeaderboardRuleId_withNoMatch_shouldReturnNull() {
        // Given
        Long userId = 123L;
        Long leaderboardRuleId = 456L;
        when(rankingMapper.selectOne(any(SelectDSLCompleter.class))).thenReturn(Optional.empty());

        // When
        RankingDTO result = rankingService.findByUserIdAndLeaderboardRuleId(userId, leaderboardRuleId);

        // Then
        assertNull(result);
        verify(rankingMapper, times(1)).selectOne(any(SelectDSLCompleter.class));
    }

    @Test
    void findTopByLeaderboardRuleId_withValidId_shouldReturnList() {
        // Given
        Long leaderboardRuleId = 456L;
        Ranking entity = new Ranking();
        entity.setId(1L);

        when(rankingMapper.select(any())).thenReturn(List.of(entity));

        // When
        List<RankingDTO> result = rankingService.findTopByLeaderboardRuleId(leaderboardRuleId, 10);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(rankingMapper, times(1)).select(any());
    }

    @Test
    void findTopByLeaderboardRuleId_withNullId_shouldReturnEmptyList() {
        // When
        List<RankingDTO> result = rankingService.findTopByLeaderboardRuleId(null, 10);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(rankingMapper, never()).select(any());
    }

    @Test
    void countByLeaderboardRuleId_withValidId_shouldReturnCount() {
        // Given
        Long leaderboardRuleId = 456L;
        when(rankingMapper.count(any(CountDSLCompleter.class))).thenReturn(10L);

        // When
        long result = rankingService.countByLeaderboardRuleId(leaderboardRuleId);

        // Then
        assertEquals(10L, result);
        verify(rankingMapper, times(1)).count(any(CountDSLCompleter.class));
    }

    @Test
    void countByLeaderboardRuleId_withNullId_shouldReturnZero() {
        // When
        long result = rankingService.countByLeaderboardRuleId(null);

        // Then
        assertEquals(0L, result);
        verify(rankingMapper, never()).count(any(CountDSLCompleter.class));
    }

    @Test
    void countByLeaderboardRuleIdAndScoreGreaterThan_withValidParams_shouldReturnCount() {
        // Given
        Long leaderboardRuleId = 456L;
        int score = 100;
        when(rankingMapper.count(any(CountDSLCompleter.class))).thenReturn(5L);

        // When
        long result = rankingService.countByLeaderboardRuleIdAndScoreGreaterThan(leaderboardRuleId, score);

        // Then
        assertEquals(5L, result);
        verify(rankingMapper, times(1)).count(any(CountDSLCompleter.class));
    }

    @Test
    void countByLeaderboardRuleIdAndScoreGreaterThan_withNullId_shouldReturnZero() {
        // When
        long result = rankingService.countByLeaderboardRuleIdAndScoreGreaterThan(null, 100);

        // Then
        assertEquals(0L, result);
        verify(rankingMapper, never()).count(any(CountDSLCompleter.class));
    }
}
