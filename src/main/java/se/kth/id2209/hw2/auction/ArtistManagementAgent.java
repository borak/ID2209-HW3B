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
        auctionsLock.lock();
        try {
            auctions.put(432, new Auction(new ArrayList(), 1000, 400, 
                    Auction.Quality.HIGH_QUALITY, 432)); 
            auctions.put(743, new Auction(new ArrayList(), 5321, 3200, 
                    Auction.Quality.LOW_QUALITY, 743));
            auctions.put(344, new Auction(new ArrayList(), 100, 34, 
                    Auction.Quality.LOW_QUALITY, 344));
            auctions.put(888, new Auction(new ArrayList(), 433, 213, 
                    Auction.Quality.LOW_QUALITY, 888));
            auctions.put(777, new Auction(new ArrayList(), 60, 20, 
                    Auction.Quality.HIGH_QUALITY, 777));
            auctions.put(999, new Auction(new ArrayList(), 40200, 24000, 
                    Auction.Quality.HIGH_QUALITY, 999));
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
