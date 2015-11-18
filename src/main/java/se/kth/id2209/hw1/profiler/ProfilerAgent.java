/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.id2209.hw1.profiler;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.DataStore;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.states.MsgReceiver;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import se.kth.id2209.hw1.exhibition.Artifact;
import se.kth.id2209.hw1.exhibition.CuratorAgent;
import se.kth.id2209.hw1.smartmuseum.TourGuideAgent;
import se.kth.id2209.hw1.util.DFUtilities;
import se.kth.id2209.hw1.util.Ontologies;

/**
 * Profiler Agent travels around the network and looks for interesting
 * information about art and culture from online museums or art galleries on the
 * internet.
 *
 * The Profiler Agent interacts directly with Tour Guide Agent to get a
 * personalized virtual tour. The Profile Agent interacts with Curator Agent to
 * obtain detailed information about each of the items stated in the virtual
 * tour.
 *
 * TODO: Implement behaviours. Behaviors should correspond to each category
 * below: Simple Behavior (at least 5 different behaviors): – CyclicBehaviour,
 * MsgReceiver, OneShotBehaviour, SimpleAchieveREInitiator,
 * SimpleAchieveREResponder, TickerBehaviour, WakerBehaviour Composite Behaviors
 * (at least 2 different behaviors): – ParallelBehaviour, FSMBehaviour,
 * SequentialBehaviour
 *
 * CyclicBehaviour - curator listen artifact requests OneShotBehaviour - ask for
 * information from curatorAgent MsgReceiver - receiver in profiler agent,
 * receiver in curatoragent TickerBehaviour - touragent WakerBehaviour - curator
 * checks DB
 *
 * ParallelBehaviour - curator SequentialBehaviour - touragent
 *
 * @author Kim
 */
public class ProfilerAgent extends Agent {

    private UserProfile profile;
    private CuratorAgent cAgent; // temporary - register at DF instead
    private TourGuideAgent tgAgent; // temporary - register at DF instead
    private List<Integer> recommendedArtifacts;
    private List<Artifact> lookedUpArtifacts;
    private static final String ACL_LANGUAGE = "Java Serialized";

    @Override
    protected void setup() {
        super.setup();
        recommendedArtifacts = new ArrayList();
        lookedUpArtifacts = new ArrayList();
        List<String> interests = new ArrayList();
        interests.add(Artifact.GENRE.PAINTING.toString());
        List<Integer> visitedItems = new ArrayList();
        profile = new UserProfile(22, "Programmer", UserProfile.GENDER.male, 
            interests, visitedItems);
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());

        ServiceDescription sd = new ServiceDescription();
        sd.setType("Profiler-agent");
        sd.setName(getLocalName());
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        addBehaviour(new CyclicBehaviour() {
            boolean isDone = false;
            
            @Override
            public void action() {
                isDone = sendTourGuideRequest();
            }
            
            public boolean isDone() {
                return isDone;
            }
            
        });

        addBehaviour(new MsgReceiverBehaviour(this, null, MsgReceiver.INFINITE,
                new DataStore(), null));
    }

    private boolean sendTourGuideRequest() {
        DFAgentDescription dfdTGA = new DFAgentDescription();
        ServiceDescription sdTGA = new ServiceDescription();
        sdTGA.setType("Tour-Guide-agent");
        dfdTGA.addServices(sdTGA);
        AID[] aids = DFUtilities.searchDF(this, dfdTGA);
        if (aids.length < 1) {
            return false;
        }
        
        AID tgAgent = (AID) aids[0];
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(tgAgent);
        msg.setLanguage(ACL_LANGUAGE);
        msg.setOntology(Ontologies.PROFILER_REQUEST_TOUR_AGENT);
        try {
            msg.setContentObject(getAID());
            send(msg);
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        //myGui.dispose();
        System.out.println("Agent " + getAID().getName() + " is terminating.");
    }

    private class MsgReceiverBehaviour extends MsgReceiver {

        public MsgReceiverBehaviour(Agent a, MessageTemplate mt, long deadline,
                DataStore s, java.lang.Object msgKey) {
            super(a, mt, deadline, s, msgKey);
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

            if (msg.getOntology().equalsIgnoreCase(Ontologies.ARTIFACT_REQUEST_INFO)) {
                addBehaviour(new HandleArtifactInfoResponse(getAgent(), msg));
            } else if (msg.getOntology().equalsIgnoreCase(Ontologies.ARTIFACT_RECOMMENDATION_ID)) {
                addBehaviour(new HandleArtifactRecommendation(getAgent(), msg));
            }
        }
    }

    private class HandleArtifactInfoResponse extends OneShotBehaviour {

        private ACLMessage msg;

        HandleArtifactInfoResponse(Agent a, ACLMessage msg) {
            super(a);
            this.msg = msg;
        }

        @Override
        public void action() {
            try {
                Artifact content = (Artifact) msg.getContentObject();
                lookedUpArtifacts.add(content);
                profile.addVisitedItem(content.getId());
                System.out.println("Received artifact info: " + content);
            } catch (UnreadableException ex) {
                Logger.getLogger(ProfilerAgent.class.getName()).log(Level.SEVERE, null, ex);
                block();
            }
        }
    }

    private class HandleArtifactRecommendation extends OneShotBehaviour {

        private ACLMessage msg;

        HandleArtifactRecommendation(Agent a, ACLMessage msg) {
            super(a);
            this.msg = msg;
        }

        @Override
        public void action() {
            Integer content = null;
            try {
                content = (Integer) msg.getContentObject();
                recommendedArtifacts.add(content);
                System.out.println("Agent " + getAID().getName()
                        + ": was recommended artifact with ID=" + content);
            } catch (UnreadableException ex) {
                Logger.getLogger(ProfilerAgent.class.getName()).log(Level.SEVERE, null, ex);
                block();
            }

            // If interested (always atm) ask for information
            addBehaviour(new ArtifactRequestBehaviour(getAgent(), content));
        }
    }

    private class ArtifactRequestBehaviour extends OneShotBehaviour {

        private AID cAgent;
        private Integer artifactId;

        public ArtifactRequestBehaviour(Agent a, Integer artifactId) {
            super(a);
            this.artifactId = artifactId;
            DFAgentDescription dfd = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("service");
            dfd.addServices(sd);
            AID[] aids = DFUtilities.searchDF(a, dfd);
            if (aids.length < 1) {
                throw new RuntimeException("Cannot find agent");
            }
            cAgent = aids[0];
        }

        private void sendRequest(Integer artifactId) {
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.addReceiver(cAgent);
            msg.setLanguage(ACL_LANGUAGE);
            msg.setOntology(Ontologies.ARTIFACT_REQUEST_INFO);
            try {
                msg.setContentObject(artifactId);
                send(msg);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void action() {
            sendRequest(artifactId);
        }

    }

	public Serializable getGenre() {
		// TODO Auto-generated method stub
		return null;
	}
}
