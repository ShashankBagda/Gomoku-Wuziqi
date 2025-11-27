package com.goody.nus.se.gomoku.web.base.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializerBase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

/**
 * spring mvc json long to string serialize config
 *
 * @author Goody
 * @version 1.0, 2021/1/13 10:42
 * @since 2.0.0
 */
@Configuration
public class JsonCustomSerializeConfig {

    @Bean
    public Module longToStringModule() {
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
        simpleModule.addSerializer(BigDecimal.class, BigDecimalToStringSerializer.instance);
        return simpleModule;
    }

    /**
     * BigDecimal to String
     */
    public static class BigDecimalToStringSerializer extends ToStringSerializerBase {
        public final static BigDecimalToStringSerializer instance = new BigDecimalToStringSerializer();

        public BigDecimalToStringSerializer() {
            super(BigDecimal.class);
        }

        @Override
        public String valueToString(Object value) {
            return ((BigDecimal) value).stripTrailingZeros().toPlainString();
        }
    }
}
