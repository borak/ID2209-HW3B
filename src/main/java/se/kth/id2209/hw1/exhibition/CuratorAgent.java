package se.kth.id2209.hw1.exhibition;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jade.core.Agent;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import se.kth.id2209.hw1.exhibition.Artifact.GENRE;

/**
 * An Curator Agent monitors the art gallery of the museum. It will provide
 * responses with the information about the various artifacts after appropriate
 * requests made by other agents.
 *
 *
 * @author Kim
 */
@SuppressWarnings("serial")
public class CuratorAgent extends Agent {

    private ArtGallery artGallery;
    private final static int DB_CHECKER_DELAY = 60000;

    /**
     * Initializes its state and the ArtGallery by checking and parsing a 
     * database of the artifacts and by adding a message listener for the
     * agents incomming message requests.
     */
    protected void setup() {
        artGallery = ArtGallery.getInstance();

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

    /**
     * This behavior is performing the parsing of the database's content as 
     * Artifact objects to the ArtGallery.
     */
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
                for (String s : lines) {
                    System.out.println("New artifact: " + new Artifact(s));
                }
            } catch (IOException ex) {
                Logger.getLogger(CuratorAgent.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public Artifact getArtifact(int id) {
        return artGallery.getArtifact(id);
    }

    /**
     * Deregisters its services from the DFService.
     */
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
