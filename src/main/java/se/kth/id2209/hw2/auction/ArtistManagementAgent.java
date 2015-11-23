package se.kth.id2209.hw2.auction;

import jade.core.Agent;
import jade.core.behaviours.ParallelBehaviour;
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

/**
 *
 * @author Kim
 */
public class ArtistManagementAgent extends Agent {
    
    public static final String DF_NAME = "Artist-management-agent";
    private final Map<Integer, Auction> auctions = new HashMap();
    Lock auctionsLock = new ReentrantLock();
    
    @Override
    protected void setup() {
        registerService();
        
        auctionsLock.lock();
        try {
            auctions.put(134, new Auction(new ArrayList(), 1000, 134));
            auctions.put(352, new Auction(new ArrayList(), 5321, 352));
        } finally {
            auctionsLock.unlock();
        }
        // add behaviour
            //listening for bids
            //sending status
        ParallelBehaviour pbr = new ParallelBehaviour(this,
                ParallelBehaviour.WHEN_ALL);
        pbr.addSubBehaviour(new BidListenerBehaviour(this, auctions));
        //pbr.addSubBehaviour(new DatabaseChecker(this, DB_CHECKER_DELAY));
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
}
