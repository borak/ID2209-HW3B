package se.kth.id2209.hw2.smartmuseum;

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
import java.util.ArrayList;
import se.kth.id2209.hw2.util.Ontologies;
import se.kth.id2209.hw2.util.SList;

/**
 * This behavior sends recommendations that the tour agent has put togheter to
 * the corresponding user that had had requested recommendations.
 * 
 * @author Kim
 */
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
            msg.setContentObject(new SList(list));
        } catch (IOException ex) {
            Logger.getLogger(TourGuideAgent.class.getName()).log(Level.SEVERE, null, ex);
        }

		System.out.println(myAgent.getName() + " SENDING msg: " + msg.getOntology() + " to " + receiver);
		tourGuide.send(msg);
	}
}
