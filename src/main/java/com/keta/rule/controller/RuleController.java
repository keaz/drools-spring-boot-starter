package com.keta.rule.controller;

import com.keta.rule.cluster.ClusterManager;
import com.keta.rule.cluster.notify.Update;
import com.keta.rule.cluster.state.ClusterState;
import com.keta.rule.model.UpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("v1/rule")
public class RuleController {

    private final ClusterManager clusterManager;

    @PutMapping("refresh")
    public ResponseEntity<Void> refresh() {
        clusterManager.notifyRefresh();
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @GetMapping("info")
    public ResponseEntity<ClusterState> info() {
        return ResponseEntity.ok(clusterManager.getClusterState());
    }

    @PostMapping
    public ResponseEntity<Void> updateRule(@RequestBody UpdateRequest updateRequest){
        clusterManager.notify(updateRequest);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }



}
