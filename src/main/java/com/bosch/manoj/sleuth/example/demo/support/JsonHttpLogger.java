package com.bosch.manoj.sleuth.example.demo.support;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.zalando.logbook.DefaultHttpLogWriter.Level;
import org.zalando.logbook.HttpMessage;

import java.io.IOException;
import java.util.Map;

public class JsonHttpLogger extends AbstractHttpLogger {

    public JsonHttpLogger(Logger logger, Level level, ObjectMapper mapper) {
        super(logger, level, mapper);
    }

    @Override
    protected String format(Map<String, Object> context) throws IOException {
        context.put("body", context.get("body"));
        return mapper.writeValueAsString(context);
    }

    @Override
    protected String obfuscate(String value) {
        return value.replaceFirst("(\"body\":.+)", "\"body\":\"<skipped>\"}");
    }

    @Override
    protected Object body(HttpMessage message) throws IOException {
        String body = super.minifyBody(message);
        if (isJson(message.getContentType())) {
            return new JsonBody(body);
        } else {
            return body;
        }
    }

    private static final class JsonBody {

        private final String json;

        private JsonBody(final String json) {
            this.json = json;
        }

        @JsonRawValue
        @JsonValue
        public String getJson() {
            return json;
        }
    }
}
