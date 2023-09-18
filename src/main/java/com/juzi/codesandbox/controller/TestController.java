package com.juzi.codesandbox.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author codejuzi
 */
@RestController
@RequestMapping("/")
public class TestController {

    @GetMapping("/health")
    public String healthCheck() {
        return "ok";
    }
}