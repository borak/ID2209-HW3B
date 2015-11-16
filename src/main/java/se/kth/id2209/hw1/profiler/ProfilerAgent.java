/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.id2209.hw1.profiler;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import java.util.List;
import se.kth.id2209.hw1.exhibition.CuratorAgent;
import se.kth.id2209.hw1.smartmuseum.TourGuideAgent;

/**
 * Profiler Agent travels around the network and looks for interesting
 * information about art and culture from online museums or art galleries on the
 * internet.
 * 
 *  The Profiler Agent interacts directly with Tour Guide Agent to get a 
 *   personalized virtual tour.
 *  The Profile Agent interacts with Curator Agent to obtain detailed 
 *   information about each of the items stated in the virtual tour.
 * 
 * TODO: Implement behaviours. 
 *       Behaviors should correspond to each category below:
 *           Simple Behavior (at least 5 different behaviors):
 *              – CyclicBehaviour, MsgReceiver, OneShotBehaviour,
 *                SimpleAchieveREInitiator, SimpleAchieveREResponder,
 *                TickerBehaviour, WakerBehaviour
 *           Composite Behaviors (at least 2 different behaviors):
 *              – ParallelBehaviour, FSMBehaviour, SequentialBehaviour
 * 
 * 
 * @author Kim
 */
public class ProfilerAgent extends Agent {
    private UserProfile profile;
    private CuratorAgent cAgent; // temporary - register at DF instead
    private TourGuideAgent tgAgent; // temporary - register at DF instead
    
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

        addBehaviour(new TestBehaviour());
    }

    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        //myGui.dispose();
        System.out.println("Agent " + getAID().getName() +" is terminating.");
    }

    // Example of a behaviour skeleton. Remove and recreate as its own class 
    // outside of this class.
    private class TestBehaviour extends Behaviour {

        @Override
        public void action() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean done() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }
}
