package se.kth.id2209.hw2.auction;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import java.util.Map;
import se.kth.id2209.hw2.util.Ontologies;

/**
 *
 * @author Kim
 */
class BidListenerBehaviour extends CyclicBehaviour {
    private final ArtistManagementAgent agent;
    private final Map<Integer, Auction> auctions;

    BidListenerBehaviour(ArtistManagementAgent agent, 
            Map<Integer, Auction> auctions) {
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
                int price = -1;
                int item = -1;
                Auction auction = null;
                try {
                    item = Integer.parseInt(msg.getContent());
                    price = (int) msg.getContentObject();
                    auction = auctions.get(item);
                } catch (Exception ex) {
                    block();
                }

                if (auction != null || auction.isDone()) {
                    reply.setPerformative(ACLMessage.FAILURE);
                    agent.send(reply);
                    System.out.println("Agent AID=" + msg.getSender()
                            + " proposed bid on auction=" + auction
                            + " with price=" + price
                            + ". Auction could not be found or is "
                            + "completed.");
                } else if (msg.getPerformative() == ACLMessage.PROPOSE) {
                    try {
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
                    } catch (Exception ex) {
                        System.err.println("Could not read auction bid "
                                + "content.");
                        block();
                    }
                } else if (msg.getPerformative() == ACLMessage.REFUSE) {
                    reply.setPerformative(ACLMessage.CONFIRM);
                    agent.send(reply);

                    // remove from participants?
                    auction.removeParticipant(msg.getSender());
                    reply.setPerformative(ACLMessage.CONFIRM);
                    agent.send(reply);
                } else {
                    block();
                }
            }
        } else {
            block();
        }
    }

}
