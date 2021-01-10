package com.keta.rule.service;

import com.keta.rule.Fact;

public interface RuleService <T extends Fact> {

    T execute(T fact);

}
