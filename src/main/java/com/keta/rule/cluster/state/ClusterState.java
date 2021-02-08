package com.keta.rule.cluster.state;

import lombok.Data;
import org.apache.commons.collections4.map.HashedMap;

import java.util.List;
import java.util.Map;

@Data
public class ClusterState {

   private Map<String,Member> members =  new HashedMap<>();

}
