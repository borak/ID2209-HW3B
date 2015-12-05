package se.kth.id2209.hw2.exhibition;

import jade.core.AID;
import jade.core.Location;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import se.kth.id2209.hw2.auction.Auction;
import se.kth.id2209.hw2.auctionstrategies.*;
import se.kth.id2209.hw2.util.Ontologies;

/**
 * AuctionListener handles the incoming communication from an auctioneer, and handles each type of response accordingly
 *
 */
public class AuctionListenerBehaviour extends CyclicBehaviour {

    private final CuratorAgent curator;
    static final MessageTemplate mt = new MessageTemplate(new MessageTemplate.MatchExpression() {
        @Override
        public boolean match(ACLMessage msg) {
            String ontology = msg.getOntology();
            return ontology.equalsIgnoreCase(Ontologies.AUCTION_START)
                    || ontology.equalsIgnoreCase(Ontologies.CALL_FOR_PROPOSALS)
                    || ontology.equalsIgnoreCase(Ontologies.AUCTION_NO_BIDS)
                    || ontology.equalsIgnoreCase(Ontologies.AUCTION_WON)
                    || ontology.equalsIgnoreCase(Ontologies.AUCTION_STOP)
                    || msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL
                    || msg.getPerformative() == ACLMessage.REJECT_PROPOSAL;
        }
    });
    final List<Auction> knownAuctions = new ArrayList();
    final List<Artifact> boughtArtifacts = new ArrayList();
    final List<Auction> participatingAuctions = new ArrayList();
    final Map<Integer, BidSettings> auctionSettings = new HashMap();
    final Lock partAucLock = new ReentrantLock();

    AuctionListenerBehaviour(CuratorAgent curator) {
        this.curator = curator;
    }


    private Auction getAuction(Auction auction) {
        partAucLock.lock();
        try {
            for (Auction a : participatingAuctions) {
                if (a.equals(auction)) {
                    return a;
                }
            }
        } finally {
            partAucLock.unlock();
        }
        return null;
    }

    /**
     * Looks at the message type and starts the appropriate handler
     */
    @Override
    public void action() {
        ACLMessage msg = curator.receive(mt);
        
        if (msg != null) {
            String ontology = msg.getOntology();

            if (ontology.equalsIgnoreCase(Ontologies.AUCTION_START)) {
                handleAuctionStart(msg);
            } else if (ontology.equalsIgnoreCase(Ontologies.CALL_FOR_PROPOSALS)) {
                handleCFP(msg);
            } else if (ontology.equalsIgnoreCase(Ontologies.AUCTION_NO_BIDS)) {
                handleAuctionNoBids(msg);
            } else if (ontology.equalsIgnoreCase(Ontologies.AUCTION_WON)) {
                handleAuctionWon(msg);
                moveHome();
            } else if (msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                handleAcceptProposal(msg);
            } else if (msg.getPerformative() == ACLMessage.REJECT_PROPOSAL) {
                handleReject(msg);
            } else if(ontology.equalsIgnoreCase(Ontologies.AUCTION_STOP)) {
                moveHome();
            }
        } else {
            block();
        }
    }
    
    private void moveHome() {
        myAgent.addBehaviour(new OneShotBehaviour(myAgent) { 
            @Override
            public void action() {
                CuratorAgent agent = (CuratorAgent)myAgent;
                if(!agent.isClone()) {
                    return;
                }
                Location home = agent.getHome();
                System.out.println("CURATOR ::::: ATTEMPTING MOVING from=" + myAgent.here() + " to " + home);
                if (home != null) {
                    myAgent.doMove(home);
                }
            }
        });
    }

    private void handleAuctionStart(ACLMessage msg) {
        try {
            if (msg.getContentObject() != null && msg.getContentObject() instanceof Auction) {
                final Auction auction = (Auction) msg.getContentObject();
                knownAuctions.add(auction);
                myAgent.addBehaviour(new OneShotBehaviour() {
                    @Override
                    public void action() {
                        participatingAuctions.add(auction);
                        Random random = new Random();
                        double maxFactor = ((double)(random.nextInt(60) + 30)) / 100;
                        int maxPrice = (int)((float) auction.getCurrentPrice() * maxFactor);
                        int strategy = 0;
                        int prefPrice = (int)((float) 0.7 * maxPrice);
                        BidSettings bs = new BidSettings(maxPrice, prefPrice, strategy);
                        auctionSettings.put(auction.getArtifact().getId(), bs);
                    }
                });
            } else {
                block();
            }
        } catch (UnreadableException ex) {
            block();
        }
    }

    private Strategy getStrategy(ACLMessage msg, CuratorAgent agent,
            BidSettings bs) {
        int i = agent.getCuratorId();
        if (bs.getStrategy() != 0) {
            i = bs.getStrategy();
        }

        switch (i) {
            case 1:
                return new StrategyOne(msg, agent, bs);
            case 2:
                return new StrategyTwo(msg, agent, bs);
            case 3:
                return new StrategyThree(msg, agent, bs);
            case 4:
                return new StrategyFour(msg, agent, bs);
            default:
                return new StrategyOne(msg, agent, bs);
        }
    }

    private void handleCFP(ACLMessage msg) {
        try {
            if (msg.getContentObject() != null && msg.getContentObject() instanceof Auction) {
                final Auction auction = (Auction) msg.getContentObject();

                if (participatingAuctions.contains(auction)) {
                    myAgent.addBehaviour(getStrategy(msg, curator, auctionSettings.get(auction.getArtifact().getId())));
                }
            } else {
                block();
            }
        } catch (UnreadableException ex) {
            block();
        }
    }

    private void handleAuctionNoBids(ACLMessage msg) {
        try {
            if (msg.getContentObject() != null && msg.getContentObject() instanceof Auction) {
                final Auction auction = (Auction) msg.getContentObject();
            } else {
                block();
            }
        } catch (UnreadableException ex) {
            block();
        }
    }

    private void handleAuctionWon(ACLMessage msg) {
        try {
            if (msg.getContentObject() != null && msg.getContentObject() instanceof Auction) {
                final Artifact art = ((Auction) msg.getContentObject()).getArtifact();
                boughtArtifacts.add(art);

                for (Auction auction : participatingAuctions) {
                    Auction match = getAuction(auction);
                    if (match != null && match.getArtifact().getId()
                            == art.getId()) {
                        participatingAuctions.remove(match);
                        knownAuctions.remove(match);
                        break;
                    }
                }
            } else {
                block();
            }
        } catch (UnreadableException ex) {
            block();
        }
    }

    private void handleAcceptProposal(ACLMessage msg) {
        try {
            if (msg.getContentObject() != null && msg.getContentObject() instanceof Auction) {
                final Auction auction = (Auction) msg.getContentObject();
            } else {
                block();
            }
        } catch (UnreadableException ex) {
            block();
        }
    }

    private void handleReject(ACLMessage msg) {
        try {
            if (msg.getContentObject() != null && msg.getContentObject() instanceof Auction) {
                final Auction auction = (Auction) msg.getContentObject();
            } else {
                block();
            }
        } catch (UnreadableException ex) {
            block();
        }
    }
}
