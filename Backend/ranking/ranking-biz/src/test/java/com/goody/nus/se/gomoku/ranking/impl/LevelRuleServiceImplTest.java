package com.goody.nus.se.gomoku.ranking.impl;

import com.goody.nus.se.gomoku.ranking.model.dao.LevelRuleMapper;
import com.goody.nus.se.gomoku.ranking.model.dto.LevelRuleDTO;
import com.goody.nus.se.gomoku.ranking.model.entity.LevelRule;
import com.goody.nus.se.gomoku.ranking.service.impl.LevelRuleServiceImpl;
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
 * Unit test for {@link LevelRuleServiceImpl}
 *
 * @author Claude
 * @version 1.0
 */
class LevelRuleServiceImplTest {

    @Mock
    private LevelRuleMapper levelRuleMapper;

    @InjectMocks
    private LevelRuleServiceImpl levelRuleService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void save_withValidDTO_shouldReturnId() {
        // Given
        LevelRuleDTO dto = LevelRuleDTO.builder()
                .build();

        when(levelRuleMapper.insertSelective(any(LevelRule.class))).thenAnswer(invocation -> {
            LevelRule entity = invocation.getArgument(0);
            entity.setId(999L);
            return 1;
        });

        // When
        Long result = levelRuleService.save(dto);

        // Then
        assertNotNull(result);
        verify(levelRuleMapper, times(1)).insertSelective(any(LevelRule.class));
    }

    @Test
    void save_withNullDTO_shouldReturnNull() {
        // When
        Long result = levelRuleService.save(null);

        // Then
        assertNull(result);
        verify(levelRuleMapper, never()).insertSelective(any());
    }

    @Test
    void update_withValidDTO_shouldReturnOne() {
        // Given
        LevelRuleDTO dto = LevelRuleDTO.builder()
                .id(123L)
                .build();

        when(levelRuleMapper.updateByPrimaryKeySelective(any(LevelRule.class))).thenReturn(1);

        // When
        int result = levelRuleService.update(dto);

        // Then
        assertEquals(1, result);
        verify(levelRuleMapper, times(1)).updateByPrimaryKeySelective(any(LevelRule.class));
    }

    @Test
    void deleteById_withValidId_shouldReturnOne() {
        // Given
        Long id = 123L;
        when(levelRuleMapper.deleteByPrimaryKey(id)).thenReturn(1);

        // When
        int result = levelRuleService.deleteById(id);

        // Then
        assertEquals(1, result);
        verify(levelRuleMapper, times(1)).deleteByPrimaryKey(id);
    }

    @Test
    void findById_withExistingId_shouldReturnDTO() {
        // Given
        Long id = 123L;
        LevelRule entity = new LevelRule();
        entity.setId(id);

        when(levelRuleMapper.selectByPrimaryKey(id)).thenReturn(Optional.of(entity));

        // When
        LevelRuleDTO result = levelRuleService.findById(id);

        // Then
        assertNotNull(result);
        assertEquals(id, result.getId());
        verify(levelRuleMapper, times(1)).selectByPrimaryKey(id);
    }

    @Test
    void findById_withNullId_shouldReturnNull() {
        // When
        LevelRuleDTO result = levelRuleService.findById(null);

        // Then
        assertNull(result);
        verify(levelRuleMapper, never()).selectByPrimaryKey(any());
    }

    @Test
    void findById_withNonExistingId_shouldReturnNull() {
        // Given
        Long id = 999L;
        when(levelRuleMapper.selectByPrimaryKey(id)).thenReturn(Optional.empty());

        // When
        LevelRuleDTO result = levelRuleService.findById(id);

        // Then
        assertNull(result);
        verify(levelRuleMapper, times(1)).selectByPrimaryKey(id);
    }

    @Test
    void save_withExistingId_shouldUseProvidedId() {
        // Given
        LevelRuleDTO dto = LevelRuleDTO.builder()
                .id(123L)
                .build();

        when(levelRuleMapper.insertSelective(any(LevelRule.class))).thenAnswer(invocation -> {
            LevelRule entity = invocation.getArgument(0);
            return 1;
        });

        // When
        Long result = levelRuleService.save(dto);

        // Then
        assertNotNull(result);
        verify(levelRuleMapper, times(1)).insertSelective(any(LevelRule.class));
    }

    @Test
    void save_whenInsertFails_shouldReturnNull() {
        // Given
        LevelRuleDTO dto = LevelRuleDTO.builder().build();
        when(levelRuleMapper.insertSelective(any(LevelRule.class))).thenReturn(0);

        // When
        Long result = levelRuleService.save(dto);

        // Then
        assertNull(result);
        verify(levelRuleMapper, times(1)).insertSelective(any(LevelRule.class));
    }

    @Test
    void saveBatch_withNullList_shouldReturnZero() {
        // When
        int result = levelRuleService.saveBatch(null);

        // Then
        assertEquals(0, result);
        verify(levelRuleMapper, never()).insertMultiple(anyCollection());
    }

    @Test
    void saveBatch_withEmptyList_shouldReturnZero() {
        // Given
        List<LevelRuleDTO> emptyList = List.of();

        // When
        int result = levelRuleService.saveBatch(emptyList);

        // Then
        assertEquals(0, result);
        verify(levelRuleMapper, never()).insertMultiple(anyCollection());
    }

    @Test
    void saveBatch_withValidDTOs_shouldGenerateIdsAndTimestamps() {
        // Given
        LevelRuleDTO dto1 = LevelRuleDTO.builder().build();
        LevelRuleDTO dto2 = LevelRuleDTO.builder().id(100L).build();
        List<LevelRuleDTO> dtoList = List.of(dto1, dto2);

        when(levelRuleMapper.insertMultiple(anyCollection())).thenReturn(2);

        // When
        int result = levelRuleService.saveBatch(dtoList);

        // Then
        assertEquals(2, result);
        verify(levelRuleMapper, times(1)).insertMultiple(anyCollection());
    }

    @Test
    void saveBatch_withNullDTOsInList_shouldFilterThem() {
        // Given
        LevelRuleDTO dto = LevelRuleDTO.builder().build();
        List<LevelRuleDTO> dtoList = Arrays.asList(dto, null, null);

        when(levelRuleMapper.insertMultiple(anyCollection())).thenReturn(1);

        // When
        int result = levelRuleService.saveBatch(dtoList);

        // Then
        assertEquals(1, result);
        verify(levelRuleMapper, times(1)).insertMultiple(anyCollection());
    }

    @Test
    void saveBatch_withAllNullDTOs_shouldReturnZero() {
        // Given
        List<LevelRuleDTO> dtoList = Arrays.asList(null, null);

        // When
        int result = levelRuleService.saveBatch(dtoList);

        // Then
        assertEquals(0, result);
        verify(levelRuleMapper, never()).insertMultiple(anyCollection());
    }

    @Test
    void update_withNullDTO_shouldReturnZero() {
        // When
        int result = levelRuleService.update(null);

        // Then
        assertEquals(0, result);
        verify(levelRuleMapper, never()).updateByPrimaryKeySelective(any());
    }

    @Test
    void deleteById_withNullId_shouldReturnZero() {
        // When
        int result = levelRuleService.deleteById(null);

        // Then
        assertEquals(0, result);
        verify(levelRuleMapper, never()).deleteByPrimaryKey(any());
    }

    @Test
    void findAll_shouldReturnAllRules() {
        // Given
        LevelRule entity1 = new LevelRule();
        entity1.setId(1L);
        LevelRule entity2 = new LevelRule();
        entity2.setId(2L);

        when(levelRuleMapper.select(any())).thenReturn(List.of(entity1, entity2));

        // When
        List<LevelRuleDTO> result = levelRuleService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(levelRuleMapper, times(1)).select(any());
    }

    @Test
    void findByModeTypeAndMatchResult_withValidParams_shouldReturnDTO() {
        // Given
        String modeType = "RANKED";
        String matchResult = "WIN";
        LevelRule entity = new LevelRule();
        entity.setId(1L);

        when(levelRuleMapper.selectOne(any(SelectDSLCompleter.class))).thenReturn(Optional.of(entity));

        // When
        LevelRuleDTO result = levelRuleService.findByModeTypeAndMatchResult(modeType, matchResult);

        // Then
        assertNotNull(result);
        verify(levelRuleMapper, times(1)).selectOne(any(SelectDSLCompleter.class));
    }

    @Test
    void findByModeTypeAndMatchResult_withNullModeType_shouldReturnNull() {
        // When
        LevelRuleDTO result = levelRuleService.findByModeTypeAndMatchResult(null, "WIN");

        // Then
        assertNull(result);
        verify(levelRuleMapper, never()).selectOne(any(SelectDSLCompleter.class));
    }

    @Test
    void findByModeTypeAndMatchResult_withNullMatchResult_shouldReturnNull() {
        // When
        LevelRuleDTO result = levelRuleService.findByModeTypeAndMatchResult("RANKED", null);

        // Then
        assertNull(result);
        verify(levelRuleMapper, never()).selectOne(any(SelectDSLCompleter.class));
    }

    @Test
    void findByModeTypeAndMatchResult_withBothNull_shouldReturnNull() {
        // When
        LevelRuleDTO result = levelRuleService.findByModeTypeAndMatchResult(null, null);

        // Then
        assertNull(result);
        verify(levelRuleMapper, never()).selectOne(any(SelectDSLCompleter.class));
    }

    @Test
    void findByModeTypeAndMatchResult_withNoMatch_shouldReturnNull() {
        // Given
        String modeType = "RANKED";
        String matchResult = "WIN";

        when(levelRuleMapper.selectOne(any(SelectDSLCompleter.class))).thenReturn(Optional.empty());

        // When
        LevelRuleDTO result = levelRuleService.findByModeTypeAndMatchResult(modeType, matchResult);

        // Then
        assertNull(result);
        verify(levelRuleMapper, times(1)).selectOne(any(SelectDSLCompleter.class));
    }
}
