package se.kth.id2209.hw2.auction;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import java.io.IOException;
import java.util.List;
import se.kth.id2209.hw2.util.Ontologies;

/**
 *  Sends messages to all the bidders, informing them that an auction is starting
 *
 */
public class InformStartOfAuctionBehaviour extends OneShotBehaviour {
    private Auction auction;
    private List<AID> receivers;

    public InformStartOfAuctionBehaviour(Auction auction, Agent a, List<AID> receivers) {
        super(a);
        this.auction = auction;
        this.receivers = receivers;
    }
    
    @Override
    public void action() {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setOntology(Ontologies.AUCTION_START);
        try {
            msg.setContentObject(auction);
            for(AID r : receivers) {
                if(!r.equals(myAgent.getAID())) {
                    msg.addReceiver(r);
                }
            }
            myAgent.send(msg);
            System.out.println(myAgent.getName() + " SENDING message: "
                    + msg.getOntology());
        } catch (IOException ex) {
            System.err.println("Error occurred when "+myAgent.getName()
                    +" created auction start message.");
            block();
        }
    }
    
}
