package com.bosch.manoj.sleuth.example.demo.support;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class LogbookBodyFilterProperties {
    private final List<String> body = new ArrayList<>();
}
