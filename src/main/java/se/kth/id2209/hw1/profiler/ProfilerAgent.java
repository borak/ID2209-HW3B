/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.id2209.hw1.profiler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.DataStore;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.proto.states.MsgReceiver;
import se.kth.id2209.hw1.exhibition.Artifact;
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
	private List<Integer> recommendedArtifacts;
	private List<Artifact> lookedUpArtifacts;
	static final String ACL_LANGUAGE = "Java Serialized";

	@SuppressWarnings("serial")
	@Override
	protected void setup() {
		super.setup();
		setRecommendedArtifacts(new ArrayList<Integer>());
		setLookedUpArtifacts(new ArrayList<Artifact>());
		
		List<String> interests = new ArrayList<String>();
		List<Integer> visitedItems = new ArrayList<Integer>();

		interests.add(Artifact.GENRE.PAINTING.toString());

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

	public UserProfile getUserProfile() {
		return profile;
	}

	public List<Artifact> getLookedUpArtifacts() {
		return lookedUpArtifacts;
	}

	public void setLookedUpArtifacts(List<Artifact> lookedUpArtifacts) {
		this.lookedUpArtifacts = lookedUpArtifacts;
	}

	public List<Integer> getRecommendedArtifacts() {
		return recommendedArtifacts;
	}

	public void setRecommendedArtifacts(List<Integer> recommendedArtifacts) {
		this.recommendedArtifacts = recommendedArtifacts;
	}
}
