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
import java.util.ArrayList;
import se.kth.id2209.hw1.profiler.UserProfile;
import se.kth.id2209.hw1.util.Ontologies;

/**
 * This message receiver behavior listens for new profiler agents that wants
 * recommendations and responses for sent queries to the curator agent.
 * 
 * @author Kim
 */
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
		System.out.println(myAgent.getAID().getName()
				+ " RECIEVED message: " + msg.getOntology());
		if (msg != null) {
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

			if (msg.getOntology().equalsIgnoreCase(Ontologies.PROFILER_REQUEST_TOUR_AGENT)) {
				UserProfile up = (UserProfile) o;

				tourGuide.usersLock.lock();
                            try {
                                tourGuide.getUsers().put(msg.getSender(), up);
                            } finally {
                                tourGuide.usersLock.unlock();
                            }

				tourGuide.startTour();
			} else if (msg.getOntology().equalsIgnoreCase(Ontologies.QUERY_ARTIFACTS)) {
				Map<String, AID> requests = tourGuide.getRequests();
				final AID user = requests.get(msg.getConversationId());
				List<ACLMessage> msglist = tourGuide.getResponses().get(user);
				if(msglist == null) {
                                    msglist = new ArrayList<>();
                                }
				System.out.println("added msg " + msg + " to msgList: " + msglist);
				msglist.add(msg);
				requests.remove(msg.getConversationId());
			}
		}else {
			System.err.println("Agent " + myAgent.getAID().getName()
					+ " RECIEVED message: null.");
			block();
		}
                
                myAgent.addBehaviour(new TGAMsgReceiverBehaviour(myAgent, null, 
                        deadline, getDataStore(), null));
	}
}
