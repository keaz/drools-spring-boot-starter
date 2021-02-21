package com.keta.rule.service;

import com.keta.rule.Fact;
import com.keta.rule.model.RuleVersion;
import com.keta.rule.model.UpdateRequest;

public interface Session {

    void refresh();

    <T extends Fact> void fireRules(T t);

    RuleVersion getCurrentVersion();

    void notifyState();

}
