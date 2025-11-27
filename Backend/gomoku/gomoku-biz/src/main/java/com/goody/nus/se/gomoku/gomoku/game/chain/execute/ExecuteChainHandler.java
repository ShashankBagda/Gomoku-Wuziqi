package com.goody.nus.se.gomoku.gomoku.game.chain.execute;

import com.goody.nus.se.gomoku.gomoku.enums.ActionType;
import com.goody.nus.se.gomoku.gomoku.model.GameAction;
import com.goody.nus.se.gomoku.gomoku.mongo.entity.GameDocument;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * execute chain handler
 *
 * @author Haotian
 * @version 1.0, 2025/10/14
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ExecuteChainHandler {
    private final List<ExecuteChain> executeChains;

    private Map<ActionType, List<ExecuteChain>> executeChainMap = new HashMap<>();

    @PostConstruct
    public void init() {
        executeChainMap = executeChains.stream()
                .sorted(Comparator.comparingInt(ExecuteChain::sort))
                .flatMap(chain -> chain.getActionTypes().stream()
                        .map(actionType -> Map.entry(actionType, chain)))
                .collect(Collectors.groupingBy(Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
        log.info("ExecuteChainHandler initialized with chains: {}", executeChainMap);
    }

    /**
     * handle the action execution
     *
     * @param game   game document
     * @param action action to execute
     */
    public void handle(GameDocument game, GameAction action) {
        List<ExecuteChain> chains = executeChainMap.get(action.getType());
        if (CollectionUtils.isEmpty(chains)) {
            log.warn("No execution chains found for action type: {}", action);
            return;
        }
        for (ExecuteChain chain : chains) {
            log.debug("Executing chain: {} for action type: {}", chain.getClass().getSimpleName(), action);
            final boolean hit = chain.check(game, action);
            if (hit) {
                log.debug("Chain {} matched for action type: {}, executing...", chain.getClass().getSimpleName(), action);
                chain.execute(game, action);
                // Update game document
                game.setVersion(game.getVersion() + 1);
                game.setLastAction(action);
                game.addActionToHistory(action);
                game.setUpdateTime(System.currentTimeMillis());
                return;
            }
        }
    }
}
