package com.keta.rule.service.impl;

import com.keta.rule.Fact;
import com.keta.rule.service.GitService;
import com.keta.rule.service.Session;
import com.keta.rule.model.RuleVersion;
import com.keta.rule.config.ConfigData;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.KieRepository;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.io.ResourceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Log4j2
@Service
@DependsOn({"gitServiceImpl"})
public class DroolsSessionImpl implements Session {

    private KieServices kieServices = KieServices.Factory.get();

    private final ConfigData configData;
    private final GitService gitService;
    @Autowired(required = false)
    private KieSession kieSession;


    @PostConstruct
    public void init(){
        log.info("Initializing Drools Session");
        createSession();
    }

    @Override
    public void refresh() {
        log.info("Refreshing Drools Session");
        gitService.pullRepo();
        createSession();
        log.info("Refreshed Drools Session");
    }

    @Override
    public <T extends Fact> void fireRules(T t) {
        log.debug("Executing rules on fact {}",t);
        kieSession.insert(t);
        kieSession.fireAllRules();
    }

    private List<String> ruleTables() {
        String ruleFileDirectory = configData.getClonedDirectory() + File.separator + configData.getDirectory();
        log.info("Extracting Rules tables from {}",ruleFileDirectory);
        File ruleDirectory = new File(ruleFileDirectory);
        File[] files = ruleDirectory.listFiles();
        return Arrays.stream(files).sequential().filter(File::isFile)
                .filter(file -> file.getName().endsWith("xlsx"))
                .map(File::getPath).collect(Collectors.toList());
    }

    private KieFileSystem getKieFileSystem(){
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
        List<String> rulesTables = ruleTables();
        log.info("Creating Kie file system from files {}",rulesTables);
        for (String rule : rulesTables) {
            kieFileSystem.write(ResourceFactory.newFileResource(rule));
        }
        return kieFileSystem;
    }

    private void getKieRepository() {
        final KieRepository kieRepository = kieServices.getRepository();
        kieRepository.addKieModule(() -> kieRepository.getDefaultReleaseId());
    }

    private void createSession(){
        getKieRepository();
        log.debug("Creating KieBuilder");
        KieBuilder kb = kieServices.newKieBuilder(getKieFileSystem());
        kb.buildAll();

        KieModule kieModule = kb.getKieModule();
        KieContainer kContainer = kieServices.newKieContainer(kieModule.getReleaseId());

        kieSession = kContainer.newKieSession();
        log.info("KieSession created");
    }

    @Override
    public RuleVersion getCurrentVersion() {
        return gitService.getCurrentVersion();
    }


}
