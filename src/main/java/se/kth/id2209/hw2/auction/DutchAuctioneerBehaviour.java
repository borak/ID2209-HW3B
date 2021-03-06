package se.kth.id2209.hw2.auction;

import jade.core.AID;
import jade.core.Agent;
import jade.core.Location;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import se.kth.id2209.hw2.util.DFUtilities;
import se.kth.id2209.hw2.util.Ontologies;

/**
 * Handles incoming and outgoing messages regarding a dutch auction
 *
 */
class DutchAuctioneerBehaviour extends CyclicBehaviour {

    private final Agent agent;
    private final Map<Integer, Auction> auctions;
    private float priceDecrementConstant = 0.90f;
    private List<Auction> finishedAuctions = new ArrayList<Auction>();
    private Lock fLock = new ReentrantLock();

    DutchAuctioneerBehaviour(Agent agent,
            Map<Integer, Auction> auctions) {
        super(agent);
        this.agent = agent;
        this.auctions = auctions;
    }

    /**
     * Handles incoming messages from bidders
     */
    @Override
    public void action() {
        ACLMessage msg = agent.receive();

        if (msg != null) {
            int item = -1, price = -1;
            try {
                Object[] objs = (Object[]) msg.getContentObject();
                item = (int) objs[0];
                price = (int) objs[1];
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println(myAgent.getLocalName() + ": received (AUCTION) from " + msg.getSender().getLocalName());
            }
            final Auction auction = auctions.get(item);

            if (auction == null) {
                System.err.println(myAgent.getName() + ": Could not find auction.");
                block();
                return;
            }
            
            if (msg.getPerformative() == ACLMessage.INFORM
                    && msg.getOntology().equalsIgnoreCase(
                            Ontologies.CALL_FOR_PROPOSALS_TIMEOUT)) {
                performCFP(auction);
            } else if (msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                try {
                    handleAcceptProposal(msg, price, auction);
                } catch (Exception ex) {
                    System.err.println(myAgent.getName() + "Could not read or accept auction "
                            + "bid content.");
                    block();
                }
            } else if (msg.getPerformative() == ACLMessage.REJECT_PROPOSAL) {
                handleRejectProposal(auction, msg);
            }
            else if(msg.getOntology().equalsIgnoreCase(Ontologies.AUCTION_COMPLETE))
            {
                fLock.lock();
                try
                {
                    try
                    {

                        finishedAuctions.add((Auction) msg.getContentObject());
                    } catch (UnreadableException e)
                    {
                        e.printStackTrace();
                    }
                    if (finishedAuctions.size() > 1)
                    {
                        int max = 0;
                        Auction maxAuction = null;
                        for (Auction a : finishedAuctions)
                        {
                            if (a.getCurrentPrice() > max)
                            {
                                max = a.getCurrentPrice();
                                maxAuction = a;
                            }
                        }
                        System.out.println("Highest price from auction " + maxAuction.getArtifact().getId() + " was: " + max);
                    }
                }
                finally{
                    fLock.unlock();

                }
            }

        } else {
            block();
        }
    }
    
    private void moveHome(final Auction a) {
        myAgent.addBehaviour(new OneShotBehaviour(myAgent) { 
            @Override
            public void action() {
                ArtistManagementAgent agent = (ArtistManagementAgent)myAgent;
                Location home = agent.getHome();
                System.out.println("ARTIST ::::: ATTEMPTING MOVING from=" + myAgent.here() + " to " + home);
                if (home != null) {
                    if(!home.toString().equalsIgnoreCase(myAgent.here().toString()))
                    {
                        myAgent.doMove(home);
                    }
                    notifyParent(a);
                }
            }
        });
    }

    public void notifyParent(final Auction a) {

        myAgent.addBehaviour(new OneShotBehaviour()
        {
            @Override
            public void action()
            {
                AID[] artistManagement = DFUtilities.searchAllDF(myAgent, ArtistManagementAgent.DF_NAME);
                for(AID aid: artistManagement)
                {
                    if(!aid.getName().contains("clone"))
                    {
                        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                        msg.setOntology(Ontologies.AUCTION_COMPLETE);
                        msg.addReceiver(aid);
                        try
                        {
                            msg.setContentObject(a);
                        } catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                        myAgent.send(msg);
                    }


                }
            }
        });
    }


    private void handleRejectProposal(Auction auction, ACLMessage msg) {
        auction.addParticipantWhoRejected(msg.getSender());

        if (auction.getParticipantsWhichRejected().size()
                == auction.getParticipants().size()) {
            sendNoBidsCall(auction);
            performCFP(auction);
        }
    }

    private void performCFP(final Auction auction) {
        if (auction.getParticipants().isEmpty()) {
            System.err.println(myAgent.getAID() + " performing CFP but no participants was found.");
            return;
        }
        
        int newPrice = (int) ((float) auction.getCurrentPrice()
                * priceDecrementConstant);
        if (newPrice >= auction.getLowestPrice() && !auction.isDone()) {
            auction.setCurrentPrice(newPrice);
            auction.getParticipantsWhichRejected().clear();
            auction.CFPCounter++;
            myAgent.addBehaviour(new CFPBehaviour(auction, myAgent,
                    auction.getParticipants()));
        } else {
            myAgent.addBehaviour(new OneShotBehaviour(myAgent) {

                @Override
                public void action() {
                    ACLMessage stoppingAuctionMsg = new ACLMessage(ACLMessage.INFORM);
                    stoppingAuctionMsg.setOntology(Ontologies.AUCTION_STOP);
                    try {
                        stoppingAuctionMsg.setContentObject(auction);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    for (AID aid : auction.getParticipants()) {
                        stoppingAuctionMsg.addReceiver(aid);
                    }
                    myAgent.send(stoppingAuctionMsg);
                    moveHome(auction);
                }
            });
        }
    }

    private boolean validateMessage(ACLMessage msg) {
        Auction auction = null;
        try {
            int item = Integer.parseInt(msg.getContent());
            int price = (int) msg.getContentObject();
            if (price < 0) {
                return false;
            }
            auction = auctions.get(item);
        } catch (Exception ex) {
            return false;
        }
        return auction != null;
    }

    private void handleAcceptProposal(ACLMessage msg, int price,
            final Auction auction) throws Exception {

        if (auction == null || auction.isDone()
                || price < auction.getCurrentPrice()) {
            handleRejectProposal(auction, msg);
            ACLMessage reply = msg.createReply();
            reply.setOntology(Ontologies.AUCTION_BID);
            reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
            reply.addReceiver(msg.getSender());
            myAgent.send(reply);
            return;
        }

        auction.setIsDone(true);
        auction.setWinner(msg.getSender());
        myAgent.addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                myAgent.addBehaviour(new InformAuctionWonBehaviour(
                        auction, myAgent));
                moveHome(auction);
            }
        });
        
        // callback only on victory?
        myAgent.addBehaviour(new OneShotBehaviour(myAgent) {

                @Override
                public void action() {
                    ACLMessage stoppingAuctionMsg = new ACLMessage(ACLMessage.INFORM);
                    stoppingAuctionMsg.setOntology(Ontologies.AUCTION_STOP);
                    try {
                        stoppingAuctionMsg.setContentObject(auction);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    for (AID aid : auction.getParticipants()) {
                        if(!aid.equals(auction.getWinner())) {
                            stoppingAuctionMsg.addReceiver(aid);
                        }
                    }
                    myAgent.send(stoppingAuctionMsg);
                }
            });
    }

    private void sendNoBidsCall(Auction auction) {
        ACLMessage noBidsMsg = new ACLMessage(ACLMessage.INFORM);
        noBidsMsg.setOntology(Ontologies.AUCTION_NO_BIDS);
        noBidsMsg.setContent(auction.getArtifact().getId() + "");
        for (AID aid : auction.getParticipants()) {
            noBidsMsg.addReceiver(aid);
        }
        agent.send(noBidsMsg);
    }

    private Auction getAuction(ACLMessage msg) {
        Auction auctionTest = null;
        try {
            int item = Integer.parseInt(msg.getContent());
            int price = (int) msg.getContentObject();
            auctionTest = auctions.get(item);
        } catch (NumberFormatException | UnreadableException ex) {
            System.err.println(myAgent.getName() + "An unexpected error occurred when parsing "
                    + "message.");
        }
        return auctionTest;
    }
}
