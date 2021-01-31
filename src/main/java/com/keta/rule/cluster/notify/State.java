package com.keta.rule.cluster.notify;

import lombok.Data;
import lombok.Getter;

import java.util.Date;

@Getter
public class State extends ClusterMessage {

    private String gitTag;
    private String commitId;
    private String commitAuthor;
    private Date commitDate;
    private String commitMessage;

    public State(String address, String gitTag, String commitId, String commitAuthor, Date commitDate, String commitMessage) {
        super(address);
        this.gitTag = gitTag;
        this.commitId = commitId;
        this.commitAuthor = commitAuthor;
        this.commitDate = commitDate;
        this.commitMessage = commitMessage;
    }

}
