package se.kth.id2209.hw1.profiler;

import java.util.logging.Level;
import java.util.logging.Logger;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import se.kth.id2209.hw1.exhibition.Artifact;

@SuppressWarnings("serial")
class HandleArtifactInfoResponse extends OneShotBehaviour {

    private ACLMessage msg;
	private ProfilerAgent profilerAgent;

    HandleArtifactInfoResponse(Agent a, ACLMessage msg) {
        super(a);
        this.profilerAgent = (ProfilerAgent) a;
        this.msg = msg;
    }

	@Override
    public void action() {
        try {
            Artifact content = (Artifact) msg.getContentObject();
            profilerAgent.getLookedUpArtifacts().add(content);
            profilerAgent.getUserProfile().addVisitedItem(content.getId());
            System.out.println("Received artifact info: " + content);
        } catch (UnreadableException ex) {
            Logger.getLogger(ProfilerAgent.class.getName()).log(Level.SEVERE, null, ex);
            block();
        }
    }
}
