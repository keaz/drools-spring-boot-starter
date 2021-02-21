package com.keta.rule.service;

import com.keta.rule.model.RuleVersion;

public interface GitService {

    void cloneRepo();

    void pullRepo();

    void checkoutFor(String commit);

    RuleVersion getCurrentVersion();

}
