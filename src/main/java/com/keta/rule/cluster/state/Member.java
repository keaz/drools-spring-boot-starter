package com.keta.rule.cluster.state;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Member {

    @EqualsAndHashCode.Include
    private String memberId;
    private String gitTag;
    private String commitId;
    private String commitAuthor;
    private Date commitDate;
    private String commitMessage;



}
