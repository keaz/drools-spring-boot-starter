package com.keta.rule.controller;

import com.keta.rule.model.RuleVersion;
import com.keta.rule.service.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("v1/rule")
public class RuleController {

    private final Session session;

    @PutMapping("refresh")
    public ResponseEntity<Void> refresh() {
        session.refresh();
        return ResponseEntity.ok().build();
    }

    @GetMapping("info")
    public ResponseEntity<RuleVersion> info() {
        return ResponseEntity.ok(session.getCurrentVersion());
    }


}
