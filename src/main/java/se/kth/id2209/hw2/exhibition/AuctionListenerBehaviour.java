package se.kth.id2209.hw2.exhibition;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.util.*;
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
                    || msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL
                    || msg.getPerformative() == ACLMessage.REJECT_PROPOSAL;
        }
    });
    final List<Auction> knownAuctions = new ArrayList();
    final List<Artifact> boughtArtifacts = new ArrayList();
    final Map<Auction, Integer> participatingAuctions = new HashMap();
    final Map<Auction, BidSettings> auctionSettings = new HashMap();

    AuctionListenerBehaviour(CuratorAgent curator) {
        this.curator = curator;
    }

    /**
     * Looks at the message type and starts the appropriate handler
     */
    @Override
    public void action() {
        ACLMessage msg = curator.receive(mt);

        if (msg != null) {
            System.out.println(curator.getName() + " RECIEVED message: "
                    + msg.getOntology());
            String ontology = msg.getOntology();

            if (ontology.equalsIgnoreCase(Ontologies.AUCTION_START)) {
                handleAuctionStart(msg);
            } else if (ontology.equalsIgnoreCase(Ontologies.CALL_FOR_PROPOSALS)) {
                handleCFP(msg);
            } else if (ontology.equalsIgnoreCase(Ontologies.AUCTION_NO_BIDS)) {
                handleAuctionNoBids(msg);
            } else if (ontology.equalsIgnoreCase(Ontologies.AUCTION_WON)) {
                handleAuctionWon(msg);
            } else if (msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                handleAcceptProposal(msg);
            } else if (msg.getPerformative() == ACLMessage.REJECT_PROPOSAL) {
                handleAcceptReject(msg);
            } 
        } else {
            block();
        }
    }

    private void handleAuctionStart(ACLMessage msg) {
        try {
            if (msg.getContentObject() != null && msg.getContentObject() instanceof Auction) {
                final Auction auction = (Auction) msg.getContentObject();
                knownAuctions.add(auction);
                myAgent.addBehaviour(new OneShotBehaviour() {
                    @Override
                    public void action() {
                        participatingAuctions.put(auction, 0);
                        Random random = new Random();
                        double maxFactor = (random.nextInt(8) + 6)/10 ;
                        int maxPrice = (int)Math.floor(auction.getCurrentPrice() * maxFactor);


                        //-------------------------------------------------------------
                        //Change strategy, set to 0 to have one of each
//                        int strategy = random.nextInt(4) + 1;
                      int strategy = 0;
                        //-------------------------------------------------------------


                        BidSettings bs= new BidSettings(maxPrice, (int) Math.floor(0.7*maxPrice), strategy);
                        auctionSettings.put(auction, bs);
                    }
                });
            } else {
                block();
            }
        } catch (UnreadableException ex) {
            block();
        }
    }
    
    private Strategy getStrategy(ACLMessage msg, CuratorAgent agent, BidSettings bs) {
        int i = curator.getCuratorId();
        if(bs.getStrategy()!=0)
            i = bs.getStrategy();

        switch(i) {
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

                if (participatingAuctions.get(auction) != null) {
                    myAgent.addBehaviour(getStrategy(msg, curator, auctionSettings.get(auction)));
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
                /*if (participatingAuctions.get(auction) != null) {
                    participatingAuctions.remove(auction);
                }*/
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
                final Artifact art = (Artifact) msg.getContentObject();
                boughtArtifacts.add(art);
                
                for(Auction auction : participatingAuctions.keySet()) {
                    if(((Artifact)auction.getItem()).getId() == art.getId()) {
                        participatingAuctions.remove(auction);
                        knownAuctions.remove(auction);
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
                //boughtAuctions.add(auction);
                //participatingAuctions.remove(auction);
                //knownAuctions.remove(auction);
            } else {
                block();
            }
        } catch (UnreadableException ex) {
            block();
        }
    }

    private void handleAcceptReject(ACLMessage msg) {
        try {
            if (msg.getContentObject() != null && msg.getContentObject() instanceof Auction) {
                final Auction auction = (Auction) msg.getContentObject();
                //participatingAuctions.remove(auction);
                //knownAuctions.remove(auction);
            } else {
                block();
            }
        } catch (UnreadableException ex) {
            block();
        }
    }
}
