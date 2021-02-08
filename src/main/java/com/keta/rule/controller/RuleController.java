package com.keta.rule.controller;

import com.keta.rule.cluster.ClusterManager;
import com.keta.rule.cluster.state.ClusterState;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("v1/rule")
public class RuleController {

    private final ClusterManager clusterManager;

    @PutMapping("refresh")
    public ResponseEntity<Void> refresh() {
        clusterManager.notifyForRefresh();
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @GetMapping("info")
    public ResponseEntity<ClusterState> info() {
        return ResponseEntity.ok(clusterManager.getClusterState());
    }


}
