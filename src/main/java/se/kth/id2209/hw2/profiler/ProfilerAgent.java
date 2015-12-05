package se.kth.id2209.hw2.profiler;

import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.content.onto.basic.Result;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jade.core.AID;
import jade.core.Agent;
import jade.core.Location;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.DataStore;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPANames;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.JADEAgentManagement.QueryAgentsOnLocation;
import jade.domain.JADEAgentManagement.QueryPlatformLocationsAction;
import jade.domain.mobility.MobilityOntology;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.states.MsgReceiver;
import java.util.HashMap;
import java.util.Map;
import se.kth.id2209.hw2.exhibition.Artifact;
import se.kth.id2209.hw2.exhibition.CuratorAgent;
import se.kth.id2209.hw2.smartmuseum.TourGuideAgent;
import static se.kth.id2209.hw2.smartmuseum.TourGuideAgent.CA_DF_NAME;
import se.kth.id2209.hw2.util.DFUtilities;
import se.kth.id2209.hw2.util.MobilityListener;
import se.kth.id2209.hw2.util.Ontologies;

/**
 * A Profiler Agent travels around the network and looks for interesting
 * information about art and culture from online museums or art galleries on the
 * internet. This is done by interacting with a Tour Guide Agent to get a
 * personalized virtual tour and a Curator Agent to obtain detailed information
 * about each of the items stated in the virtual tour.
 *
 * @author Kim
 */
@SuppressWarnings("serial")
public class ProfilerAgent extends Agent {

    private UserProfile profile;
    private List<Integer> recommendedArtifacts;
    private List<Artifact> lookedUpArtifacts;
    static final String ACL_LANGUAGE = "Java Serialized";
    public static final String DF_NAME = "Profiler-agent";
    private final Map<String, Location> containerMap = new HashMap();
    private AID curatorAgent;

    /**
     * Creates a user profile and registers itself to the DFService. Starts
     * sending requests for a tour.
     */
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
        
        getContentManager().registerOntology(MobilityOntology.getInstance());
        getContentManager().registerOntology(JADEManagementOntology.getInstance());
        getContentManager().registerLanguage(new SLCodec(), FIPANames.ContentLanguage.FIPA_SL);
        
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());

        ServiceDescription sd = new ServiceDescription();
        sd.setType(DF_NAME);
        sd.setName(getLocalName());
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        SequentialBehaviour seq = new SequentialBehaviour();
        seq.addSubBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                requestContainers();
                System.out.println("PROFILER ::::: REQUEST FOR CONTAINERS SENT.");
            }
        });
        seq.addSubBehaviour(new MobilityListener(this, containerMap));
        seq.addSubBehaviour(new WakerBehaviour(this, 1000) { 
            @Override
            public void onWake() {
                curatorAgent = DFUtilities.searchDF(ProfilerAgent.this, CuratorAgent.DF_NAME);
                if(curatorAgent == null) {
                    curatorAgent = fetchCuratorFromAnotherContainer();
                }
                System.out.println("PROFILER's ATTEMPT TO FETCH CURATOR = " + curatorAgent);
            }
        });
        seq.addSubBehaviour(new SendTourGuideRequestBehaviour(this, profile));
        seq.addSubBehaviour(new MsgReceiverBehaviour(ProfilerAgent.this, null, MsgReceiver.INFINITE,
                        new DataStore(), null));
        addBehaviour(seq);
    }

    /**
     * Deregisters itself from the DFService.
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

    UserProfile getUserProfile() {
        return profile;
    }

    List<Artifact> getLookedUpArtifacts() {
        return lookedUpArtifacts;
    }

    void setLookedUpArtifacts(List<Artifact> lookedUpArtifacts) {
        this.lookedUpArtifacts = lookedUpArtifacts;
    }

    List<Integer> getRecommendedArtifacts() {
        return recommendedArtifacts;
    }

    void setRecommendedArtifacts(List<Integer> recommendedArtifacts) {
        this.recommendedArtifacts = recommendedArtifacts;
    }
    
    private AID fetchCuratorFromAnotherContainer() {
        QueryAgentsOnLocation agentAction = new QueryAgentsOnLocation();
        agentAction.setLocation(here());
        Action newAction = new Action(getAMS(), agentAction);
        ACLMessage agentRequest = new ACLMessage(ACLMessage.REQUEST);
        agentRequest.addReceiver(getAMS());
        agentRequest.setOntology(JADEManagementOntology.getInstance().getName());
        agentRequest.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
        agentRequest.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);

        try {
            getContentManager().fillContent(agentRequest, newAction);
            send(agentRequest);
            
            ACLMessage receivedMessage = blockingReceive(MessageTemplate.MatchSender(getAMS()));
            Result r = (Result) getContentManager().extractContent(receivedMessage);
            jade.util.leap.List list = (jade.util.leap.List) r.getValue();
            return (AID)list.iterator().next();
        } catch (Codec.CodecException | OntologyException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    AID getCurator() {
        return curatorAgent;
    }
    
    private void requestContainers() {
        // Send a request to the AMS to obtain the Containers
        Action action = new Action(getAMS(), new QueryPlatformLocationsAction());
        ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
        request.addReceiver(getAMS());
        request.setOntology((MobilityOntology.getInstance().getName()));
        request.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
        request.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);

        try {
            getContentManager().fillContent(request, action);
        } catch (Codec.CodecException | OntologyException e) {
            //e.printStackTrace();
        }
        send(request);
    }
}
