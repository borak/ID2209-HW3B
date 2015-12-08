package se.kth.id2209.hw2.auction;

import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.content.onto.basic.Result;
import jade.core.AID;
import jade.core.Agent;
import jade.core.Location;
import jade.core.ProfileImpl;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.core.Runtime;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPANames.ContentLanguage;
import jade.domain.FIPANames.InteractionProtocol;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.JADEAgentManagement.QueryAgentsOnLocation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import jade.domain.JADEAgentManagement.QueryPlatformLocationsAction;
import jade.domain.mobility.MobilityOntology;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentContainer;
import se.kth.id2209.hw2.exhibition.Artifact;
import se.kth.id2209.hw2.exhibition.AuctionListenerBehaviour;
import se.kth.id2209.hw2.exhibition.CuratorAgent;
import se.kth.id2209.hw2.util.DFUtilities;
import se.kth.id2209.hw2.util.MobilityListener;
import se.kth.id2209.hw2.util.SList;

/**
 * The Artist Management Agent auctions out artifacts to any number of Curator
 * Agents. It starts the Dutch auction, adds some artifacts to sell, and sets up
 * delays for the start of the auction and the first CFP.
 */
public class ArtistManagementAgent extends Agent {

    public static final String DF_NAME = "Artist-management-agent";
    private final Map<Integer, Auction> auctions = new HashMap();
    final Lock auctionsLock = new ReentrantLock();
    private static final int biddersLookupDelay = 0;
    private static final int auctionsStartDelay = 1000;
    private static final int auctionsCFPDelay = 3000;
    private final SList<AID> bidders = new SList();
    private final Lock bidderLock = new ReentrantLock();
    private final Map<String, Location> containerMap = new HashMap();
    private final static String ARTIST_CONTAINER_NAME = "auctioneer-Agent-Container";
    public final static String AUCTION1_CONTAINER_NAME = "auction-1-Container";
    public final static String AUCTION2_CONTAINER_NAME = "auction-2-Container";
    private Location home;

    @Override
    protected void setup() {
        registerService();
        initAuctions();
        bidderLock.lock();
        try {
            for (AID aid : fetchBidders()) {
                bidders.add(aid);
            }
        } finally {
            bidderLock.unlock();
        }
        Runtime runtime = Runtime.instance();
        ProfileImpl p1 = new ProfileImpl();
        p1.setParameter("container-name", ARTIST_CONTAINER_NAME);
        final AgentContainer curatorContainer = runtime.createAgentContainer(p1);
        ProfileImpl p2 = new ProfileImpl();
        p2.setParameter("container-name", AUCTION1_CONTAINER_NAME);
        final AgentContainer auction1Container = runtime.createAgentContainer(p2);
        ProfileImpl p3 = new ProfileImpl();
        p3.setParameter("container-name", AUCTION2_CONTAINER_NAME);
        final AgentContainer auction2Container = runtime.createAgentContainer(p3);



        getContentManager().registerOntology(MobilityOntology.getInstance());
        getContentManager().registerOntology(JADEManagementOntology.getInstance());
        getContentManager().registerLanguage(new SLCodec(), FIPANames.ContentLanguage.FIPA_SL);



        final SequentialBehaviour sb = new SequentialBehaviour();
        sb.addSubBehaviour(new WakerBehaviour(this, biddersLookupDelay) {
            @Override
            public void onWake() {
                requestContainers();
                System.out.println("ARTIST ::::: REQUEST FOR CONTAINERS SENT.");
            }
        });
        sb.addSubBehaviour(new MobilityListener(this, containerMap));
        sb.addSubBehaviour(new OneShotBehaviour(this) { // moves itself
            @Override
            public void action() {
                final Location dest = (Location) containerMap.get(ARTIST_CONTAINER_NAME);
                final Location cloneDest1 = (Location) containerMap.get(AUCTION1_CONTAINER_NAME);
                final Location cloneDest2 = (Location) containerMap.get(AUCTION2_CONTAINER_NAME);
                System.out.println("ARTIST ::::: ATTEMPTING MOVING from=" + here() + " to " + dest);
                if (dest != null) {
                    myAgent.addBehaviour(new OneShotBehaviour(myAgent) {
                        @Override
                        public void action() {
                            doMove(dest);
                            home = dest;
                        }
                    });
                    SequentialBehaviour seq = new SequentialBehaviour();
                    seq.addSubBehaviour(new OneShotBehaviour(myAgent) {

                        @Override
                        public void action() {
                            ArtistManagementAgent.this.doClone(cloneDest1, getLocalName() + "_clone1");
                        }
                    });
                    seq.addSubBehaviour(new OneShotBehaviour(myAgent) {

                        @Override
                        public void action() {
                            ArtistManagementAgent.this.doClone(cloneDest2, getLocalName() + "_clone2");
                        }
                    });
                    addBehaviour(seq);
                }
            }
        });
        addBehaviour(sb);
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

    private void registerService() {
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

    private void initAuctions() {
        Artifact art1 = new Artifact(432, "artifact432", "unknown", "unknown",
                "unknown", Artifact.GENRE.PAINTING,
                Artifact.Quality.UNKNOWN_QUALITY);
        Artifact art2 = new Artifact(743, "artifact743", "unknown", "unknown",
                "unknown", Artifact.GENRE.JEWELRY,
                Artifact.Quality.UNKNOWN_QUALITY);
        Artifact art3 = new Artifact(344, "artifact344", "unknown", "unknown",
                "unknown", Artifact.GENRE.FASHION,
                Artifact.Quality.UNKNOWN_QUALITY);
        Artifact art4 = new Artifact(888, "artifact888", "unknown", "unknown",
                "unknown", Artifact.GENRE.MUSIC,
                Artifact.Quality.UNKNOWN_QUALITY);
        Artifact art5 = new Artifact(777, "artifact777", "unknown", "unknown",
                "unknown", Artifact.GENRE.PAINTING,
                Artifact.Quality.UNKNOWN_QUALITY);
        Artifact art6 = new Artifact(999, "artifact999", "unknown", "unknown",
                "unknown", Artifact.GENRE.SCULPTURE,
                Artifact.Quality.UNKNOWN_QUALITY);

        Random random = new Random();
        int[] prices = new int[6];
        int[] minprices = new int[6];
        for (int i = 0; i < 6; i++) {
            prices[i] = random.nextInt(40000) + 200;
            minprices[i] = (int) ((double) prices[i] * (random.nextInt(4) / 10 + 0.1));
        }
        bidderLock.lock();
        auctionsLock.lock();
        try {
            auctions.put(art1.getId(), new Auction(bidders, prices[0], minprices[0],
                    art1, false, Artifact.Quality.HIGH_QUALITY));
            auctions.put(art2.getId(), new Auction(bidders, prices[1], minprices[1],
                    art2, false, Artifact.Quality.LOW_QUALITY));
            auctions.put(art3.getId(), new Auction(bidders, prices[2], minprices[2],
                    art3, false, Artifact.Quality.LOW_QUALITY));
            auctions.put(art4.getId(), new Auction(bidders, prices[3], minprices[3],
                    art4, false, Artifact.Quality.LOW_QUALITY));
            auctions.put(art5.getId(), new Auction(bidders, prices[4], minprices[4],
                    art5, false, Artifact.Quality.HIGH_QUALITY));
            auctions.put(art6.getId(), new Auction(bidders, prices[5],
                    minprices[5], art6, false, Artifact.Quality.HIGH_QUALITY));
        } finally {
            auctionsLock.unlock();
            bidderLock.unlock();
        }
    }

    AID[] fetchBidders() {
        return DFUtilities.searchAllDF(this, CuratorAgent.DF_NAME);
    }

    private jade.util.leap.List fetchBiddersFromOtherContainers() {
        QueryAgentsOnLocation agentAction = new QueryAgentsOnLocation();
        agentAction.setLocation(here());
        Action newAction = new Action(getAMS(), agentAction);
        ACLMessage agentRequest = new ACLMessage(ACLMessage.REQUEST);
        agentRequest.addReceiver(getAMS());
        agentRequest.setOntology(JADEManagementOntology.getInstance().getName());
        agentRequest.setLanguage(ContentLanguage.FIPA_SL);
        agentRequest.setProtocol(InteractionProtocol.FIPA_REQUEST);

        try {
            getContentManager().fillContent(agentRequest, newAction);
            send(agentRequest);
            ACLMessage receivedMessage = blockingReceive(MessageTemplate.MatchSender(getAMS()));
            Result r = (Result) getContentManager().extractContent(receivedMessage);
            jade.util.leap.List list = (jade.util.leap.List) r.getValue();
            return list;
        } catch (Codec.CodecException | OntologyException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public void afterClone() {
        if(getName().split("clone").length >= 3) {
            System.err.println("DELETING CLONE " + getName());
            try {
                doDelete();
            } catch(Exception e) {

            }
        } else {
            getContentManager().registerOntology(MobilityOntology.getInstance());
            getContentManager().registerOntology(JADEManagementOntology.getInstance());
            getContentManager().registerLanguage(new SLCodec(), FIPANames.ContentLanguage.FIPA_SL);

            ParallelBehaviour pbr = new ParallelBehaviour(this,
                    ParallelBehaviour.WHEN_ALL);
            pbr.addSubBehaviour(new DutchAuctioneerBehaviour(this, auctions));
            pbr.addSubBehaviour(new WakerBehaviour(this, auctionsStartDelay) {
                @Override
                public void onWake() {
                    auctionsLock.lock();
                    try {
                        int count = 0;
                        for (Auction auc : auctions.values()) {
                            ArtistManagementAgent.this.addBehaviour(
                                    new InformStartOfAuctionBehaviour(auc,
                                            ArtistManagementAgent.this, bidders));
                                    count++;
                                    if(count ==2)
                                    {
                                        break;
                                    }
                        }
                    } finally {
                        auctionsLock.unlock();
                    }
                }
            });
            pbr.addSubBehaviour(new WakerBehaviour(this, auctionsCFPDelay) {
                @Override
                public void onWake() {
                    auctionsLock.lock();
                    try {
                        int count =0;
                        for (Auction auc : auctions.values()) {
                            ArtistManagementAgent.this.addBehaviour(
                                    new CFPBehaviour(auc,
                                            ArtistManagementAgent.this, bidders));
                                count++;
                            if(count==2)
                            {
                                break;
                            }
                        }
                    } finally {
                        auctionsLock.unlock();
                    }
                }
            });

            final SequentialBehaviour sb = new SequentialBehaviour();
            sb.addSubBehaviour(new WakerBehaviour(this, biddersLookupDelay) {
                @Override
                public void onWake() {
                    requestContainers();
                    System.out.println("ARTIST ::::: REQUEST FOR CONTAINERS SENT.");
                }
            });
            sb.addSubBehaviour(new MobilityListener(this, containerMap));

            sb.addSubBehaviour(new OneShotBehaviour(this) { // find the curators
                @Override
                public void action() {
                    jade.util.leap.List result = fetchBiddersFromOtherContainers();
                    bidderLock.lock();
                    try {
                        for (Object o : result.toArray()) {
                            if(((AID) o).getName().contains("curator")) {
                                bidders.add((AID) o);
                            }
                        }
                        System.out.println("ARTIST ::::: AGENTS FETCHED FROM CONTAINERS ="
                                + result.size() + " BIDDERS =" + bidders.size());
                    } finally {
                        bidderLock.unlock();
                    }
                }
            });
            sb.addSubBehaviour(pbr);
            addBehaviour(sb);
        }
    }
}
