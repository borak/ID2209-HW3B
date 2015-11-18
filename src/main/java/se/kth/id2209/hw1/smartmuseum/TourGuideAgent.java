/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.id2209.hw1.smartmuseum;

import jade.core.AID;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import jade.core.Agent;
import jade.core.behaviours.DataStore;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.states.MsgReceiver;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import se.kth.id2209.hw1.exhibition.Artifact;
import se.kth.id2209.hw1.exhibition.CuratorAgent;
import se.kth.id2209.hw1.profiler.ProfilerAgent;
import se.kth.id2209.hw1.profiler.UserProfile;
import se.kth.id2209.hw1.util.DFUtilities;
import se.kth.id2209.hw1.util.Ontologies;

/**
 * Tour Guide Agent retrieves the information about artifacts in the
 * gallery/museum and builds a virtual tour (upon the request) for profiler
 * agent.
 *
 * â€“The virtual tour contains list of related items (based on userâ€™s interest,
 * age, etc..)
 *
 * ïƒ˜ Tour Guide agent interacts with Curator Agent in order to build the virtual
 * tour.
 *
 * TODO: Implement behaviours. Behaviors should correspond to each category
 * below: ï�± Simple Behavior (at least 5 different behaviors): â€“ CyclicBehaviour,
 * MsgReceiver, OneShotBehaviour, SimpleAchieveREInitiator,
 * SimpleAchieveREResponder, TickerBehaviour, WakerBehaviour ï�± Composite
 * Behaviors (at least 2 different behaviors): â€“ ParallelBehaviour,
 * FSMBehaviour, SequentialBehaviour
 *
 * @author Kim
 */
public class TourGuideAgent extends Agent {

    private static final long serialVersionUID = 5883088851872677769L;
    private HashMap<AID, UserProfile> users = new HashMap<>();
    private TGAMsgReceiverBehaviour msgReceiver;

    @SuppressWarnings("serial")
    @Override
    protected void setup() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());

        ServiceDescription sd = new ServiceDescription();
        sd.setType("Tour-Guide-agent");
        sd.setName(getLocalName());
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        ParallelBehaviour par = new ParallelBehaviour(ParallelBehaviour.WHEN_ALL);
        msgReceiver = new TGAMsgReceiverBehaviour(this, 
                null, MsgReceiver.INFINITE, new DataStore(), null);
        par.addSubBehaviour(msgReceiver);
    }

    // TODO: se till att denna kallas dÃ¥ en profiler vill starta en tour
    @SuppressWarnings("serial")
    void startTour() {

        SequentialBehaviour seq = new SequentialBehaviour();
        seq.addSubBehaviour(new OneShotBehaviour() {

            @Override
            public void action() {
                Iterator<Entry<AID, UserProfile>> it = users.entrySet().iterator();
                ACLMessage msg = null;

                while (it.hasNext()) {
                    Entry<AID, UserProfile> entry = it.next();
                    AID aid = entry.getKey();
                    UserProfile profile = entry.getValue();
                    List<String> interests = profile.getInterests();
                    ArrayList<Artifact.GENRE> genres = new ArrayList<>();
                    for(String s : interests) {
                        genres.add(Artifact.GENRE.valueOf(s));
                    }
                    
                    // Search for a Curator agent
                    DFAgentDescription dfdTGA = new DFAgentDescription();
                    ServiceDescription sdTGA = new ServiceDescription();
                    sdTGA.setType("Curator-agent");
                    dfdTGA.addServices(sdTGA);
                    AID[] aids = DFUtilities.searchDF(TourGuideAgent.this, dfdTGA);
                    if (aids.length < 1 || aids[0] == null) {
                        block();
                    }
                    AID caid = aids[0];
                    
                    try {
                        msg.addReceiver(caid); 	
                        msg.setOntology(Ontologies.QUERY_ARTIFACTS);
                        msg.setContentObject((Serializable) genres);
                        
                        msgReceiver.addRequest(msg.getConversationId(), aid);
                        send(msg);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        seq.addSubBehaviour(new PresentBehaviour(this, 10000));
        addBehaviour(seq);
    }
    
    private class TGAMsgReceiverBehaviour extends MsgReceiver {
        private Map<String, AID> requests;

        public TGAMsgReceiverBehaviour(Agent a, MessageTemplate mt, long deadline,
                DataStore s, java.lang.Object msgKey) {
            super(a, mt, deadline, s, msgKey);
            requests = new HashMap();
        }

        @Override
        public void handleMessage(ACLMessage msg) {
            if (msg == null) {
                System.err.println("Agent " + getAID().getName()
                        + " received message: null.");
                block();
            }
            System.out.println("Agent " + getAID().getName()
                    + " received message: " + msg.getOntology());

            if (msg.getOntology().equalsIgnoreCase(Ontologies.PROFILER_REQUEST_TOUR_AGENT)) {
                try {
                    Object o = msg.getContentObject();
                    if(o == null) {
                        block();
                    }
                    //?????
                    UserProfile up = (UserProfile) o;
                    users.put(msg.getSender(), up);
                    requests.remove(msg.getConversationId());
                    startTour();
                } catch (UnreadableException ex) {
                    Logger.getLogger(TourGuideAgent.class.getName()).log(Level.SEVERE, null, ex);
                    block();
                }
            }
        }

        private void addRequest(String msgId, AID aid) {
            requests.put(msgId, aid);
        }
    }

    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        System.out.println("Agent " + getAID().getName() + " is terminating.");
    }

    @SuppressWarnings("serial")
    class PresentBehaviour extends TickerBehaviour {

        private Iterator<Entry<AID, UserProfile>> it = users.entrySet().iterator();
        private int index;

        public PresentBehaviour(Agent a, long period) {
            super(a, period);
            index = 0;
        }

        // ????????????????
        @Override
        protected void onTick() {
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            Entry<AID, UserProfile> entry = it.next();
            AID aid = entry.getKey();
            List<String> names = null;//= entry.getValue();

            if (index > names.size()) {
                System.out.println(aid.getName() + ": Your tour is finished, get out of here!");
                users.remove(aid);
            } else {
                String name = names.get(index);

                msg.addReceiver(aid);
                try {
                    msg.setContentObject(name);
                    send(msg);

                    System.out.println(myAgent + ": presenting item " + name + " to " + aid);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            index++;
        }
    }
}
