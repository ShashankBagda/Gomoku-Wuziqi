package com.goody.nus.se.gomoku.common.basiccheck;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Example service for testing {@link BasicCheck} functionality
 *
 * @author Goody
 * @version 1.0, 2023/01/30
 * @since 1.0.0
 */
@Service
public class TargetNotNullAndPositiveService {

    @BasicCheck
    public void alwaysTrue(Long id, String name, List<Long> list, Map<Long, Long> map, Set<Long> set) {
    }

    @BasicCheck
    public void checkLongOnly(@CheckLong Long id, String name, List<Long> list, Map<Long, Long> map, Set<Long> set) {
    }

    @BasicCheck
    public void checkStringOnly(Long id, @CheckString String name, List<Long> list, Map<Long, Long> map, Set<Long> set) {
    }

    @BasicCheck
    public void checkNullOnly(@CheckNull Long id, @CheckNull String name, @CheckNull List<Long> list, @CheckNull Map<Long, Long> map, @CheckNull Set<Long> set) {
    }

    @BasicCheck
    public void checkCollectionAndThrowException(Long id, String name, @CheckCollection List<Long> list, @CheckMap Map<Long, Long> map, @CheckCollection Set<Long> set) {
    }

    @BasicCheck(returnType = BasicCheck.ReturnType.EMPTY)
    public List<Long> checkCollectionAndReturnList(Long id, String name, @CheckCollection List<Long> list, @CheckMap Map<Long, Long> map, @CheckCollection Set<Long> set) {
        return null;
    }

    @BasicCheck(returnType = BasicCheck.ReturnType.EMPTY)
    public Map<Long, Long> checkCollectionAndReturnMap(Long id, String name, @CheckCollection List<Long> list, @CheckMap Map<Long, Long> map, @CheckCollection Set<Long> set) {
        return null;
    }

    @BasicCheck(returnType = BasicCheck.ReturnType.EMPTY)
    public Set<Long> checkCollectionAndReturnSet(Long id, String name, @CheckCollection List<Long> list, @CheckMap Map<Long, Long> map, @CheckCollection Set<Long> set) {
        return null;
    }

    @BasicCheck(returnType = BasicCheck.ReturnType.EMPTY)
    public Optional checkCollectionAndReturnOptional(Long id, String name, @CheckCollection List<Long> list, @CheckMap Map<Long, Long> map, @CheckCollection Set<Long> set) {
        return null;
    }

    @BasicCheck
    public void checkAll(@CheckLong Long id, @CheckString String name, @CheckCollection List<Long> list, @CheckMap Map<Long, Long> map, @CheckCollection Set<Long> set) {
    }

    @BasicCheck(returnType = BasicCheck.ReturnType.NULL)
    public void checkAllReturnNull(@CheckLong Long id, @CheckString String name, @CheckCollection List<Long> list, @CheckMap Map<Long, Long> map, @CheckCollection Set<Long> set) {
    }

    @BasicCheck
    public void checkObject(@CheckObject ValidDTO dto) {
    }
}
