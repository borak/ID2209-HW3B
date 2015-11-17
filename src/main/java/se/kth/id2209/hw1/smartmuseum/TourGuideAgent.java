/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.id2209.hw1.smartmuseum;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.states.MsgReceiver;
import se.kth.id2209.hw1.exhibition.Artifact;
import se.kth.id2209.hw1.exhibition.CuratorAgent;
import se.kth.id2209.hw1.profiler.ProfilerAgent;
import se.kth.id2209.hw1.profiler.UserProfile;
import util.Ontologies;

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
	private HashMap<ProfilerAgent, List<String>> map = new HashMap<>();

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
		par.addSubBehaviour(new MsgReceiver() {
			// TODO: MAKE !
		});
	}

	// TODO: se till att denna kallas då en profiler vill starta en tour
	@SuppressWarnings("serial")
	void startTour() {

		SequentialBehaviour seq = new SequentialBehaviour();
		seq.addSubBehaviour(new OneShotBehaviour() {

			@Override
			public void action() {				
				Iterator<Entry<ProfilerAgent, List<String>>> it = map.entrySet().iterator();
				ACLMessage msg = null;

				while(it.hasNext()) {
					Entry<ProfilerAgent, List<String>> entry = it.next();
					ProfilerAgent profiler = entry.getKey();
					try {
						msg.addReceiver(cAgent.getAID()); // TODO ANVÄND DF	
						msg.setOntology(Ontologies.ARTIFACT_RECOMMENDATION_NAME);			
						msg.setContentObject((Serializable) profiler.getGenre());
						send(msg);

						ACLMessage recMsg = blockingReceive(); // should block until response

						List<String> list = (List<String>) recMsg.getContentObject(); 
						map.put(profiler, list);
					} catch (Exception e) {
						e.printStackTrace();
					}				
				}
			}
		});
		seq.addSubBehaviour(new PresentBehaviour(this, 10000));

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
		private Iterator<Entry<ProfilerAgent, List<String>>> it = map.entrySet().iterator();			
		private int index;		

		public PresentBehaviour(Agent a, long period) {
			super(a, period);
			index = 0;
		}

		@Override
		protected void onTick() {			
			ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
			Entry<ProfilerAgent, List<String>> entry = it.next();
			ProfilerAgent profiler = entry.getKey();
			List<String> names = entry.getValue();

			if(index > names.size()) {
				System.out.println(profiler.getName() + ": Your tour is finished, get out of here!");
				map.remove(profiler);
			} else {
				String name = names.get(index);

				msg.addReceiver(profiler.getAID());
				try {
					msg.setContentObject(name);
					send(msg);

					System.out.println(myAgent + ": presenting item " + name + " to " + profiler);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			index++; 
		}			
	}
}
