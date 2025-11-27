package com.goody.nus.se.gomoku.common.basiccheck;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Basic parameter validation annotation with configurable return types
 * <ol>
 *     <li>EMPTY will automatically choose based on method return type</li>
 *     <li>NULL will return null, suitable for void methods</li>
 *     <li>EXCEPTION (default) will throw an exception</li>
 * </ol>
 *
 * @author Goody
 * @version 1.0, 2023/01/30
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BasicCheck {
    /** Return type when validation fails, defaults to throwing exception */
    ReturnType returnType() default ReturnType.EMPTY;

    /**
     * Return type options for validation failure
     */
    enum ReturnType {
        /** Return empty collection/map/optional */
        EMPTY,
        /** Return null */
        NULL,
        /** Throw exception */
        EXCEPTION
    }
}
