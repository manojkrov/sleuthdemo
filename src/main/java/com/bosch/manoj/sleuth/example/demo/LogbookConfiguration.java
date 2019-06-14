package com.bosch.manoj.sleuth.example.demo;

import com.bosch.manoj.sleuth.example.demo.support.HttpLogger;
import com.bosch.manoj.sleuth.example.demo.support.JsonHttpLogger;
import com.bosch.manoj.sleuth.example.demo.support.KeyValueHttpLogger;
import com.bosch.manoj.sleuth.example.demo.support.LogbookBodyFilterProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.logbook.DefaultHttpLogWriter;
import org.zalando.logbook.spring.LogbookProperties;

@Configuration
public class LogbookConfiguration {

    @Bean
    @Qualifier("logbook")
    public ObjectMapper logbookObjectMapper() {
        return new ObjectMapper()
                .registerModules(new Jdk8Module(), new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Bean
    @ConditionalOnProperty(name = "logbook.format.style", havingValue = "json")
    public HttpLogger jsonHttpLogSupport(final LogbookProperties properties) {
        LogbookProperties.Write write = properties.getWrite();
        Logger logger = LoggerFactory.getLogger(write.getCategory());
        DefaultHttpLogWriter.Level level = write.getLevel();
        return new JsonHttpLogger(logger, level, logbookObjectMapper());
    }

    @Bean
    @ConditionalOnProperty(name = "logbook.format.style", havingValue = "kv", matchIfMissing = true)
    public HttpLogger kvHttpLogSupport(final LogbookProperties properties) {
        LogbookProperties.Write write = properties.getWrite();
        Logger logger = LoggerFactory.getLogger(write.getCategory());
        DefaultHttpLogWriter.Level level = write.getLevel();
        return new KeyValueHttpLogger(logger, level, logbookObjectMapper());
    }

    @Bean
    @ConfigurationProperties(prefix = "logbook.obfuscate")
    public LogbookBodyFilterProperties logbookBodyFilterProperties() {
        return new LogbookBodyFilterProperties();
    }

}
