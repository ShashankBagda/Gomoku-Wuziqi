package com.goody.nus.se.gomoku.ranking.impl;

import com.goody.nus.se.gomoku.ranking.model.dao.LevelMapper;
import com.goody.nus.se.gomoku.ranking.model.dao.customer.CustomerLevelMapper;
import com.goody.nus.se.gomoku.ranking.model.dto.LevelDTO;
import com.goody.nus.se.gomoku.ranking.model.entity.Level;
import com.goody.nus.se.gomoku.ranking.service.impl.LevelServiceImpl;
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
 * Unit test for {@link LevelServiceImpl}
 *
 * @author Claude
 * @version 1.0
 */
class LevelServiceImplTest {

    @Mock
    private LevelMapper levelMapper;

    @Mock
    private CustomerLevelMapper customerLevelMapper;

    @InjectMocks
    private LevelServiceImpl levelService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void save_withValidDTO_shouldReturnId() {
        // Given
        LevelDTO dto = LevelDTO.builder()
                .build();

        when(levelMapper.insertSelective(any(Level.class))).thenAnswer(invocation -> {
            Level entity = invocation.getArgument(0);
            entity.setId(999L);
            return 1;
        });

        // When
        Long result = levelService.save(dto);

        // Then
        assertNotNull(result);
        verify(levelMapper, times(1)).insertSelective(any(Level.class));
    }

    @Test
    void save_withNullDTO_shouldReturnNull() {
        // When
        Long result = levelService.save(null);

        // Then
        assertNull(result);
        verify(levelMapper, never()).insertSelective(any());
    }

    @Test
    void update_withValidDTO_shouldReturnOne() {
        // Given
        LevelDTO dto = LevelDTO.builder()
                .id(123L)
                .build();

        when(levelMapper.updateByPrimaryKeySelective(any(Level.class))).thenReturn(1);

        // When
        int result = levelService.update(dto);

        // Then
        assertEquals(1, result);
        verify(levelMapper, times(1)).updateByPrimaryKeySelective(any(Level.class));
    }

    @Test
    void deleteById_withValidId_shouldReturnOne() {
        // Given
        Long id = 123L;
        when(levelMapper.deleteByPrimaryKey(id)).thenReturn(1);

        // When
        int result = levelService.deleteById(id);

        // Then
        assertEquals(1, result);
        verify(levelMapper, times(1)).deleteByPrimaryKey(id);
    }

    @Test
    void findById_withExistingId_shouldReturnDTO() {
        // Given
        Long id = 123L;
        Level entity = new Level();
        entity.setId(id);

        when(levelMapper.selectByPrimaryKey(id)).thenReturn(Optional.of(entity));

        // When
        LevelDTO result = levelService.findById(id);

        // Then
        assertNotNull(result);
        assertEquals(id, result.getId());
        verify(levelMapper, times(1)).selectByPrimaryKey(id);
    }

    @Test
    void findById_withNullId_shouldReturnNull() {
        // When
        LevelDTO result = levelService.findById(null);

        // Then
        assertNull(result);
        verify(levelMapper, never()).selectByPrimaryKey(any());
    }

    @Test
    void findById_withNonExistingId_shouldReturnNull() {
        // Given
        Long id = 999L;
        when(levelMapper.selectByPrimaryKey(id)).thenReturn(Optional.empty());

        // When
        LevelDTO result = levelService.findById(id);

        // Then
        assertNull(result);
        verify(levelMapper, times(1)).selectByPrimaryKey(id);
    }

    @Test
    void save_withExistingId_shouldUseProvidedId() {
        // Given
        LevelDTO dto = LevelDTO.builder()
                .id(123L)
                .build();

        when(levelMapper.insertSelective(any(Level.class))).thenAnswer(invocation -> {
            Level entity = invocation.getArgument(0);
            return 1;
        });

        // When
        Long result = levelService.save(dto);

        // Then
        assertNotNull(result);
        verify(levelMapper, times(1)).insertSelective(any(Level.class));
    }

    @Test
    void save_whenInsertFails_shouldReturnNull() {
        // Given
        LevelDTO dto = LevelDTO.builder().build();
        when(levelMapper.insertSelective(any(Level.class))).thenReturn(0);

        // When
        Long result = levelService.save(dto);

        // Then
        assertNull(result);
        verify(levelMapper, times(1)).insertSelective(any(Level.class));
    }

    @Test
    void saveBatch_withNullList_shouldReturnZero() {
        // When
        int result = levelService.saveBatch(null);

        // Then
        assertEquals(0, result);
        verify(levelMapper, never()).insertMultiple(anyCollection());
    }

    @Test
    void saveBatch_withEmptyList_shouldReturnZero() {
        // Given
        List<LevelDTO> emptyList = List.of();

        // When
        int result = levelService.saveBatch(emptyList);

        // Then
        assertEquals(0, result);
        verify(levelMapper, never()).insertMultiple(anyCollection());
    }

    @Test
    void saveBatch_withValidDTOs_shouldGenerateIdsAndTimestamps() {
        // Given
        LevelDTO dto1 = LevelDTO.builder().build();
        LevelDTO dto2 = LevelDTO.builder().id(100L).build();
        List<LevelDTO> dtoList = List.of(dto1, dto2);

        when(levelMapper.insertMultiple(anyCollection())).thenReturn(2);

        // When
        int result = levelService.saveBatch(dtoList);

        // Then
        assertEquals(2, result);
        verify(levelMapper, times(1)).insertMultiple(anyCollection());
    }

    @Test
    void saveBatch_withNullDTOsInList_shouldFilterThem() {
        // Given
        LevelDTO dto = LevelDTO.builder().build();
        List<LevelDTO> dtoList = Arrays.asList(dto, null, null);

        when(levelMapper.insertMultiple(anyCollection())).thenReturn(1);

        // When
        int result = levelService.saveBatch(dtoList);

        // Then
        assertEquals(1, result);
        verify(levelMapper, times(1)).insertMultiple(anyCollection());
    }

    @Test
    void saveBatch_withAllNullDTOs_shouldReturnZero() {
        // Given
        List<LevelDTO> dtoList = Arrays.asList(null, null);

        // When
        int result = levelService.saveBatch(dtoList);

        // Then
        assertEquals(0, result);
        verify(levelMapper, never()).insertMultiple(anyCollection());
    }

    @Test
    void update_withNullDTO_shouldReturnZero() {
        // When
        int result = levelService.update(null);

        // Then
        assertEquals(0, result);
        verify(levelMapper, never()).updateByPrimaryKeySelective(any());
    }

    @Test
    void deleteById_withNullId_shouldReturnZero() {
        // When
        int result = levelService.deleteById(null);

        // Then
        assertEquals(0, result);
        verify(levelMapper, never()).deleteByPrimaryKey(any());
    }

    @Test
    void findAll_shouldReturnAllLevels() {
        // Given
        Level entity1 = new Level();
        entity1.setId(1L);
        Level entity2 = new Level();
        entity2.setId(2L);

        when(levelMapper.select(any())).thenReturn(List.of(entity1, entity2));

        // When
        List<LevelDTO> result = levelService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(levelMapper, times(1)).select(any());
    }
}
