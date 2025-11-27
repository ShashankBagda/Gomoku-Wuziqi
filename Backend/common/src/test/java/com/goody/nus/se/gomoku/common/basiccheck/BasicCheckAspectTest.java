package com.goody.nus.se.gomoku.common.basiccheck;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link NotNullAndPositiveAspect} test
 * Disabled due to startup class configuration
 *
 * @author Goody
 * @version 1.0, 2023/01/30
 * @since 1.0.0
 */
@Disabled
@SpringBootTest(classes = TestRestApplication.class)
class BasicCheckAspectTest {
    private static final Long LONG = 1L;
    private static final String STRING = "1";
    private static final List<Long> LIST = singletonList(1L);
    private static final Map<Long, Long> MAP = singletonMap(1L, 1L);
    private static final Set<Long> SET = singleton(1L);
    @Autowired
    private TargetNotNullAndPositiveService targetService;

    @Test
    void check_alwaysTrue() {
        assertDoesNotThrow(() -> targetService.alwaysTrue(null, null, null, null, null));
        assertDoesNotThrow(() -> targetService.alwaysTrue(LONG, STRING, LIST, MAP, SET));
    }

    @Test
    void check_checkLongOnly() {
        assertThrows(IllegalArgumentException.class, () -> targetService.checkLongOnly(null, STRING, LIST, MAP, SET));
        assertThrows(IllegalArgumentException.class, () -> targetService.checkLongOnly(null, null, null, null, null));
        assertThrows(IllegalArgumentException.class, () -> targetService.checkLongOnly(-1L, STRING, LIST, MAP, SET));

        assertDoesNotThrow(() -> targetService.checkLongOnly(0L, null, null, null, null));
        assertDoesNotThrow(() -> targetService.checkLongOnly(LONG, null, null, null, null));
    }

    @Test
    void check_checkStringOnly() {
        assertThrows(IllegalArgumentException.class, () -> targetService.checkStringOnly(LONG, "", LIST, MAP, SET));
        assertThrows(IllegalArgumentException.class, () -> targetService.checkStringOnly(LONG, null, LIST, MAP, SET));
        assertDoesNotThrow(() -> targetService.checkStringOnly(0L, STRING, null, null, null));
        assertDoesNotThrow(() -> targetService.checkStringOnly(LONG, STRING, null, null, null));
        assertDoesNotThrow(() -> targetService.checkStringOnly(null, STRING, LIST, MAP, SET));
        assertDoesNotThrow(() -> targetService.checkStringOnly(null, STRING, null, null, null));
    }

    @Test
    void check_checkNullOnly() {
        assertThrows(IllegalArgumentException.class, () -> targetService.checkNullOnly(null, null, null, null, null));
        assertThrows(IllegalArgumentException.class, () -> targetService.checkNullOnly(null, STRING, LIST, MAP, SET));
        assertThrows(IllegalArgumentException.class, () -> targetService.checkNullOnly(LONG, STRING, null, MAP, SET));
        assertDoesNotThrow(() -> targetService.checkLongOnly(0L, STRING, LIST, MAP, SET));
        assertDoesNotThrow(() -> targetService.checkLongOnly(LONG, STRING, LIST, MAP, SET));
    }

    @Test
    void check_checkCollectionAndThrowException() {
        assertThrows(IllegalArgumentException.class, () -> targetService.checkCollectionAndThrowException(LONG, STRING, null, MAP, SET));
        assertThrows(IllegalArgumentException.class, () -> targetService.checkCollectionAndThrowException(LONG, STRING, LIST, null, SET));
        assertThrows(IllegalArgumentException.class, () -> targetService.checkCollectionAndThrowException(LONG, STRING, LIST, MAP, null));
        assertThrows(IllegalArgumentException.class, () -> targetService.checkCollectionAndThrowException(LONG, STRING, emptyList(), MAP, SET));
        assertThrows(IllegalArgumentException.class, () -> targetService.checkCollectionAndThrowException(LONG, STRING, LIST, emptyMap(), SET));
        assertThrows(IllegalArgumentException.class, () -> targetService.checkCollectionAndThrowException(LONG, STRING, LIST, MAP, emptySet()));
        assertDoesNotThrow(() -> targetService.checkCollectionAndThrowException(null, null, LIST, MAP, SET));
    }

    @Test
    void check_checkCollectionAndReturnList() {
        assertNotNull(targetService.checkCollectionAndReturnList(LONG, STRING, emptyList(), MAP, SET));
        assertNotNull(targetService.checkCollectionAndReturnList(LONG, STRING, LIST, emptyMap(), SET));
        assertNotNull(targetService.checkCollectionAndReturnList(LONG, STRING, LIST, MAP, emptySet()));
        assertNotNull(targetService.checkCollectionAndReturnList(LONG, STRING, null, MAP, SET));
        assertNotNull(targetService.checkCollectionAndReturnList(LONG, STRING, LIST, null, SET));
        assertNotNull(targetService.checkCollectionAndReturnList(LONG, STRING, LIST, MAP, null));
        assertNotNull(targetService.checkCollectionAndReturnList(null, null, null, null, null));
        // Annotation returns empty data, method implementation returns null
        assertNull(targetService.checkCollectionAndReturnList(null, null, LIST, MAP, SET));
    }

    @Test
    void check_checkCollectionAndReturnMap() {
        assertNotNull(targetService.checkCollectionAndReturnMap(LONG, STRING, emptyList(), MAP, SET));
        assertNotNull(targetService.checkCollectionAndReturnMap(LONG, STRING, LIST, emptyMap(), SET));
        assertNotNull(targetService.checkCollectionAndReturnMap(LONG, STRING, LIST, MAP, emptySet()));
        assertNotNull(targetService.checkCollectionAndReturnMap(LONG, STRING, null, MAP, SET));
        assertNotNull(targetService.checkCollectionAndReturnMap(LONG, STRING, LIST, null, SET));
        assertNotNull(targetService.checkCollectionAndReturnMap(LONG, STRING, LIST, MAP, null));
        assertNotNull(targetService.checkCollectionAndReturnMap(null, null, null, null, null));
        // Annotation returns empty data, method implementation returns null
        assertNull(targetService.checkCollectionAndReturnMap(null, null, LIST, MAP, SET));
    }

    @Test
    void check_checkCollectionAndReturnSet() {
        assertNotNull(targetService.checkCollectionAndReturnSet(LONG, STRING, emptyList(), MAP, SET));
        assertNotNull(targetService.checkCollectionAndReturnSet(LONG, STRING, LIST, emptyMap(), SET));
        assertNotNull(targetService.checkCollectionAndReturnSet(LONG, STRING, LIST, MAP, emptySet()));
        assertNotNull(targetService.checkCollectionAndReturnSet(LONG, STRING, null, MAP, SET));
        assertNotNull(targetService.checkCollectionAndReturnSet(LONG, STRING, LIST, null, SET));
        assertNotNull(targetService.checkCollectionAndReturnSet(LONG, STRING, LIST, MAP, null));
        assertNotNull(targetService.checkCollectionAndReturnSet(null, null, null, null, null));
        // Annotation returns empty data, method implementation returns null
        assertNull(targetService.checkCollectionAndReturnSet(null, null, LIST, MAP, SET));
    }

    @Test
    void check_checkCollectionAndReturnOptional() {
        assertEquals(Optional.empty(), targetService.checkCollectionAndReturnOptional(LONG, STRING, emptyList(), MAP, SET));
        assertEquals(Optional.empty(), targetService.checkCollectionAndReturnOptional(LONG, STRING, LIST, emptyMap(), SET));
        assertEquals(Optional.empty(), targetService.checkCollectionAndReturnOptional(LONG, STRING, LIST, MAP, emptySet()));
        assertEquals(Optional.empty(), targetService.checkCollectionAndReturnOptional(LONG, STRING, null, MAP, SET));
        assertEquals(Optional.empty(), targetService.checkCollectionAndReturnOptional(LONG, STRING, LIST, null, SET));
        assertEquals(Optional.empty(), targetService.checkCollectionAndReturnOptional(LONG, STRING, LIST, MAP, null));
        assertEquals(Optional.empty(), targetService.checkCollectionAndReturnOptional(null, null, null, null, null));
        // Annotation returns empty data, method implementation returns null
        assertNull(targetService.checkCollectionAndReturnOptional(null, null, LIST, MAP, SET));
    }

    @Test
    void check_allAndThrowException() {
        assertThrows(IllegalArgumentException.class, () -> targetService.checkAll(null, null, null, null, null));
        assertThrows(IllegalArgumentException.class, () -> targetService.checkAll(-1L, STRING, LIST, MAP, SET));
        assertThrows(IllegalArgumentException.class, () -> targetService.checkAll(LONG, STRING, LIST, emptyMap(), SET));
        assertThrows(IllegalArgumentException.class, () -> targetService.checkAll(LONG, STRING, LIST, MAP, emptySet()));
        assertThrows(IllegalArgumentException.class, () -> targetService.checkAll(LONG, STRING, null, MAP, SET));
        assertThrows(IllegalArgumentException.class, () -> targetService.checkAll(LONG, STRING, LIST, null, SET));
        assertThrows(IllegalArgumentException.class, () -> targetService.checkAll(LONG, STRING, LIST, MAP, null));
        assertThrows(IllegalArgumentException.class, () -> targetService.checkAll(LONG, "", LIST, MAP, SET));
        assertThrows(IllegalArgumentException.class, () -> targetService.checkAll(LONG, null, LIST, MAP, SET));

        assertDoesNotThrow(() -> targetService.checkAll(LONG, STRING, LIST, MAP, SET));
    }

    @Test
    void check_allAndReturnNull() {
        assertDoesNotThrow(() -> targetService.checkAllReturnNull(null, null, null, null, null));
        assertDoesNotThrow(() -> targetService.checkAllReturnNull(-1L, STRING, LIST, MAP, SET));
        assertDoesNotThrow(() -> targetService.checkAllReturnNull(LONG, STRING, LIST, emptyMap(), SET));
        assertDoesNotThrow(() -> targetService.checkAllReturnNull(LONG, STRING, LIST, MAP, emptySet()));
        assertDoesNotThrow(() -> targetService.checkAllReturnNull(LONG, STRING, null, MAP, SET));
        assertDoesNotThrow(() -> targetService.checkAllReturnNull(LONG, STRING, LIST, null, SET));
        assertDoesNotThrow(() -> targetService.checkAllReturnNull(LONG, STRING, LIST, MAP, null));
        assertDoesNotThrow(() -> targetService.checkAllReturnNull(LONG, "", LIST, MAP, SET));
        assertDoesNotThrow(() -> targetService.checkAllReturnNull(LONG, null, LIST, MAP, SET));
        assertDoesNotThrow(() -> targetService.checkAllReturnNull(LONG, STRING, LIST, MAP, SET));
    }

    @Test
    void check_Object() {
        assertThrows(IllegalArgumentException.class, () -> targetService.checkObject(null));
        assertThrows(IllegalArgumentException.class, () -> targetService.checkObject(ValidDTO.builder().build()));
        assertThrows(IllegalArgumentException.class, () -> targetService.checkObject(ValidDTO.builder()
                .id(-1)
                .build()));
        assertThrows(IllegalArgumentException.class, () -> targetService.checkObject(ValidDTO.builder()
                .id(-1)
                .name("")
                .build()));
        assertThrows(IllegalArgumentException.class, () -> targetService.checkObject(ValidDTO.builder()
                .id(-1)
                .name("a")
                .build()));
        assertThrows(IllegalArgumentException.class, () -> targetService.checkObject(ValidDTO.builder()
                .id(-1)
                .name("a")
                .list(Collections.emptyList())
                .build()));
        assertThrows(IllegalArgumentException.class, () -> targetService.checkObject(ValidDTO.builder()
                .id(-1)
                .name("a")
                .list(Collections.singletonList(null))
                .build()));
        assertThrows(IllegalArgumentException.class, () -> targetService.checkObject(ValidDTO.builder()
                .id(-1)
                .name("a")
                .list(singletonList(ValidDTO.ValidDTO1.builder().build()))
                .build()));

        assertDoesNotThrow(() -> targetService.checkObject(ValidDTO.builder()
                .id(-1)
                .name("a")
                .list(singletonList(ValidDTO.ValidDTO1.builder()
                        .age(1)
                        .build()))
                .build()));
    }
}
