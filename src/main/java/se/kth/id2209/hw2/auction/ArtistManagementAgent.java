package se.kth.id2209.hw2.auction;

import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.ProfileImpl;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.core.Runtime;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import jade.domain.JADEAgentManagement.QueryPlatformLocationsAction;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import se.kth.id2209.hw2.exhibition.Artifact;
import se.kth.id2209.hw2.exhibition.CuratorAgent;
import se.kth.id2209.hw2.util.DFUtilities;

/**
 * The Artist Management Agent auctions out artifacts to any number of Curator Agents.
 * It starts the Dutch auction, adds some artifacts to sell, and sets up delays for the start
 *   of the auction and the first CFP.
 */
public class ArtistManagementAgent extends Agent {

    public static final String DF_NAME = "Artist-management-agent";
    private final Map<Integer, Auction> auctions = new HashMap();
    final Lock auctionsLock = new ReentrantLock();
    private static final int auctionsStartDelay = 3000;
    private static final int auctionsCFPDelay = 8000; 
    private final List<AID> bidders = new ArrayList();


    @Override
    protected void setup() {
        registerService();
        initAuctions();
        for(AID aid : fetchBidders()) {
            bidders.add(aid);
        }
        Runtime runtime = Runtime.instance();
        ProfileImpl p1 = new ProfileImpl();
        p1.setParameter("container-name", "auctioneer-Agent-Container");

        AgentContainer artistManagementContainer =  runtime.createAgentContainer(p1);


//        this.doMove();

//        containers[1] =  runtime.createAgentContainer(new ProfileImpl());
//        try
//        {
//            AgentController ac = artistManagementContainer.createNewAgent("testaren","se.kth.id2209.hw2.exhibition.CuratorAgent", null);
//            containers[0].start();
//            ac.start();

//        } catch (ControllerException e)
//        {
//            e.printStackTrace();
//        }


//        sendRequest(new Action(getAMS(), new QueryPlatformLocationsAction()));

        ParallelBehaviour pbr = new ParallelBehaviour(this,
                ParallelBehaviour.WHEN_ALL);
        pbr.addSubBehaviour(new DutchAuctioneerBehaviour(this, auctions));
        pbr.addSubBehaviour(new WakerBehaviour(this, auctionsStartDelay) {
            @Override
            public void onWake() {
                auctionsLock.lock();
                try {
                    for(Auction auc : auctions.values()) {
                        ArtistManagementAgent.this.addBehaviour(
                                new InformStartOfAuctionBehaviour(auc,
                                        ArtistManagementAgent.this, bidders)); 
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
                    for(Auction auc : auctions.values()) {
                        ArtistManagementAgent.this.addBehaviour(
                                new CFPBehaviour(auc,
                                        ArtistManagementAgent.this, bidders)); 
                    }
                } finally {
                    auctionsLock.unlock();
                }
            }
        });
        addBehaviour(pbr);
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
        for(int i=0; i<6; i++) {
            prices[i] = random.nextInt(40000) + 200;
            minprices[i] = (int)((double)prices[i] * (random.nextInt(4)/10 + 0.1));
        }
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
        }
    }

    AID[] fetchBidders() {
        return DFUtilities.searchAllDF(this, CuratorAgent.DF_NAME);
    }
}
