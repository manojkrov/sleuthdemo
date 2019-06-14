package com.bosch.manoj.sleuth.example.demo.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.logbook.Correlation;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.Precorrelation;
import org.zalando.logbook.RawHttpRequest;

import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class SpittedHttpLogWriter implements HttpLogWriter {

    private final Logger head;
    private final Logger full;

    private final Level level;

    private final UnaryOperator<String> obfuscator;

    public SpittedHttpLogWriter(Logger logger, Level level, UnaryOperator<String> obfuscator) {
        this.head = LoggerFactory.getLogger(logger.getName() + ".head");
        this.full = LoggerFactory.getLogger(logger.getName() + ".full");
        this.level = level;
        this.obfuscator = obfuscator;
    }

    @Override
    public void writeRequest(Precorrelation<String> precorrelation) throws IOException {
        level.write(head, obfuscator.apply(precorrelation.getRequest()));
        level.write(full, precorrelation.getRequest());
    }

    @Override
    public void writeResponse(Correlation<String, String> correlation) throws IOException {
        level.write(head, obfuscator.apply(correlation.getResponse()));
        level.write(full, correlation.getResponse());
    }

    @Override
    public boolean isActive(final RawHttpRequest request) {
        return true;
    }


    public enum Level {
        INFO(Logger::info, Logger::isInfoEnabled),
        WARN(Logger::warn, Logger::isWarnEnabled),
        ERROR(Logger::error, Logger::isErrorEnabled),
        DEBUG(Logger::debug, Logger::isDebugEnabled),
        TRACE(Logger::trace, Logger::isTraceEnabled);

        private final BiConsumer<Logger, String> consumer;
        private final Predicate<Logger> activator;

        Level(BiConsumer<Logger, String> consumer, Predicate<Logger> activator) {
            this.consumer = consumer;
            this.activator = activator;
        }

        public void write(Logger logger, String value) {
            if (activator.test(logger)) {
                consumer.accept(logger, value);
            }
        }
    }
}
