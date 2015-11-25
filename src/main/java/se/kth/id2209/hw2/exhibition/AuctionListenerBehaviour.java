/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.id2209.hw2.exhibition;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import se.kth.id2209.hw2.auction.Auction;
import se.kth.id2209.hw2.auctionstrategies.BidSettings;
import se.kth.id2209.hw2.auctionstrategies.Strategy;
import se.kth.id2209.hw2.auctionstrategies.StrategyOne;
import se.kth.id2209.hw2.auctionstrategies.StrategyTwo;
import se.kth.id2209.hw2.util.Ontologies;

/**
 *
 * @author Kim
 */
public class AuctionListenerBehaviour extends CyclicBehaviour {

    private AID travelGuide, profiler;
    private CuratorAgent curator;
    MessageTemplate mt = new MessageTemplate(new MessageTemplate.MatchExpression() {
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
    final List<Auction> boughtAuctions = new ArrayList();
    final Map<Auction, Integer> participatingAuctions = new HashMap();
    //private Class<? extends Strategy> strategy;

    AuctionListenerBehaviour(CuratorAgent curator) {
        this.curator = curator;
    }

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
                //Decide if the agent shuld join in on the auction
                myAgent.addBehaviour(new OneShotBehaviour() {
                    @Override
                    public void action() {
                        // Join them all
                        participatingAuctions.put(auction, 0);
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
        switch(curator.getCuratorId()) {
            case 1:
                return new StrategyOne(msg, agent, bs);
            case 2:
                return new StrategyTwo(msg, agent, bs);
            case 3:
                return null;
            case 4:
                return null;
            default:
                return new StrategyOne(msg, agent, bs);
        }
    }

    private void handleCFP(ACLMessage msg) {
        try {
            if (msg.getContentObject() != null && msg.getContentObject() instanceof Auction) {
                final Auction auction = (Auction) msg.getContentObject();

                if (participatingAuctions.get(auction) != null) {
                    myAgent.addBehaviour(getStrategy(msg, curator, null)); // change to appropriate bid setting
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

    private void handleAcceptProposal(ACLMessage msg) {
        try {
            if (msg.getContentObject() != null && msg.getContentObject() instanceof Auction) {
                final Auction auction = (Auction) msg.getContentObject();
                boughtAuctions.add(auction);
                participatingAuctions.remove(auction);
                knownAuctions.remove(auction);
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
                participatingAuctions.remove(auction);
                knownAuctions.remove(auction);
            } else {
                block();
            }
        } catch (UnreadableException ex) {
            block();
        }
    }
}
