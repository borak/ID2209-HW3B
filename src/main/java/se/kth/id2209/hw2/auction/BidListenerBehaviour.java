package se.kth.id2209.hw2.auction;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import java.util.Map;
import se.kth.id2209.hw2.util.Ontologies;

/**
 *
 * @author Kim
 */
class BidListenerBehaviour extends CyclicBehaviour {

    private final Agent agent;
    private final Map<Integer, Auction> auctions;

    BidListenerBehaviour(Agent agent,
            Map<Integer, Auction> auctions) {
        super(agent);
        this.agent = agent;
        this.auctions = auctions;
    }

    @Override
    public void action() {
        ACLMessage msg = agent.receive();

        if (msg != null) {
            String ontology = msg.getOntology();

            if (ontology.equalsIgnoreCase(Ontologies.AUCTION_BID)) {
                ACLMessage reply = msg.createReply();
                reply.addReceiver(msg.getSender());

                if (!validateMessage(msg)) {
                    block();
                    return;
                }

                final Auction auction = getAuction(msg);

                if (msg.getPerformative() == ACLMessage.PROPOSE) {
                    try {
                        handleProposal(msg);
                    } catch (Exception ex) {
                        System.err.println("Could not read or accept auction "
                                + "bid content.");
                        block();
                        return;
                    }
                } else if (msg.getPerformative() == ACLMessage.NOT_UNDERSTOOD) {
                    auction.addNotUnderstood(msg.getSender());
                } else {
                    block();
                    return;
                }

                if (auction.getNotUnderstood().size() == auction.getParticipants().size()) {
                    auction.setIsDone(true);
                    sendNoBidsCall(auction);
                } else if (auction.getBids().size()
                        + auction.getNotUnderstood().size()
                        == auction.getParticipants().size()) {
                    myAgent.addBehaviour(new CFPBehaviour(auction, myAgent,
                            auction.getBidders()));
                }
            } else {
                block();
            }
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

    private void handleProposal(ACLMessage msg) throws Exception {
        ACLMessage reply = msg.createReply();
        reply.addReceiver(msg.getSender());
        int item = Integer.parseInt(msg.getContent());
        int price = (int) msg.getContentObject();
        Auction auction = auctions.get(item);

        if (auction != null || auction.isDone()
                || price <= auction.getPrice()) {
            reply.setPerformative(ACLMessage.FAILURE);
            agent.send(reply);
            System.out.println("Agent AID=" + msg.getSender()
                    + " proposed bid on auction=" + auction
                    + " with price=" + price
                    + ". Auction could not be found or is "
                    + "completed.");
            return;
        }

        if (price > auction.getPrice()) {
            auction.addBid(msg.getSender(), price);
            reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
            agent.send(reply);
            System.out.println("Agent AID=" + msg.getSender()
                    + " proposed bid on auction=" + auction
                    + " with price=" + price + ". Proposal was "
                    + "ACCEPTED. ");
        } else {
            reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
            agent.send(reply);
            System.out.println("Agent AID=" + msg.getSender()
                    + " proposed bid on auction=" + auction
                    + " with price=" + price + ". Proposal was "
                    + "REJECTED.");
        }
    }

    private void sendNoBidsCall(Auction auction) {
        ACLMessage noBidsMsg = new ACLMessage(ACLMessage.INFORM);
        noBidsMsg.setOntology(Ontologies.AUCTION_NO_BIDS);
        noBidsMsg.setContent(auction.getItem() + "");
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
        } catch (Exception ex) {
            System.err.println("An unexpected error occurred when parsing "
                    + "message. This should never happen and means that the "
                    + "validation failed. ");
        }
        return auctionTest;
    }
}
