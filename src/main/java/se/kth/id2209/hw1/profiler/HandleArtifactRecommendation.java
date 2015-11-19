package se.kth.id2209.hw1.profiler;

import java.util.logging.Level;
import java.util.logging.Logger;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

/**
 * In order to process a received recommendation of an artifact, this class
 * will modify the Profiler agent's state by storing that recommendation and
 * forward the request to a Curator agent.
 *
 * @author Kim
 */
class HandleArtifactRecommendation extends OneShotBehaviour {
    private ACLMessage msg;
    private ProfilerAgent profilerAgent;

    HandleArtifactRecommendation(Agent a, ACLMessage msg) {
        super(a);
        this.profilerAgent = (ProfilerAgent) a;
        this.msg = msg;
    }

    @Override
    public void action() {
        Integer content = null;
        try {
            content = (Integer) msg.getContentObject();
            profilerAgent.getRecommendedArtifacts().add(content);
            System.out.println(myAgent.getAID().getName()
                    + ": was recommended artifact with ID=" + content);
        } catch (UnreadableException ex) {
            Logger.getLogger(ProfilerAgent.class.getName()).log(Level.SEVERE, null, ex);
            block();
        }

        myAgent.addBehaviour(new ArtifactRequestBehaviour(getAgent(), content));
    }
}
