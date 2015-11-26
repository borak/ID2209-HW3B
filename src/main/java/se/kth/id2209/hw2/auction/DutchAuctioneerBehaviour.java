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
class DutchAuctioneerBehaviour extends CyclicBehaviour {

    private final Agent agent;
    private final Map<Integer, Auction> auctions;
    private float priceDecrementConstant = 0.9f;

    DutchAuctioneerBehaviour(Agent agent,
            Map<Integer, Auction> auctions) {
        super(agent);
        this.agent = agent;
        this.auctions = auctions;
    }

    @Override
    public void action() {
        ACLMessage msg = agent.receive();

        if (msg != null) {
            System.out.println(myAgent.getName() + " RECIEVED message: "
                    + msg.getOntology());
            String ontology = msg.getOntology();

            if (ontology.equalsIgnoreCase(Ontologies.AUCTION_BID)) {
                ACLMessage reply = msg.createReply();
                reply.addReceiver(msg.getSender());

                if (!validateMessage(msg)) {
                    block();
                    return;
                }

                final Auction auction = getAuction(msg);

                if (msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                    try {
                        handleAcceptProposal(msg);
                    } catch (Exception ex) {
                        System.err.println("Could not read or accept auction "
                                + "bid content.");
                        block();
                        return;
                    }
                } else if (msg.getPerformative() == ACLMessage.REJECT_PROPOSAL) {
                    auction.addParticipantWhoRejected(msg.getSender());
                } else {
                    block();
                    return;
                }

                if (auction.getParticipantsWhichRejected().size() == auction.getParticipants().size()) {
                    sendNoBidsCall(auction);
                    int newPrice = (int) ((float) auction.getCurrentPrice() 
                            * priceDecrementConstant);
                    if (newPrice >= auction.getLowestPrice()) {
                        auction.setCurrentPrice(newPrice);
                        myAgent.addBehaviour(new CFPBehaviour(auction, myAgent,
                                auction.getParticipants()));
                    } else {
                        auction.setIsDone(true);
                    }
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

    private void handleAcceptProposal(ACLMessage msg) throws Exception {
        ACLMessage reply = msg.createReply();
        reply.addReceiver(msg.getSender());
        int item = Integer.parseInt(msg.getContent());
        int price = (int) msg.getContentObject();
        final Auction auction = auctions.get(item);

        if (auction != null || auction.isDone()
                || price <= auction.getCurrentPrice()) {
            reply.setPerformative(ACLMessage.FAILURE);
            agent.send(reply);
            System.out.println("Agent AID=" + msg.getSender()
                    + " proposed bid on auction=" + auction
                    + " with price=" + price
                    + ". Auction could not be found or is "
                    + "completed.");
            block();
            return;
        }

        auction.setIsDone(true);
        auction.setWinner(msg.getSender());
        myAgent.addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                myAgent.addBehaviour(new InformAuctionWonBehaviour(
                        auction, myAgent));
            }
        });

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
