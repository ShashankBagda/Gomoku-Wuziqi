package com.goody.nus.se.gomoku.gomoku.service.impl;

import com.goody.nus.se.gomoku.gomoku.model.dao.GameRoomMapper;
import com.goody.nus.se.gomoku.gomoku.model.dao.customer.CustomerGameRoomMapper;
import com.goody.nus.se.gomoku.gomoku.model.dto.GameRoomDTO;
import com.goody.nus.se.gomoku.gomoku.model.entity.GameRoom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link GameRoomServiceImpl}
 *
 * @author Claude
 * @version 1.0
 */
class GameRoomServiceImplTest {

    @Mock
    private GameRoomMapper gameRoomMapper;

    @Mock
    private CustomerGameRoomMapper customerGameRoomMapper;

    @InjectMocks
    private GameRoomServiceImpl gameRoomService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void save_withValidDTO_shouldReturnId() {
        // Given
        GameRoomDTO dto = GameRoomDTO.builder()
                .roomCode("123456")
                .status((byte) 1)
                .build();

        when(gameRoomMapper.insertSelective(any(GameRoom.class))).thenAnswer(invocation -> {
            GameRoom entity = invocation.getArgument(0);
            entity.setId(999L);
            return 1;
        });

        // When
        Long result = gameRoomService.save(dto);

        // Then
        assertNotNull(result);
        verify(gameRoomMapper, times(1)).insertSelective(any(GameRoom.class));
    }

    @Test
    void save_withNullDTO_shouldReturnNull() {
        // When
        Long result = gameRoomService.save(null);

        // Then
        assertNull(result);
        verify(gameRoomMapper, never()).insertSelective(any());
    }

    @Test
    void saveBatch_withValidList_shouldInsertAll() {
        // Given
        List<GameRoomDTO> dtoList = Arrays.asList(
                GameRoomDTO.builder().roomCode("111111").build(),
                GameRoomDTO.builder().roomCode("222222").build()
        );

        when(gameRoomMapper.insertMultiple(anyList())).thenReturn(2);

        // When
        int result = gameRoomService.saveBatch(dtoList);

        // Then
        assertEquals(2, result);
        verify(gameRoomMapper, times(1)).insertMultiple(anyList());
    }

    @Test
    void update_withValidDTO_shouldReturnOne() {
        // Given
        GameRoomDTO dto = GameRoomDTO.builder()
                .id(123L)
                .roomCode("123456")
                .status((byte) 2)
                .build();

        when(gameRoomMapper.updateByPrimaryKeySelective(any(GameRoom.class))).thenReturn(1);

        // When
        int result = gameRoomService.update(dto);

        // Then
        assertEquals(1, result);
        verify(gameRoomMapper, times(1)).updateByPrimaryKeySelective(any(GameRoom.class));
    }

    @Test
    void deleteById_withValidId_shouldReturnOne() {
        // Given
        Long id = 123L;
        when(gameRoomMapper.deleteByPrimaryKey(id)).thenReturn(1);

        // When
        int result = gameRoomService.deleteById(id);

        // Then
        assertEquals(1, result);
        verify(gameRoomMapper, times(1)).deleteByPrimaryKey(id);
    }

    @Test
    void findById_withExistingId_shouldReturnDTO() {
        // Given
        Long id = 123L;
        GameRoom entity = new GameRoom();
        entity.setId(id);
        entity.setRoomCode("123456");

        when(gameRoomMapper.selectByPrimaryKey(id)).thenReturn(Optional.of(entity));

        // When
        GameRoomDTO result = gameRoomService.findById(id);

        // Then
        assertNotNull(result);
        assertEquals(id, result.getId());
        verify(gameRoomMapper, times(1)).selectByPrimaryKey(id);
    }

    @Test
    void findById_withNullId_shouldReturnNull() {
        // When
        GameRoomDTO result = gameRoomService.findById(null);

        // Then
        assertNull(result);
        verify(gameRoomMapper, never()).selectByPrimaryKey(any());
    }

    @Test
    void findById_withNonExistingId_shouldReturnNull() {
        // Given
        Long id = 999L;
        when(gameRoomMapper.selectByPrimaryKey(id)).thenReturn(Optional.empty());

        // When
        GameRoomDTO result = gameRoomService.findById(id);

        // Then
        assertNull(result);
        verify(gameRoomMapper, times(1)).selectByPrimaryKey(id);
    }

    @Test
    void save_withDTOHavingId_shouldNotGenerateNewId() {
        // Given
        Long existingId = 123L;
        GameRoomDTO dto = GameRoomDTO.builder()
                .id(existingId)
                .roomCode("123456")
                .status((byte) 1)
                .build();

        when(gameRoomMapper.insertSelective(any(GameRoom.class))).thenAnswer(invocation -> {
            GameRoom entity = invocation.getArgument(0);
            assertEquals(existingId, entity.getId());
            return 1;
        });

        // When
        Long result = gameRoomService.save(dto);

        // Then
        assertNotNull(result);
        verify(gameRoomMapper, times(1)).insertSelective(any(GameRoom.class));
    }

    @Test
    void save_insertFails_shouldReturnNull() {
        // Given
        GameRoomDTO dto = GameRoomDTO.builder()
                .roomCode("123456")
                .status((byte) 1)
                .build();

        when(gameRoomMapper.insertSelective(any(GameRoom.class))).thenReturn(0);

        // When
        Long result = gameRoomService.save(dto);

        // Then
        assertNull(result);
        verify(gameRoomMapper, times(1)).insertSelective(any(GameRoom.class));
    }

    @Test
    void saveBatch_withNullList_shouldReturnZero() {
        // When
        int result = gameRoomService.saveBatch(null);

        // Then
        assertEquals(0, result);
        verify(gameRoomMapper, never()).insertMultiple(anyCollection());
    }

    @Test
    void saveBatch_withEmptyList_shouldReturnZero() {
        // When
        int result = gameRoomService.saveBatch(Arrays.asList());

        // Then
        assertEquals(0, result);
        verify(gameRoomMapper, never()).insertMultiple(anyCollection());
    }

    @Test
    void saveBatch_withListContainingNull_shouldFilterOutNull() {
        // Given
        List<GameRoomDTO> dtoList = Arrays.asList(
                GameRoomDTO.builder().roomCode("111111").build(),
                null,
                GameRoomDTO.builder().roomCode("222222").build()
        );

        when(gameRoomMapper.insertMultiple(anyList())).thenAnswer(invocation -> {
            List<GameRoom> entities = invocation.getArgument(0);
            assertEquals(2, entities.size()); // null should be filtered out
            return 2;
        });

        // When
        int result = gameRoomService.saveBatch(dtoList);

        // Then
        assertEquals(2, result);
        verify(gameRoomMapper, times(1)).insertMultiple(anyList());
    }

    @Test
    void saveBatch_withDTOsHavingNoId_shouldGenerateIds() {
        // Given
        List<GameRoomDTO> dtoList = Arrays.asList(
                GameRoomDTO.builder().roomCode("111111").build(),
                GameRoomDTO.builder().roomCode("222222").build()
        );

        when(gameRoomMapper.insertMultiple(anyList())).thenAnswer(invocation -> {
            List<GameRoom> entities = invocation.getArgument(0);
            // Verify all entities have generated IDs
            entities.forEach(entity -> assertNotNull(entity.getId()));
            return entities.size();
        });

        // When
        int result = gameRoomService.saveBatch(dtoList);

        // Then
        assertEquals(2, result);
        verify(gameRoomMapper, times(1)).insertMultiple(anyList());
    }

    @Test
    void saveBatch_withDTOsHavingNoTimestamps_shouldSetTimestamps() {
        // Given
        List<GameRoomDTO> dtoList = Arrays.asList(
                GameRoomDTO.builder().roomCode("111111").build(),
                GameRoomDTO.builder().roomCode("222222").build()
        );

        when(gameRoomMapper.insertMultiple(anyList())).thenAnswer(invocation -> {
            List<GameRoom> entities = invocation.getArgument(0);
            // Verify all entities have timestamps set
            entities.forEach(entity -> {
                assertNotNull(entity.getCreatedAt());
                assertNotNull(entity.getUpdatedAt());
            });
            return entities.size();
        });

        // When
        int result = gameRoomService.saveBatch(dtoList);

        // Then
        assertEquals(2, result);
        verify(gameRoomMapper, times(1)).insertMultiple(anyList());
    }

    @Test
    void saveBatch_withAllNulls_shouldReturnZero() {
        // Given
        List<GameRoomDTO> dtoList = Arrays.asList(null, null, null);

        // When
        int result = gameRoomService.saveBatch(dtoList);

        // Then
        assertEquals(0, result);
        verify(gameRoomMapper, never()).insertMultiple(anyCollection());
    }

    @Test
    void update_withNullDTO_shouldReturnZero() {
        // When
        int result = gameRoomService.update(null);

        // Then
        assertEquals(0, result);
        verify(gameRoomMapper, never()).updateByPrimaryKeySelective(any());
    }

    @Test
    void deleteById_withNullId_shouldReturnZero() {
        // When
        int result = gameRoomService.deleteById(null);

        // Then
        assertEquals(0, result);
        verify(gameRoomMapper, never()).deleteByPrimaryKey(any());
    }

    @Test
    void findAll_shouldReturnAllRecords() {
        // Given
        GameRoom entity1 = new GameRoom();
        entity1.setId(1L);
        entity1.setRoomCode("111111");

        GameRoom entity2 = new GameRoom();
        entity2.setId(2L);
        entity2.setRoomCode("222222");

        when(gameRoomMapper.select(any())).thenReturn(Arrays.asList(entity1, entity2));

        // When
        List<GameRoomDTO> result = gameRoomService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(gameRoomMapper, times(1)).select(any());
    }

    @Test
    void findRoomIdByRoomCode_withValidCode_shouldReturnId() {
        // Given
        String roomCode = "ABCD1234";
        GameRoom entity = new GameRoom();
        entity.setId(123L);
        entity.setRoomCode(roomCode);

        when(gameRoomMapper.select(any())).thenReturn(Arrays.asList(entity));

        // When
        Long result = gameRoomService.findRoomIdByRoomCode(roomCode);

        // Then
        assertNotNull(result);
        assertEquals(123L, result);
        verify(gameRoomMapper, times(1)).select(any());
    }

    @Test
    void findRoomIdByRoomCode_withNullCode_shouldReturnNull() {
        // When
        Long result = gameRoomService.findRoomIdByRoomCode(null);

        // Then
        assertNull(result);
        verify(gameRoomMapper, never()).select(any());
    }

    @Test
    void findRoomIdByRoomCode_withEmptyCode_shouldReturnNull() {
        // When
        Long result = gameRoomService.findRoomIdByRoomCode("");

        // Then
        assertNull(result);
        verify(gameRoomMapper, never()).select(any());
    }

    @Test
    void findRoomIdByRoomCode_withNonExistingCode_shouldReturnNull() {
        // Given
        String roomCode = "NOTFOUND";
        when(gameRoomMapper.select(any())).thenReturn(Arrays.asList());

        // When
        Long result = gameRoomService.findRoomIdByRoomCode(roomCode);

        // Then
        assertNull(result);
        verify(gameRoomMapper, times(1)).select(any());
    }
}
