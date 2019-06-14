package com.bosch.manoj.sleuth.example.demo.support;

import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.HttpLogWriter;

public interface HttpLogger extends HttpLogWriter, HttpLogFormatter {
}
