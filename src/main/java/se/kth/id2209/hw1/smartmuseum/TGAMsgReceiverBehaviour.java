package se.kth.id2209.hw1.smartmuseum;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.DataStore;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.states.MsgReceiver;
import se.kth.id2209.hw1.profiler.UserProfile;
import se.kth.id2209.hw1.util.Ontologies;

@SuppressWarnings("serial")
class TGAMsgReceiverBehaviour extends MsgReceiver {

    private TourGuideAgent tourGuide;

	public TGAMsgReceiverBehaviour(Agent a, MessageTemplate mt, long deadline,
            DataStore s, java.lang.Object msgKey) {
        super(a, mt, deadline, s, msgKey);
        this.tourGuide = (TourGuideAgent) a;
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

        Object o = null;
        try {
            o = msg.getContentObject();
            if (o == null) {
                block();
            }
        } catch (UnreadableException ex) {
            Logger.getLogger(TourGuideAgent.class.getName()).log(Level.SEVERE, null, ex);
            block();
        }

        // New user
        if (msg.getOntology().equalsIgnoreCase(Ontologies.PROFILER_REQUEST_TOUR_AGENT)) {
            UserProfile up = (UserProfile) o;
            
            TourGuideAgent.usersLock.lock();
            tourGuide.getUsers().put(msg.getSender(), up);
            TourGuideAgent.usersLock.unlock();
            
            tourGuide.startTour();
        } else if (msg.getOntology().equalsIgnoreCase(Ontologies.ARTIFACT_RECOMMENDATION_ID)) {
        	Map<String, AID> requests = tourGuide.getRequests();
            final AID user = requests.get(msg.getConversationId());
            List<ACLMessage> msglist = tourGuide.getResponses().get(user);
            msglist.add(msg);
            requests.remove(msg.getConversationId());
        }
    }
}