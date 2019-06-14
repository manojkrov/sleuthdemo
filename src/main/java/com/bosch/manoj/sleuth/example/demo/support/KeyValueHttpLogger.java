package com.bosch.manoj.sleuth.example.demo.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.zalando.logbook.DefaultHttpLogWriter.Level;
import org.zalando.logbook.HttpMessage;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

public class KeyValueHttpLogger extends AbstractHttpLogger {
    public KeyValueHttpLogger(Logger logger, Level level, ObjectMapper mapper) {
        super(logger, level, mapper);
    }

    @Override
    protected String format(Map<String, Object> context) throws IOException {
        return context.entrySet().stream().map(this::join).collect(Collectors.joining(" "));
    }

    @Override
    protected String obfuscate(String value) {
        return value.replaceFirst("(body=.*)", "body=<skipped>");
    }

    @Override
    protected Object body(HttpMessage message) throws IOException {
        return translate(minifyBody(message));
    }

    private String join(Map.Entry<String, Object> entry) {
        return String.format("%s=%s", entry.getKey(), entry.getValue());
    }
}
