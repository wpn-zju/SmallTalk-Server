package com.smalltalknow.service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

// Todo: HTTP Version
@Controller
public class SmallTalkController {
    @GetMapping("/queue/")
    public ResponseEntity<?> loadUser(
            @RequestParam("session") String session
    ) {
        return ResponseEntity.notFound().build();
    }
}
