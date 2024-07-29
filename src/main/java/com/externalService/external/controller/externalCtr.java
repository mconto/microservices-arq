package com.externalService.external.controller;

import com.externalService.external.response.ResponseHTTP;
import com.externalService.external.service.DataService;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

public class externalCtr {

    @Autowired
    DataService dataService;

    @GetMapping("/get-data")
    public ResponseEntity<String> returnGetData(@RequestParam String url, @RequestParam String body) {
        return dataService.getRequest(url);
    }

    @PostMapping("/post-data")
    public ResponseEntity<String> returnPostData(@RequestParam String url, @RequestParam String body) {
        dataService.postRequest(url, body);
    }
}
