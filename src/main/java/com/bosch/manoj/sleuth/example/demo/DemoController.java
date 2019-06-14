package com.bosch.manoj.sleuth.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@RestController
@Slf4j
@RefreshScope
public class DemoController {

    @GetMapping(value = "/", produces = APPLICATION_JSON_UTF8_VALUE)
    public Map<String, String> helloSleuth() {
        Map<String,String> response = new HashMap<>();
        response.put("Result", "success");
        return response;
    }

    @PostMapping(value = "/", produces = APPLICATION_JSON_UTF8_VALUE)
    public Map<String, String> hiSleuth() {

        Map<String,String> response = new HashMap<>();
        response.put("Result", "success");
        return response;

    }
}
