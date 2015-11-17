/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.id2209.hw1.profiler;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import se.kth.id2209.hw1.exhibition.Artifact;
import se.kth.id2209.hw1.exhibition.CuratorAgent;
import se.kth.id2209.hw1.smartmuseum.TourGuideAgent;
import util.DFUtilities;

/**
 * Profiler Agent travels around the network and looks for interesting
 * information about art and culture from online museums or art galleries on the
 * internet.
 *
 *  The Profiler Agent interacts directly with Tour Guide Agent to get a
 * personalized virtual tour.  The Profile Agent interacts with Curator Agent
 * to obtain detailed information about each of the items stated in the virtual
 * tour.
 *
 * TODO: Implement behaviours. Behaviors should correspond to each category
 * below:  Simple Behavior (at least 5 different behaviors): – CyclicBehaviour,
 * MsgReceiver, OneShotBehaviour, SimpleAchieveREInitiator,
 * SimpleAchieveREResponder, TickerBehaviour, WakerBehaviour  Composite
 * Behaviors (at least 2 different behaviors): – ParallelBehaviour,
 * FSMBehaviour, SequentialBehaviour
 *
 *
 * @author Kim
 */
public class ProfilerAgent extends Agent {

    private UserProfile profile;
    private CuratorAgent cAgent; // temporary - register at DF instead
    private TourGuideAgent tgAgent; // temporary - register at DF instead
    private List<Artifact> artifacts;

    @Override
    protected void setup() {
        super.setup();
        //profile = new UserProfile(...);
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

        addBehaviour(new ArtifactRequestBehaviour(this, 5000));
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

    public void addArtifacts(List<Artifact> artifacts) {
        if (this.artifacts == null || this.artifacts.isEmpty()) {
            this.artifacts = artifacts;
        } else {
            this.artifacts.addAll(artifacts);
        }
    }

    // Example of a behaviour skeleton. Remove and recreate as its own class 
    // outside of this class.
    private class ArtifactRequestBehaviour extends TickerBehaviour {

        private AID cAgent;

        public ArtifactRequestBehaviour(Agent a, long period) {
            super(a, period);
            DFAgentDescription dfd = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("service");
            dfd.addServices(sd);
            SearchConstraints ALL = new SearchConstraints();
            ALL.setMaxResults(new Long(-1));
            AID[] aids = DFUtilities.searchDF(a, dfd);
            if (aids.length < 1) {
                throw new RuntimeException("Cannot find agent");
            }
            cAgent = aids[0];
        }

        private void sendRequest(AID name) {
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.addReceiver(name);
            msg.setLanguage("Java Serialized");
            msg.setOntology("sup dawg");
            try {
                msg.setContentObject(null);
                send(msg);
            } catch (IOException ex) {
                
            }
        }

        @Override
        protected void onTick() {
            ProfilerAgent agent = (ProfilerAgent) getAgent();
            //List<Artifact> artifacts = cAgent.addBehaviour(new RequestPerformer());
            //agent.notifyProducts(artifacts);
        }

    }
}
