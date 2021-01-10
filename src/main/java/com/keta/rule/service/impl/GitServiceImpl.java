package com.keta.rule.service.impl;

import com.keta.rule.config.ConfigData;
import com.keta.rule.exception.GitException;
import com.keta.rule.model.RuleVersion;
import com.keta.rule.service.GitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.*;

@Log4j2
@RequiredArgsConstructor
@Service
public class GitServiceImpl implements GitService {

    private final ConfigData configData;

    @PostConstruct
    public void init() {
        cloneRepo();
    }

    @Override
    public void cloneRepo() {
        File file = new File(configData.getClonedDirectory());
        Git git = null;
        try {
            if (file.exists()) {
                git = Git.open(file);
                log.info("Rule repo exists locally, pulling the rules");
            } else {
                log.info("Rule repo does not exists locally, cloning the rules");
                Git.cloneRepository();
                git = Git.cloneRepository()
                        .setCredentialsProvider(new UsernamePasswordCredentialsProvider(configData.getGitUserName(), configData.getGitToken()))
                        .setURI(configData.getGitUrl()).setDirectory(file).call();
            }
            git.pull().call();
        } catch (GitAPIException | IOException e) {
            log.error("Failed to close/pull repo ", e);
            throw new GitException("Failed to close/pull repo ",e);
        }finally {
            if(git != null){
                git.close();
            }
        }
        log.info("Successfully cloned/pulled the repo {} ", configData.getGitUrl());
    }

    @Override
    public void pullRepo() {
        File file = new File(configData.getClonedDirectory());
        try (Git git = Git.open(file)) {
            log.info("Rule repo exists locally, pulling the rules");
            git.pull().call();
        } catch (IOException | GitAPIException e) {
            log.error("Failed to pull repo ", e);
            throw new GitException("Failed to pull repo ",e);
        }
    }

    @Override
    public RuleVersion getCurrentVersion() {
        log.info("Get Current version of the rules");
        File file = new File(configData.getClonedDirectory());
        try (Git git = Git.open(file)) {
            Ref head = git.getRepository().getRefDatabase().findRef("HEAD");
            List<Ref> list = git.tagList().call();
            Optional<Ref> tag = list.stream().filter(ref -> ref.getObjectId().equals(head.getObjectId())).findFirst();
            Iterable<RevCommit> logs = git.log().call();
            RevCommit lastCommit = logs.iterator().next();
            RuleVersion ruleVersion = createVersion(lastCommit);

            tag.ifPresent(ref -> ruleVersion.setGitTag(ref.getName()));
            return ruleVersion;
        } catch (IOException | GitAPIException e) {
            log.error("Error getting rule version", e);
            throw new GitException("Error getting rule version",e);
        }

    }

    private RuleVersion createVersion(RevCommit lastCommit) {
        RuleVersion ruleVersion = new RuleVersion();
        ruleVersion.setCommitAuthor(lastCommit.getCommitterIdent().getName());
        ruleVersion.setCommitMessage(lastCommit.getFullMessage());
        ruleVersion.setCommitId(lastCommit.getId().getName());
        PersonIdent authorIdent = lastCommit.getAuthorIdent();
        Date authorDate = authorIdent.getWhen();
        TimeZone authorTimeZone = authorIdent.getTimeZone();
        Calendar instance = Calendar.getInstance(authorTimeZone);
        instance.setTime(authorDate);
        ruleVersion.setCommitDate(instance.getTime());

        return ruleVersion;
    }

}
