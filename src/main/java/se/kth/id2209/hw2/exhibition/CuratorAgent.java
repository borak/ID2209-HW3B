package se.kth.id2209.hw2.exhibition;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.*;
import jade.core.Runtime;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPANames;
import jade.domain.FIPANames.ContentLanguage;
import jade.domain.FIPANames.InteractionProtocol;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.JADEAgentManagement.QueryPlatformLocationsAction;
import jade.domain.mobility.MobileAgentDescription;
import jade.domain.mobility.MobilityOntology;
import jade.domain.mobility.MoveAction;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentContainer;
import se.kth.id2209.hw2.exhibition.Artifact.GENRE;

/**
 * An Curator Agent monitors the art gallery of the museum. It will provide
 * responses with the information about the various artifacts after appropriate
 * requests made by other agents.
 *
 */
@SuppressWarnings("serial")
public class CuratorAgent extends Agent {

    private ArtGallery artGallery;
    private final static int DB_CHECKER_DELAY = 100;
    public static final String DF_NAME = "Curator-agent";
    private static int curatorId;
    private Map containerMap = new HashMap();
    private AgentContainer currentContainer = null;
    private String containerName;

    /**
     * Initializes its state and the ArtGallery by checking and parsing a
     * database of the artifacts and by adding a message listener for the agents
     * incomming message requests.
     */
    @Override
    protected void setup() {
        curatorId = UniqueCuratorIdGiver.createUniqueId();
        artGallery = ArtGallery.getInstance();
        containerName = getLocalName() + "-Agent-Container";

        getContentManager().registerOntology(MobilityOntology.getInstance());
        getContentManager().registerOntology(JADEManagementOntology.getInstance());
        getContentManager().registerLanguage(new SLCodec(), FIPANames.ContentLanguage.FIPA_SL);

        Runtime runtime = Runtime.instance();
        ProfileImpl p = new ProfileImpl();
        p.setParameter("container-name", containerName);
        AgentContainer curatorContainer = runtime.createAgentContainer(p);
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                requestContainers();

            }
        });

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());

        ServiceDescription sd = new ServiceDescription();
        sd.setType(DF_NAME);
        sd.setName(getLocalName() + curatorId);
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        ParallelBehaviour pbr = new ParallelBehaviour(this,
                ParallelBehaviour.WHEN_ALL);
        pbr.addSubBehaviour(new ArtifactListenerBehaviour(this));
        pbr.addSubBehaviour(new AuctionListenerBehaviour(this));
        if (curatorId == UniqueCuratorIdGiver.FIRST_ID) {
            pbr.addSubBehaviour(new DatabaseChecker(this, DB_CHECKER_DELAY));
        }
        SequentialBehaviour sb = new SequentialBehaviour();
        sb.addSubBehaviour(new MobilityListener(this, containerMap));
        sb.addSubBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                //flytta, klona, faster, stronger, better
//                doClone((ContainerID)containerMap.get(index), getLocalName() + "_clone" + index);

                //flytta
                AID aid = new AID(getName(), AID.ISLOCALNAME);
                Location dest = (Location) containerMap.get(containerName);
                if (dest != null) {
                    doMove(dest);
                }

                String destName = dest.getName();
                MobileAgentDescription mad = new MobileAgentDescription();
                mad.setName(aid);
                mad.setDestination(dest);
                MoveAction ma = new MoveAction();
                ma.setMobileAgentDescription(mad);
                Action action = new Action(aid, ma);
                ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
                request.addReceiver(getAMS());
                request.setOntology(JADEManagementOntology.getInstance().getName());
                request.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
                request.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);

                try {
                    getContentManager().fillContent(request, action);
                } catch (Codec.CodecException e) {
                    e.printStackTrace();
                } catch (OntologyException e) {
                    e.printStackTrace();
                }
                send(request);

                //clone
            }
        });
        sb.addSubBehaviour(pbr);
        addBehaviour(sb);
    }

    int getCuratorId() {
        return curatorId;
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

    private void requestContainers() {
        //Register the SL content language

        //Register the mobility ontology
//        getContentManager().registerOntology(JADEManagementOntology.getInstance());
        // Send a request to the AMS to obtain the Containers
        Action action = new Action(getAMS(), new QueryPlatformLocationsAction());
        ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
        request.addReceiver(getAMS());
        request.setOntology(JADEManagementOntology.getInstance().getName());
        request.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
        request.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);

        try {
            getContentManager().fillContent(request, action);
        } catch (Codec.CodecException e) {
            e.printStackTrace();
        } catch (OntologyException e) {
            e.printStackTrace();
        }
        send(request);
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

    public ArrayList<Integer> getArtifactIdList(ArrayList<GENRE> genres) {
        ArrayList<Integer> artIds = new ArrayList<Integer>();
        for (GENRE genre : genres) {
            artIds.addAll(artGallery.getArtifactIdList(genre));
        }
        return artIds;
    }

}
