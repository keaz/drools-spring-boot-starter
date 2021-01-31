package com.keta.rule.cluster.jgroup;

import com.keta.rule.cluster.ClusterManager;
import com.keta.rule.cluster.MessageReceiver;
import com.keta.rule.cluster.notify.Refresh;
import com.keta.rule.cluster.notify.State;
import com.keta.rule.cluster.notify.Update;
import com.keta.rule.cluster.state.ClusterState;
import com.keta.rule.cluster.state.Member;
import com.keta.rule.model.RuleVersion;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jgroups.*;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Log4j2
@Service
public class JGroupClusterManager implements ClusterManager, Receiver {

    private JChannel channel;

    private final MessageReceiver messageReceiver;
    private final ClusterState clusterState = new ClusterState();

    @PostConstruct
    @Override
    public void join() {
        try {
            channel = new JChannel("src/main/resources/tcp.xml");
            channel.connect("drools-cluster");
            channel.setReceiver(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void notifyForRefresh() {
        String addressAsString = channel.getAddressAsString();
        ObjectMessage message = new ObjectMessage();
        message.setObject(new Refresh(addressAsString));
        try {
            channel.send(message);
        } catch (Exception e) {
            throw new RuntimeException(e);
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

        if(message instanceof State){
            log.info("Handling State event {}", message);
            Address destination = msg.getDest();
            State state = (State)message;
            if(clusterState.getMembers().containsKey(destination)){
                Member member = clusterState.getMembers().get(destination);
                createMember(state, member);
                return;
            }
            log.warn("Cannot find member {} ",destination);
        }
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

    private Member createMember(Address address){
        Member member = new Member();
        member.setAddress(address.toString());
        return member;
    }

    @Override
    public ClusterState getClusterState() {
        return clusterState;
    }

    @Override
    public void notifyState(RuleVersion ruleVersion){
        String addressAsString = channel.getAddressAsString();
        ObjectMessage message = new ObjectMessage();

        message.setObject(new Refresh(addressAsString));
        try {
            channel.send(message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
