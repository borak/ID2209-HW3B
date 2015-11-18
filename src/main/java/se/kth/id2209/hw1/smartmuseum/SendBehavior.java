package se.kth.id2209.hw1.smartmuseum;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.util.Logger;
import se.kth.id2209.hw1.util.Ontologies;

@SuppressWarnings("serial")
class SendBehavior extends OneShotBehaviour {

	private final AID receiver;
	private final ACLMessage queryResponse;
	private TourGuideAgent tourGuide;

	SendBehavior(Agent a, AID receiver, ACLMessage queryResponse) {
		this.tourGuide = (TourGuideAgent) a;
		this.receiver = receiver;
		this.queryResponse = queryResponse;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void action() {
		List<Integer> list = null;
		try {
			list = (List<Integer>) queryResponse.getContentObject();
		} catch (UnreadableException ex) {
			Logger.getLogger(TourGuideAgent.class.getName()).log(Level.SEVERE, null, ex);
			block();
		}
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.addReceiver(receiver);
		msg.setOntology(Ontologies.ARTIFACT_RESPONSE_RECOMMENDATION_ID);
		msg.setConversationId(msg.getConversationId());

		try {
			msg.setContentObject((Serializable) list);
		} catch (IOException ex) {
			Logger.getLogger(TourGuideAgent.class.getName()).log(Level.SEVERE, null, ex);
		}

		tourGuide.send(msg);
	}
}