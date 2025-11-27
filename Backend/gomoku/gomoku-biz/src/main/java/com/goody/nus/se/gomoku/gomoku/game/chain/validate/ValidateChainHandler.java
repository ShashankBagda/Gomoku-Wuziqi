package com.goody.nus.se.gomoku.gomoku.game.chain.validate;

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
 * validate chain handler
 *
 * @author Haotian
 * @version 1.0, 2025/10/14
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ValidateChainHandler {
    private final List<ValidateChain> validateChains;

    private Map<ActionType, List<ValidateChain>> validateChainMap = new HashMap<>();

    @PostConstruct
    public void init() {
        validateChainMap = validateChains.stream()
                .sorted(Comparator.comparingInt(ValidateChain::sort))
                .flatMap(chain -> chain.getActionTypes().stream()
                        .map(actionType -> Map.entry(actionType, chain)))
                .collect(Collectors.groupingBy(Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
        log.info("ValidateChainHandler initialized with chains: {}", validateChainMap);
    }

    /**
     * handle the action validation
     *
     * @param gameDocument game document
     * @param action       action to validate
     * @return true if valid, false otherwise
     */
    public boolean handle(GameDocument gameDocument, GameAction action) {
        List<ValidateChain> chains = validateChainMap.get(action.getType());
        if (CollectionUtils.isEmpty(chains)) {
            log.warn("No validation chains found for action type: {}", action);
            return true;
        }
        for (ValidateChain chain : chains) {
            log.debug("Executing validation chain: {} for action type: {}", chain.getClass().getSimpleName(), action);
            final boolean isStatus = chain.validateStatus() == gameDocument.getStatus();
            if (!isStatus) {
                log.warn("Game status {} does not match required status {} for validation chain: {} and action type: {}",
                        gameDocument.getStatus(), chain.validateStatus(), chain.getClass().getSimpleName(), action);
                return false;
            }
            final boolean validate = chain.validate(gameDocument, action);
            if (!validate) {
                log.warn("Fail executing validation chain: {} for action type: {}", chain.getClass().getSimpleName(), action);
                return false;
            }
        }
        return true;
    }
}
