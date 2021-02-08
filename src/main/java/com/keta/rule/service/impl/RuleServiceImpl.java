package com.keta.rule.service.impl;

import com.keta.rule.service.RuleService;
import com.keta.rule.Fact;
import com.keta.rule.service.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Log4j2
public class RuleServiceImpl<T extends Fact> implements RuleService<T> {

    private final Session session;

    @Override
    public T execute(T fact) {
        session.fireRules(fact);
        return fact;
    }
}
