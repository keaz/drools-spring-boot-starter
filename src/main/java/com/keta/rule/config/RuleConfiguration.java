package com.keta.rule.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({ConfigData.class})
@ComponentScan("com.keta.rule")
public class RuleConfiguration {


}
