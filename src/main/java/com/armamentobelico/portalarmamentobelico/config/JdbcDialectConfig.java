package com.armamentobelico.portalarmamentobelico.config;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.jdbc.core.dialect.JdbcDialect;
import org.springframework.data.relational.core.dialect.AbstractDialect;
import org.springframework.data.relational.core.dialect.LimitClause;
import org.springframework.data.relational.core.dialect.LockClause;
import org.springframework.data.relational.core.sql.LockOptions;

@Configuration(proxyBeanMethods = false)
public class JdbcDialectConfig {

    @Bean
    public JdbcDialect jdbcDialect() {
        return new SqliteJdbcDialect();
    }

    static class SqliteJdbcDialect extends AbstractDialect implements JdbcDialect {

        private static final LimitClause LIMIT_CLAUSE = new LimitClause() {
            @Override
            public String getLimit(long limit) {
                return "LIMIT " + limit;
            }

            @Override
            public String getOffset(long offset) {
                return "LIMIT -1 OFFSET " + offset;
            }

            @Override
            public String getLimitOffset(long limit, long offset) {
                return "LIMIT " + limit + " OFFSET " + offset;
            }

            @Override
            public Position getClausePosition() {
                return Position.AFTER_ORDER_BY;
            }
        };

        private static final LockClause LOCK_CLAUSE = new LockClause() {
            @Override
            public String getLock(LockOptions lockOptions) {
                return "";
            }

            @Override
            public Position getClausePosition() {
                return Position.AFTER_ORDER_BY;
            }
        };

        @Override
        public LimitClause limit() {
            return LIMIT_CLAUSE;
        }

        @Override
        public LockClause lock() {
            return LOCK_CLAUSE;
        }
    }

    @Bean
    public JdbcCustomConversions jdbcCustomConversions() {
        return new JdbcCustomConversions(Arrays.asList(
                new UuidToStringConverter(),
                new StringToUuidConverter(),
                new IntegerToBooleanConverter(),
                new BooleanToIntegerConverter(),
                new InstantToStringConverter(),
                new StringToInstantConverter(),
                new LocalDateToStringConverter(),
                new StringToLocalDateConverter()
        ));
    }

    @WritingConverter
    static class UuidToStringConverter implements Converter<UUID, String> {
        @Override
        public String convert(UUID source) {
            return source == null ? null : source.toString();
        }
    }

    @ReadingConverter
    static class StringToUuidConverter implements Converter<String, UUID> {
        @Override
        public UUID convert(String source) {
            return source == null || source.isBlank() ? null : UUID.fromString(source);
        }
    }

    @ReadingConverter
    static class IntegerToBooleanConverter implements Converter<Integer, Boolean> {
        @Override
        public Boolean convert(Integer source) {
            return source == null ? null : source != 0;
        }
    }

    @WritingConverter
    static class BooleanToIntegerConverter implements Converter<Boolean, Integer> {
        @Override
        public Integer convert(Boolean source) {
            return source == null ? null : (source ? 1 : 0);
        }
    }

    @WritingConverter
    static class InstantToStringConverter implements Converter<Instant, String> {
        @Override
        public String convert(Instant source) {
            return source == null ? null : source.toString();
        }
    }

    @ReadingConverter
    static class StringToInstantConverter implements Converter<String, Instant> {
        @Override
        public Instant convert(String source) {
            return source == null || source.isBlank() ? null : Instant.parse(source);
        }
    }

    @WritingConverter
    static class LocalDateToStringConverter implements Converter<LocalDate, String> {
        @Override
        public String convert(LocalDate source) {
            return source == null ? null : source.toString();
        }
    }

    @ReadingConverter
    static class StringToLocalDateConverter implements Converter<String, LocalDate> {
        @Override
        public LocalDate convert(String source) {
            return source == null || source.isBlank() ? null : LocalDate.parse(source);
        }
    }
}
