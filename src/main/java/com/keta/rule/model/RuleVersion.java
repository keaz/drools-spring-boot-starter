package com.keta.rule.model;

import lombok.Data;

import java.util.Date;

@Data
public class RuleVersion {

    private String gitTag;
    private String commitId;
    private String commitAuthor;
    private Date commitDate;
    private String commitMessage;

}
