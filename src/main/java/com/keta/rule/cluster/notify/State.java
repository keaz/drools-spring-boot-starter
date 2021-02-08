package com.keta.rule.cluster.notify;

import lombok.Data;
import lombok.Getter;

import java.util.Date;

@Getter
public final class State extends ClusterMessage {

    private final String gitTag;
    private final String commitId;
    private final String commitAuthor;
    private final Date commitDate;
    private final String commitMessage;

    public State(String address, String gitTag, String commitId, String commitAuthor, Date commitDate, String commitMessage) {
        super(address);
        this.gitTag = gitTag;
        this.commitId = commitId;
        this.commitAuthor = commitAuthor;
        this.commitDate = commitDate;
        this.commitMessage = commitMessage;
    }

}
