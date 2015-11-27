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

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import se.kth.id2209.hw2.auction.Auction;
import se.kth.id2209.hw2.auctionstrategies.*;
import se.kth.id2209.hw2.util.Ontologies;

/**
 *
 * @author Kim
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

    @Override
    public void action() {
        ACLMessage msg = curator.receive(mt);

        if (msg != null) {
            //System.out.println(curator.getName() + " RECIEVED message: "
            //        + msg.getOntology());
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
                handleReject(msg);
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
                        partAucLock.lock();
                        try {
                            participatingAuctions.add(auction);
                        } finally {
                            partAucLock.unlock();
                        }
                        Random random = new Random();
                        double maxFactor = (random.nextInt(8) + 2) / 10;
                        int maxPrice = (int) Math.floor(auction.getCurrentPrice() * maxFactor);
                        int strategy = 0;
                        BidSettings bs = new BidSettings(maxPrice, (int) Math.floor(0.7 * maxPrice), strategy);
                        auctionSettings.put(((Artifact) auction.getArtifact()).getId(), bs);
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
        int i = curator.getCuratorId();
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
                if (getAuction(auction) != null) {
                    myAgent.addBehaviour(getStrategy(msg, curator,
                            auctionSettings.get(auction.getArtifact().getId())));
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

    private void handleReject(ACLMessage msg) {
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
