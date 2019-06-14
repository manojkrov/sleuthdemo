package com.bosch.manoj.sleuth.example.demo.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Strings;
import org.slf4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.zalando.logbook.*;

import java.io.IOException;
import java.util.*;

import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;

@Slf4j
public abstract class AbstractHttpLogger implements HttpLogger {

    private static final List<String> REPLACED_BODY =
            List.of("<binary>", "<multipart>", "<stream>", "<skipped>", "", "<empty>");

    private static final List<MediaType> MEDIA_TYPES =
            List.of(MediaType.valueOf("application/json"), MediaType.valueOf("application/*+json"));

    protected final ObjectMapper mapper;
    private final HttpLogWriter writer;

    public AbstractHttpLogger(Logger logger, DefaultHttpLogWriter.Level level, ObjectMapper mapper) {
        writer = new SpittedHttpLogWriter(logger, SpittedHttpLogWriter.Level.valueOf(level.name()), this::obfuscate);
        this.mapper = mapper;
    }

    protected abstract String format(Map<String, Object> context) throws IOException;

    protected abstract String obfuscate(String value);

    protected abstract Object body(final HttpMessage message) throws IOException;

    protected String minifyBody(HttpMessage message) throws IOException {
        String body = message.getBodyAsString();

        if (REPLACED_BODY.contains(body)) {
            return translate(body);
        } else {
            try {
                return translate(compact(body, message.getContentType()));
            } catch (Exception ex) {
                log.trace("Error while JSON parsing {}, Exception {}", body, ex);
                return translate(body);
            }
        }
    }

    protected final boolean isJson(@Nullable String contentType) {
        try {
            return contentType != null && MEDIA_TYPES.stream().anyMatch(type -> {
                MediaType mediaType = MediaType.valueOf(contentType);
                return type.isCompatibleWith(mediaType);
            });
        } catch (Exception ex) {
            log.trace("Error parsing media type {}", contentType, ex);
            return false;
        }
    }

    private String compact(String body, @Nullable String contentType) throws IOException {
        if (!isJson(contentType)) {
            return body;
        } else {
            JsonNode jsonNode = mapper.readValue(body, JsonNode.class);
            return mapper
                    .writer()
                    .without(INDENT_OUTPUT)
                    .writeValueAsString(jsonNode);
        }

    }

    @Override
    public String format(Precorrelation<HttpRequest> precorrelation) throws IOException {
        return format(prepare(precorrelation));
    }

    @Override
    public String format(Correlation<HttpRequest, HttpResponse> correlation) throws IOException {
        return format(prepare(correlation));
    }

    @Override
    public void writeRequest(Precorrelation<String> precorrelation) throws IOException {
        writer.writeRequest(precorrelation);
    }

    @Override
    public void writeResponse(Correlation<String, String> correlation) throws IOException {
        writer.writeResponse(correlation);
    }

    protected Map<String, Object> prepare(final Precorrelation<HttpRequest> precorrelation) throws IOException {
        final String correlationId = precorrelation.getId();
        final HttpRequest request = precorrelation.getRequest();

        final Map<String, Object> content = new LinkedHashMap<>();

        if (Origin.REMOTE.equals(request.getOrigin())) {
            content.put("authentication", getAuthenticationName());
        }

        content.put("origin", translate(request.getOrigin()));
        content.put("type", "request");
        content.put("correlation", correlationId);
        content.put("protocol", request.getProtocolVersion());
        content.put("remote", request.getRemote());

        content.put("method", request.getMethod());
        content.put("uri", request.getRequestUri());

        content.put("headers", request.getHeaders());
        content.put("body", body(request));

        return content;
    }


    protected Map<String, Object> prepare(final Correlation<HttpRequest, HttpResponse> correlation) throws IOException {
        final HttpRequest request = correlation.getRequest();
        final HttpResponse response = correlation.getResponse();

        final Map<String, Object> content = new LinkedHashMap<>();

        if (Origin.LOCAL.equals(response.getOrigin())) {
            content.put("authentication", getAuthenticationName());
        }

        content.put("origin", translate(response.getOrigin()));
        content.put("type", "response");
        content.put("correlation", correlation.getId());
        content.put("duration", correlation.getDuration().toMillis());
        content.put("protocol", response.getProtocolVersion());
        content.put("status", response.getStatus());

        content.put("method", request.getMethod());
        content.put("uri", request.getRequestUri());


        content.put("headers", response.getHeaders());
        content.put("body", body(response));

        return content;
    }

    protected static String translate(final String body) {
        return Strings.isNullOrEmpty(body) ? "<empty>" : body;
    }


    protected static String translate(final Origin origin) {
        return origin.name().toLowerCase(Locale.ROOT);
    }

    protected String getAuthenticationName() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                       .map(Authentication::getName)
                       .orElse("anonymous");
    }


}
