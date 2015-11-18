package se.kth.id2209.hw1.exhibition;

import java.io.Serializable;
import java.util.ArrayList;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import se.kth.id2209.hw1.exhibition.Artifact.GENRE;
import se.kth.id2209.hw1.profiler.ProfilerAgent;
import se.kth.id2209.hw1.profiler.UserProfile.GENDER;
import se.kth.id2209.hw1.smartmuseum.TourGuideAgent;

/**
 * ïƒ˜ Curator Agent monitors the gallery/museum.
 * ïƒ˜ A gallery/museum contains detailed information of artifacts such as:
 *      ï‚§ id, name, creator, date of creation, place of creation, genre etc.
 * 
 * TODO: Implement behaviours. 
 *       Behaviors should correspond to each category below:
 *          ï�± Simple Behavior (at least 5 different behaviors):
 *              â€“ CyclicBehaviour, MsgReceiver, OneShotBehaviour,
 *                SimpleAchieveREInitiator, SimpleAchieveREResponder,
 *                TickerBehaviour, WakerBehaviour
 *          ï�± Composite Behaviors (at least 2 different behaviors):
 *              â€“ ParallelBehaviour, FSMBehaviour, SequentialBehaviour
 * 
 * @author Kim
 */

@SuppressWarnings("serial")
public class CuratorAgent extends Agent {
	private ProfilerAgent pAgent; // temporary - register at DF instead
	private TourGuideAgent tgAgent; // temporary - register at DF instead
	private ArtGallery artGallery;
	private final static int DB_CHECKER_DELAY = 60000;

	protected void setup() {
		artGallery = artGallery.getInstance();

		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());

		ServiceDescription sd = new ServiceDescription();
		sd.setType("Curator-agent");
		sd.setName(getLocalName());
		dfd.addServices(sd);

		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}

		ParallelBehaviour pbr = new ParallelBehaviour(this,
				ParallelBehaviour.WHEN_ALL);
		pbr.addSubBehaviour(new ListenerBehaviour(this));
		pbr.addSubBehaviour(new DatabaseChecker(this, DB_CHECKER_DELAY));
		addBehaviour(pbr);
	}

	private class DatabaseChecker extends WakerBehaviour {

		private static final String DB_PATH = "src/main/resources/db.txt";

		public DatabaseChecker(Agent a, long timeout) {
			super(a, timeout);
		}

		@Override
		public void onWake() {
			try {
				File file = new File(DB_PATH);
				List<String> lines = Files.readAllLines(file.toPath());
				for(String s : lines) {
					System.out.println(new Artifact(s));
				}
			} catch (IOException ex) {
				Logger.getLogger(CuratorAgent.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	public Artifact getArtifact(int id) {
		return artGallery.getArtifact(id);
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

	public ArrayList<Integer> getArtifactIdList(GENRE genre) {		
		return artGallery.getArtifactIdList(genre);
	}
	
	
	public ArrayList<String> getArtifactNameList(GENRE genre) {		
		return artGallery.getArtifactNameList(genre);
	}

}
