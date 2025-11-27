package com.goody.nus.se.gomoku.common.basiccheck;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Basic parameter validation AOP aspect
 * Provides automatic parameter checking for methods annotated with @BasicCheck
 *
 * @author Goody
 * @version 1.0, 2023/01/30
 * @since 1.0.0
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class NotNullAndPositiveAspect {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    /** Pointcut for methods annotated with @BasicCheck */
    @Pointcut("@annotation(com.goody.nus.se.gomoku.common.basiccheck.BasicCheck)")
    private void handleBasicCheckPoint() {
    }

    /**
     * Main execution logic for parameter validation
     *
     * @param point      proceeding join point
     * @param basicCheck basic check annotation
     * @return method result or validation failure result
     * @throws Throwable exception
     */
    @Around("handleBasicCheckPoint() && @annotation(basicCheck)")
    public Object around(ProceedingJoinPoint point, BasicCheck basicCheck) throws Throwable {
        final Object[] args = point.getArgs();
        final MethodSignature signature = (MethodSignature) point.getSignature();
        final Method method = signature.getMethod();
        final Parameter[] parameters = method.getParameters();

        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].isAnnotationPresent(CheckNull.class) && null == args[i]) {
                return this.getReturnObj(basicCheck, method);
            }
            if (parameters[i].isAnnotationPresent(CheckLong.class) && (!(args[i] instanceof Long) || ((Long) args[i]) <= -1L)) {
                return this.getReturnObj(basicCheck, method);
            }
            if (parameters[i].isAnnotationPresent(CheckString.class) && (!(args[i] instanceof String) || StringUtils.isBlank((String) args[i]))) {
                return this.getReturnObj(basicCheck, method);
            }
            if (parameters[i].isAnnotationPresent(CheckCollection.class) && (!(args[i] instanceof Collection) || CollectionUtils.isEmpty((Collection) args[i]))) {
                return this.getReturnObj(basicCheck, method);
            }
            if (parameters[i].isAnnotationPresent(CheckMap.class) && (!(args[i] instanceof Map) || MapUtils.isEmpty((Map) args[i]))) {
                return this.getReturnObj(basicCheck, method);
            }
            if (parameters[i].isAnnotationPresent(CheckObject.class) && (args[i] == null || !this.valid(args[i]))) {
                return this.getReturnObj(basicCheck, method);
            }
        }
        return point.proceed();
    }

    private Object getReturnObj(BasicCheck annotation, Method method) {
        if (annotation.returnType() == BasicCheck.ReturnType.NULL) {
            log.info("#NotNullAndPositiveAspect return null with {}", method.getName());
            return null;
        }
        if (annotation.returnType() == BasicCheck.ReturnType.EMPTY) {
            Class<?> returnType = method.getReturnType();
            if (returnType == Collection.class || returnType == List.class) {
                log.info("#NotNullAndPositiveAspect return emptyList with {}", method.getName());
                return Collections.emptyList();
            }
            if (returnType == Set.class) {
                log.info("#NotNullAndPositiveAspect return emptySet with {}", method.getName());
                return Collections.emptySet();
            }
            if (returnType == Map.class) {
                log.info("#NotNullAndPositiveAspect return emptyMap with {}", method.getName());
                return Collections.emptyMap();
            }
            if (returnType == Optional.class) {
                log.info("#NotNullAndPositiveAspect return Optional.empty() with {}", method.getName());
                return Optional.empty();
            }
            if (returnType == Long.class) {
                log.info("#NotNullAndPositiveAspect return Long with {}", method.getName());
                return 0L;
            }
            if (returnType == Integer.class) {
                log.info("#NotNullAndPositiveAspect return Integer with {}", method.getName());
                return 0;
            }
            if (returnType == String.class) {
                log.info("#NotNullAndPositiveAspect return String with {}", method.getName());
                return "";
            }
            if (returnType == Boolean.class) {
                log.info("#NotNullAndPositiveAspect return Boolean with {}", method.getName());
                return false;
            }
            return null;
        }
        log.info("#NotNullAndPositiveAspect throw exception with {}", method.getName());
        throw new IllegalArgumentException();
    }

    /**
     * Parameter validation using Bean Validation
     *
     * @param o object to validate
     * @return true if valid, false if validation failed
     */
    private boolean valid(Object o) {
        return CollectionUtils.isEmpty(VALIDATOR.validate(o));
    }
}
