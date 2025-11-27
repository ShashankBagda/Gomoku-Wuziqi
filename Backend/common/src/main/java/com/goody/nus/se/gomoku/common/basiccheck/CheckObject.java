package com.goody.nus.se.gomoku.common.basiccheck;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates object using Bean Validation (JSR-303) annotations
 *
 * @author yuzehui
 * @version 1.0, 2023/04/12
 * @since 1.0.0
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CheckObject {
}
