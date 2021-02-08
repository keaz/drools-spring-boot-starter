package com.keta.rule.cluster.jgroup;

import com.keta.rule.cluster.ClusterManager;
import com.keta.rule.cluster.MessageReceiver;
import com.keta.rule.cluster.notify.Refresh;
import com.keta.rule.cluster.notify.State;
import com.keta.rule.cluster.notify.Update;
import com.keta.rule.cluster.state.ClusterState;
import com.keta.rule.cluster.state.Member;
import com.keta.rule.exception.NotifyException;
import com.keta.rule.model.RuleVersion;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jgroups.*;
import org.jgroups.util.MessageBatch;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Log4j2
public class JGroupClusterManager implements ClusterManager, Receiver {

    private final JChannel channel;
    private final ClusterState clusterState = new ClusterState();
    private MessageReceiver messageReceiver;

    @PostConstruct
    @Override
    public void join() {
        log.info("Joining the JGroup cluster");
        channel.setReceiver(this);
    }

    @Override
    public void setMessageReceiver(MessageReceiver messageReceiver) {
        this.messageReceiver = messageReceiver;
    }

    @Override
    public void notifyForRefresh() {
        String addressAsString = channel.getAddressAsString();
        ObjectMessage message = new ObjectMessage();
        message.setObject(new Refresh(addressAsString));
        try {
            log.info("Notifying refresh to cluster ");
            channel.send(message);
        } catch (Exception e) {
            throw new NotifyException("Failed to notify refresh to cluster", e);
        }
    }

    @Override
    public void notifyUpdateUpdate(Update update) {

    }


    @Override
    public void receive(Message msg) {
        Object message = msg.getObject();
        if (message instanceof Refresh) {
            log.info("Handling Refresh event {}", message);
            return;
        }

        if (message instanceof Update) {
            log.info("Handling Update event {}", message);
            return;
        }

        if (message instanceof State) {
            log.info("Handling State event {}", message);

            State state = (State) message;
            String address = state.getAddress();
            if (clusterState.getMembers().containsKey(address)) {
                Member member = clusterState.getMembers().get(address);
                createMember(state, member);
                return;
            }
            log.warn("Cannot find member {} ", address);
        }
    }

    @Override
    public void receive(MessageBatch batch) {
        batch.stream().forEach(this::receive);
    }

    private void createMember(State state, Member member) {
        member.setCommitAuthor(state.getCommitAuthor());
        member.setCommitDate(state.getCommitDate());
        member.setCommitId(state.getCommitId());
        member.setCommitMessage(state.getCommitMessage());
        member.setGitTag(state.getGitTag());
    }

    @Override
    public void viewAccepted(View newView) {
        List<Address> addresses = newView.getMembers();
        Map<String, Member> memberMap = addresses.stream().collect(Collectors.toMap(Address::toString, this::createMember));
        clusterState.setMembers(memberMap);
        log.info("View Update {}", newView);
    }

    private Member createMember(Address address) {
        Member member = new Member();
        member.setAddress(address.toString());
        return member;
    }

    @Override
    public ClusterState getClusterState() {
        return clusterState;
    }

    @Override
    public void notifyState(RuleVersion ruleVersion) {
        String addressAsString = channel.getAddressAsString();
        ObjectMessage message = new ObjectMessage();

        State state = new State(addressAsString, ruleVersion.getGitTag(), ruleVersion.getCommitId(),
                ruleVersion.getCommitAuthor(), ruleVersion.getCommitDate()
                , ruleVersion.getCommitMessage());
        message.setObject(state);
        try {
            log.info("Notifying state to cluster");
            channel.send(message);
        } catch (Exception e) {
            log.error("Failed to notify state to cluster", e);
            throw new NotifyException("Failed to notify to cluster ", e);
        }
    }

    @Override
    @PreDestroy
    public void leave() {
        channel.close();
    }
}
