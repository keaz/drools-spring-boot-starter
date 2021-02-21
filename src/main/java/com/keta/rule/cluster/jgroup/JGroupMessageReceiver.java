package com.keta.rule.cluster.jgroup;

import com.keta.rule.cluster.MessageReceiver;
import com.keta.rule.cluster.notify.Refresh;
import com.keta.rule.cluster.notify.State;
import com.keta.rule.cluster.notify.Update;
import com.keta.rule.cluster.state.ClusterState;
import com.keta.rule.cluster.state.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jgroups.*;
import org.jgroups.util.MessageBatch;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Log4j2
public class JGroupMessageReceiver implements MessageReceiver, Receiver {

    private final JChannel channel;
    private final ClusterState clusterState = new ClusterState();

    @PostConstruct
    public void init() {
        log.info("Joining the JGroup cluster");
        channel.setReceiver(this);
    }

    @Override
    public void handleRefresh() {

    }

    @Override
    public void handleUpdate(Update update) {

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
            String address = state.getMemberId();
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

    private Member createMember(Address address) {
        Member member = new Member();
        member.setMemberId(address.toString());
        return member;
    }

    @Override
    public void viewAccepted(View newView) {
        List<Address> addresses = newView.getMembers();
        Map<String, Member> memberMap = addresses.stream().collect(Collectors.toMap(Address::toString, this::createMember));
        clusterState.setMembers(memberMap);
        log.info("View Update {}", newView);
    }

}
