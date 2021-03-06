package se.kth.id2209.hw2.auction;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import java.io.IOException;
import java.util.List;
import se.kth.id2209.hw2.exhibition.Artifact;
import se.kth.id2209.hw2.util.Ontologies;

/**
 *  Informs the bidder with the highest bid that it is the winner.
 *
 */
public class InformAuctionWonBehaviour extends OneShotBehaviour {
    private final Auction auction;

    public InformAuctionWonBehaviour(Auction auction, Agent a) {
        super(a);
        this.auction = auction;
    }
    
    @Override
    public void action() {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setOntology(Ontologies.AUCTION_WON);
        try {
            msg.setContentObject(auction);
            msg.addReceiver(auction.getWinner());
            myAgent.send(msg);
            System.out.println("Auction completed. " + auction);
        } catch (IOException ex) {
            System.err.println("Error occurred when "+myAgent.getName()
                    +" created auction won message.");
            block();
        }
    }
    
}
