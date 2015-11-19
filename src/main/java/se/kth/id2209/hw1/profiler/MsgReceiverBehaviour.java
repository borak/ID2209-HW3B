package se.kth.id2209.hw1.profiler;

import jade.core.Agent;
import jade.core.behaviours.DataStore;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.states.MsgReceiver;
import se.kth.id2209.hw1.util.Ontologies;

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
		if (msg != null) {
			System.out.println(myAgent.getAID().getName()
					+ " RECIEVED message: " + msg.getOntology());

			if (msg.getOntology().equalsIgnoreCase(Ontologies.ARTIFACT_REQUEST_INFO)) {
				myAgent.addBehaviour(new HandleArtifactInfoResponse(getAgent(), msg));
			} else if (msg.getOntology().equalsIgnoreCase(Ontologies.ARTIFACT_RECOMMENDATION_ID)) {
				myAgent.addBehaviour(new HandleArtifactRecommendation(getAgent(), msg));
			}
		}
		else {
			System.err.println(myAgent.getAID().getName()
					+ " RECIEVED message: null.");
			block();
		}
	}
}
