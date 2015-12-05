package se.kth.id2209.hw2.smartmuseum;

import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.content.onto.basic.Result;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jade.core.AID;
import jade.core.Agent;
import jade.core.Location;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.DataStore;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.ParallelBehaviour;
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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import se.kth.id2209.hw2.exhibition.Artifact;
import se.kth.id2209.hw2.profiler.UserProfile;
import se.kth.id2209.hw2.util.DFUtilities;
import se.kth.id2209.hw2.util.MobilityListener;
import se.kth.id2209.hw2.util.Ontologies;

/**
 * Tour Guide Agent retrieves the information about artifacts in the
 * gallery/museum and builds a virtual tour (upon the request) for profiler
 * agent.
 *
 * @author Kim
 */
public class TourGuideAgent extends Agent {

    private HashMap<AID, UserProfile> users = new HashMap<>();
    private TGAMsgReceiverBehaviour msgReceiver;
    private Map<String, AID> requests = new HashMap<String, AID>();
    private Map<AID, List<ACLMessage>> responses = new HashMap<AID, List<ACLMessage>>();
    Lock usersLock = new ReentrantLock();
    public static final String DF_NAME = "Tour-Guide-agent";
    public static final String CA_DF_NAME = "Curator-agent";
    private final Map<String, Location> containerMap = new HashMap();
    private AID curatorAgent;

    /**
     * Registers itself to the DFService and starts listening for incomming tour
     * requests and artifact query responses.
     */
    @Override
    protected void setup() {
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
        
        getContentManager().registerOntology(MobilityOntology.getInstance());
        getContentManager().registerOntology(JADEManagementOntology.getInstance());
        getContentManager().registerLanguage(new SLCodec(), FIPANames.ContentLanguage.FIPA_SL);

        SequentialBehaviour sb = new SequentialBehaviour();
        sb.addSubBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                requestContainers();
                System.out.println("TGA ::::: REQUEST FOR CONTAINERS SENT.");
            }
        });
        sb.addSubBehaviour(new MobilityListener(this, containerMap));
        sb.addSubBehaviour(new WakerBehaviour(this, 1000) { 
            @Override
            public void onWake() {
                curatorAgent = DFUtilities.searchDF(TourGuideAgent.this, CA_DF_NAME);
                if(curatorAgent == null) {
                    curatorAgent = fetchCuratorFromAnotherContainer();
                }
                System.out.println("TGA's ATTEMPT TO FETCH CURATOR = " + curatorAgent);
            }
        });
        
        final ParallelBehaviour par = new ParallelBehaviour(ParallelBehaviour.WHEN_ALL);
        par.addSubBehaviour(new TGAMsgReceiverBehaviour(TourGuideAgent.this,
                null, MsgReceiver.INFINITE, new DataStore(), null));
        par.addSubBehaviour(new PresentingRecommendationsBehaviour(this, 10000));
        sb.addSubBehaviour(par);
        addBehaviour(sb);
    }

    @SuppressWarnings("serial")
    void startTour() {
        System.out.println("TOUR STARTED!");
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                usersLock.lock();
                try {
                    Iterator<Entry<AID, UserProfile>> it = getUsers().entrySet().iterator();
                    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);

                    while (it.hasNext()) {
                        Entry<AID, UserProfile> entry = it.next();
                        AID aid = entry.getKey();
                        UserProfile profile = entry.getValue();
                        List<String> interests = profile.getInterests();
                        System.out.println("interests: " + interests);
                        ArrayList<Artifact.GENRE> genres = new ArrayList<>();
                        for (String s : interests) {
                            genres.add(Artifact.GENRE.valueOf(s));
                        }
                        
                        if (curatorAgent != null) {
                            try {
                                msg.addReceiver(curatorAgent);
                                msg.setOntology(Ontologies.QUERY_ARTIFACTS);
                                msg.setContentObject((Serializable) genres);

                                requests.put(msg.getConversationId(), aid);
                                if (responses.get(aid) == null) {
                                    responses.put(aid, new ArrayList());
                                }

                                System.out.println(myAgent.getName() 
                                        + " SENDING msg: " + msg.getOntology() 
                                        + " to " + curatorAgent.getName());
                                send(msg);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            block();
                        }
                    }
                } finally {
                    usersLock.unlock();
                }
            }
        }
        );
    }

    /**
     * Deregisters itself from DFService.
     */
    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        System.out.println(getAID().getName() + " is terminating.");
    }

    Map<String, AID> getRequests() {
        return requests;
    }

    void setRequests(Map<String, AID> requests) {
        this.requests = requests;
    }

    Map<AID, List<ACLMessage>> getResponses() {
        return responses;
    }

    void setResponses(Map<AID, List<ACLMessage>> responses) {
        this.responses = responses;
    }

    HashMap<AID, UserProfile> getUsers() {
        return users;
    }

    void setUsers(HashMap<AID, UserProfile> users) {
        this.users = users;
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
