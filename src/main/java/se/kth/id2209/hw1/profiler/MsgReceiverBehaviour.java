package se.kth.id2209.hw1.profiler;

import jade.core.Agent;
import jade.core.behaviours.DataStore;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.states.MsgReceiver;
import se.kth.id2209.hw1.util.Ontologies;

/**
 * In order to process a received recommendation of an artifact, this class
 * will modify the Profiler agent's state by storing that recommendation and
 * forward the request to a Curator agent.
 *
 * @author Kim
 */
/**
 * This behavior is listening for an agent's messages, specifically, requests for
 * artifacts and artifacts recommendations and start appropriate behaviors to
 * handle the message.
 * 
 * @author Kim
 */
class MsgReceiverBehaviour extends MsgReceiver {

    public MsgReceiverBehaviour(Agent a, MessageTemplate mt, long deadline,
            DataStore s, java.lang.Object msgKey) {
        super(a, mt, deadline, s, msgKey);
    }

    @Override
    public void handleMessage(ACLMessage msg) {
        if (msg == null) {
            System.err.println("Agent " + myAgent.getAID().getName()
                    + " received message: null.");
            block();
        }
        System.out.println("Agent " + myAgent.getAID().getName()
                + " received message: " + msg.getOntology());

        if (msg.getOntology().equalsIgnoreCase(Ontologies.ARTIFACT_REQUEST_INFO)) {
            myAgent.addBehaviour(new HandleArtifactInfoResponse(getAgent(), msg));
        } else if (msg.getOntology().equalsIgnoreCase(Ontologies.ARTIFACT_RECOMMENDATION_ID)) {
            myAgent.addBehaviour(new HandleArtifactRecommendation(getAgent(), msg));
        }
    }
}
