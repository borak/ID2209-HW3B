package se.kth.id2209.hw2.auction;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import java.io.IOException;
import java.util.Map;
import se.kth.id2209.hw2.util.Ontologies;

/**
 * Handles incoming and outgoing messages regarding a dutch auction
 *
 */
class DutchAuctioneerBehaviour extends CyclicBehaviour {

    private final Agent agent;
    private final Map<Integer, Auction> auctions;
    private float priceDecrementConstant = 0.90f;

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
            //System.out.println(myAgent.getName() + " RECIEVED message: ontology="
            //        + msg.getOntology());

            int item = -1, price = -1;
            try {
                Object[] objs = (Object[]) msg.getContentObject();
                item = (int) objs[0];
                price = (int) objs[1];
            } catch (Exception e) {
                e.printStackTrace();
            }
            final Auction auction = auctions.get(item);

            if (auction == null) {
                System.err.println(myAgent.getName() + "Could not find auction.");
                block();
                return;
            }
            //System.out.println("AUCTION. nr=" + auction.getParticipants().size());
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

        } else {
            block();
        }
    }

    private void handleRejectProposal(Auction auction, ACLMessage msg) {
        auction.addParticipantWhoRejected(msg.getSender());

        //System.out.println("GOT REJ. nr=" + auction.getParticipantsWhichRejected().size() 
        //    + " of " + auction.getParticipants().size());
        if (auction.getParticipantsWhichRejected().size()
                == auction.getParticipants().size()) {
            performCFP(auction);
        }
    }

    private void performCFP(Auction auction) {
        if (auction.getParticipants().isEmpty()) {
            System.err.println(myAgent.getAID() + " performing CFP but no participants was found.");
            return;
        }
        //System.out.println(myAgent.getAID() + " performing CFP.");
        sendNoBidsCall(auction);
        int newPrice = (int) ((float) auction.getCurrentPrice()
                * priceDecrementConstant);
        if (newPrice >= auction.getLowestPrice()) {
            auction.setCurrentPrice(newPrice);
            auction.getParticipantsWhichRejected().clear();
            auction.CFPCounter++;
            myAgent.addBehaviour(new CFPBehaviour(auction, myAgent,
                    auction.getParticipants()));
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
