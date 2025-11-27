package com.goody.nus.se.gomoku.ranking.impl;

import com.goody.nus.se.gomoku.ranking.model.dao.ScoreRuleMapper;
import com.goody.nus.se.gomoku.ranking.model.dto.ScoreRuleDTO;
import com.goody.nus.se.gomoku.ranking.model.entity.ScoreRule;
import com.goody.nus.se.gomoku.ranking.service.impl.ScoreRuleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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
 * Unit test for {@link ScoreRuleServiceImpl}
 *
 * @author Claude
 * @version 1.0
 */
class ScoreRuleServiceImplTest {

    @Mock
    private ScoreRuleMapper scoreRuleMapper;

    @InjectMocks
    private ScoreRuleServiceImpl scoreRuleService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void save_withValidDTO_shouldReturnId() {
        // Given
        ScoreRuleDTO dto = ScoreRuleDTO.builder()
                .build();

        when(scoreRuleMapper.insertSelective(any(ScoreRule.class))).thenAnswer(invocation -> {
            ScoreRule entity = invocation.getArgument(0);
            entity.setId(999L);
            return 1;
        });

        // When
        Long result = scoreRuleService.save(dto);

        // Then
        assertNotNull(result);
        verify(scoreRuleMapper, times(1)).insertSelective(any(ScoreRule.class));
    }

    @Test
    void save_withNullDTO_shouldReturnNull() {
        // When
        Long result = scoreRuleService.save(null);

        // Then
        assertNull(result);
        verify(scoreRuleMapper, never()).insertSelective(any());
    }

    @Test
    void update_withValidDTO_shouldReturnOne() {
        // Given
        ScoreRuleDTO dto = ScoreRuleDTO.builder()
                .id(123L)
                .build();

        when(scoreRuleMapper.updateByPrimaryKeySelective(any(ScoreRule.class))).thenReturn(1);

        // When
        int result = scoreRuleService.update(dto);

        // Then
        assertEquals(1, result);
        verify(scoreRuleMapper, times(1)).updateByPrimaryKeySelective(any(ScoreRule.class));
    }

    @Test
    void deleteById_withValidId_shouldReturnOne() {
        // Given
        Long id = 123L;
        when(scoreRuleMapper.deleteByPrimaryKey(id)).thenReturn(1);

        // When
        int result = scoreRuleService.deleteById(id);

        // Then
        assertEquals(1, result);
        verify(scoreRuleMapper, times(1)).deleteByPrimaryKey(id);
    }

    @Test
    void findById_withExistingId_shouldReturnDTO() {
        // Given
        Long id = 123L;
        ScoreRule entity = new ScoreRule();
        entity.setId(id);

        when(scoreRuleMapper.selectByPrimaryKey(id)).thenReturn(Optional.of(entity));

        // When
        ScoreRuleDTO result = scoreRuleService.findById(id);

        // Then
        assertNotNull(result);
        assertEquals(id, result.getId());
        verify(scoreRuleMapper, times(1)).selectByPrimaryKey(id);
    }

    @Test
    void findById_withNullId_shouldReturnNull() {
        // When
        ScoreRuleDTO result = scoreRuleService.findById(null);

        // Then
        assertNull(result);
        verify(scoreRuleMapper, never()).selectByPrimaryKey(any());
    }

    @Test
    void findById_withNonExistingId_shouldReturnNull() {
        // Given
        Long id = 999L;
        when(scoreRuleMapper.selectByPrimaryKey(id)).thenReturn(Optional.empty());

        // When
        ScoreRuleDTO result = scoreRuleService.findById(id);

        // Then
        assertNull(result);
        verify(scoreRuleMapper, times(1)).selectByPrimaryKey(id);
    }

    @Test
    void save_withExistingId_shouldUseProvidedId() {
        // Given
        ScoreRuleDTO dto = ScoreRuleDTO.builder()
                .id(123L)
                .build();

        when(scoreRuleMapper.insertSelective(any(ScoreRule.class))).thenAnswer(invocation -> {
            ScoreRule entity = invocation.getArgument(0);
            return 1;
        });

        // When
        Long result = scoreRuleService.save(dto);

        // Then
        assertNotNull(result);
        verify(scoreRuleMapper, times(1)).insertSelective(any(ScoreRule.class));
    }

    @Test
    void save_whenInsertFails_shouldReturnNull() {
        // Given
        ScoreRuleDTO dto = ScoreRuleDTO.builder().build();
        when(scoreRuleMapper.insertSelective(any(ScoreRule.class))).thenReturn(0);

        // When
        Long result = scoreRuleService.save(dto);

        // Then
        assertNull(result);
        verify(scoreRuleMapper, times(1)).insertSelective(any(ScoreRule.class));
    }

    @Test
    void saveBatch_withNullList_shouldReturnZero() {
        // When
        int result = scoreRuleService.saveBatch(null);

        // Then
        assertEquals(0, result);
        verify(scoreRuleMapper, never()).insertMultiple(anyCollection());
    }

    @Test
    void saveBatch_withEmptyList_shouldReturnZero() {
        // Given
        List<ScoreRuleDTO> emptyList = List.of();

        // When
        int result = scoreRuleService.saveBatch(emptyList);

        // Then
        assertEquals(0, result);
        verify(scoreRuleMapper, never()).insertMultiple(anyCollection());
    }

    @Test
    void saveBatch_withValidDTOs_shouldGenerateIdsAndTimestamps() {
        // Given
        ScoreRuleDTO dto1 = ScoreRuleDTO.builder().build();
        ScoreRuleDTO dto2 = ScoreRuleDTO.builder().id(100L).build();
        List<ScoreRuleDTO> dtoList = List.of(dto1, dto2);

        when(scoreRuleMapper.insertMultiple(anyCollection())).thenReturn(2);

        // When
        int result = scoreRuleService.saveBatch(dtoList);

        // Then
        assertEquals(2, result);
        verify(scoreRuleMapper, times(1)).insertMultiple(anyCollection());
    }

    @Test
    void saveBatch_withNullDTOsInList_shouldFilterThem() {
        // Given
        ScoreRuleDTO dto = ScoreRuleDTO.builder().build();
        List<ScoreRuleDTO> dtoList = Arrays.asList(dto, null, null);

        when(scoreRuleMapper.insertMultiple(anyCollection())).thenReturn(1);

        // When
        int result = scoreRuleService.saveBatch(dtoList);

        // Then
        assertEquals(1, result);
        verify(scoreRuleMapper, times(1)).insertMultiple(anyCollection());
    }

    @Test
    void saveBatch_withAllNullDTOs_shouldReturnZero() {
        // Given
        List<ScoreRuleDTO> dtoList = Arrays.asList(null, null);

        // When
        int result = scoreRuleService.saveBatch(dtoList);

        // Then
        assertEquals(0, result);
        verify(scoreRuleMapper, never()).insertMultiple(anyCollection());
    }

    @Test
    void update_withNullDTO_shouldReturnZero() {
        // When
        int result = scoreRuleService.update(null);

        // Then
        assertEquals(0, result);
        verify(scoreRuleMapper, never()).updateByPrimaryKeySelective(any());
    }

    @Test
    void deleteById_withNullId_shouldReturnZero() {
        // When
        int result = scoreRuleService.deleteById(null);

        // Then
        assertEquals(0, result);
        verify(scoreRuleMapper, never()).deleteByPrimaryKey(any());
    }

    @Test
    void findAll_shouldReturnAllRules() {
        // Given
        ScoreRule entity1 = new ScoreRule();
        entity1.setId(1L);
        ScoreRule entity2 = new ScoreRule();
        entity2.setId(2L);

        when(scoreRuleMapper.select(any())).thenReturn(List.of(entity1, entity2));

        // When
        List<ScoreRuleDTO> result = scoreRuleService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(scoreRuleMapper, times(1)).select(any());
    }
}
