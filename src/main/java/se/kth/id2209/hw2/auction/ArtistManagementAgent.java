package se.kth.id2209.hw2.auction;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import se.kth.id2209.hw2.exhibition.Artifact;
import se.kth.id2209.hw2.exhibition.CuratorAgent;
import se.kth.id2209.hw2.util.DFUtilities;

/**
 *
 * @author Kim
 */
public class ArtistManagementAgent extends Agent {

    public static final String DF_NAME = "Artist-management-agent";
    private final Map<Integer, Auction> auctions = new HashMap();
    final Lock auctionsLock = new ReentrantLock();
    private static final int auctionsStartDelay = 3000;
    private final List<AID> bidders = new ArrayList();

    @Override
    protected void setup() {
        registerService();
        initAuctions();
        fetchBidders();
        
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
        
        auctionsLock.lock();
        try {
            auctions.put(art1.getId(), new Auction(new ArrayList(), 1000, 550, 
                    art1, false, Artifact.Quality.HIGH_QUALITY)); 
            auctions.put(art2.getId(), new Auction(new ArrayList(), 5321, 3800, 
                    art2, false, Artifact.Quality.LOW_QUALITY));
            auctions.put(art3.getId(), new Auction(new ArrayList(), 100, 63, 
                    art3, false, Artifact.Quality.LOW_QUALITY));
            auctions.put(art4.getId(), new Auction(new ArrayList(), 433, 413, 
                    art4, false, Artifact.Quality.LOW_QUALITY));
            auctions.put(art5.getId(), new Auction(new ArrayList(), 60, 40, 
                    art5, false, Artifact.Quality.HIGH_QUALITY));
            auctions.put(art6.getId(), new Auction(new ArrayList(), 40200, 
                    28000, art6, false, Artifact.Quality.HIGH_QUALITY));
        } finally {
            auctionsLock.unlock();
        }
    }

    private void fetchBidders() {
        for(AID aid : DFUtilities.searchAllDF(this, CuratorAgent.DF_NAME)) {
            bidders.add(aid);
        }
    }
}
