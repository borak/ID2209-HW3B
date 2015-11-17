/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.id2209.hw1.smartmuseum;

import java.util.ArrayList;
import java.util.Iterator;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import se.kth.id2209.hw1.exhibition.CuratorAgent;
import se.kth.id2209.hw1.profiler.ProfilerAgent;
import se.kth.id2209.hw1.util.Ontologies;

/**
 * Tour Guide Agent retrieves the information about artifacts in the
 * gallery/museum and builds a virtual tour (upon the request) for profiler 
 * agent.
 * 
 * –The virtual tour contains list of related items (based on user’s interest, 
 * age, etc..)
 * 
 *  Tour Guide agent interacts with Curator Agent in order to build the virtual 
 * tour.
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
 * @author Kim
 */
public class TourGuideAgent extends Agent {
	private static final long serialVersionUID = 5883088851872677769L;
	private CuratorAgent cAgent; // temporary - register at DF instead
	private Tour tour;

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
	}

	void createTour() {
		tour = new Tour(this);
	}

	void deleteTour() {
		tour = null;
	}

	public void joinTour(ProfilerAgent profiler) {
		tour.addProfiler(profiler);
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
	
	class PresentBehaviour extends TickerBehaviour {
		ACLMessage msg;
		int itemId = 0;
		public PresentBehaviour(Agent a, long period) {
			super(a, period);
		}

		@Override
		protected void onTick() {
			ArrayList<ProfilerAgent> profilers = tour.getProfilers();
			Iterator<ProfilerAgent> it = profilers.iterator();
		
			while(it.hasNext()) {
				ProfilerAgent profiler = it.next();
				//msg.setContentObject(tour.getArtifacts().get(itemId));
				//profiler.send(arg0);
			}
			
			System.out.println(myAgent.getLocalName() + " checking for items");
			ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);			
			msg.addReceiver(cAgent.getAID());
			msg.setOntology(Ontologies.ARTIFACT_RECOMMENDATION);
			
			send(msg);		
			itemId++;
		}
		
	}
}
