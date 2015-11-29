package se.kth.id2209.hw2.auction;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import se.kth.id2209.hw2.exhibition.Artifact;
import se.kth.id2209.hw2.util.Ontologies;

/**
 * Sends out a Call For Proposals to all the receivers
 *
 */
public class CFPBehaviour extends OneShotBehaviour {

    private final Auction auction;
    private final List<AID> receivers;
    private static final int timeout = 12000;

    public CFPBehaviour(Auction auction, Agent a, List<AID> receivers) {
        super(a);
        this.auction = auction;
        this.receivers = receivers;
    }

    @Override
    public void action() {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setOntology(Ontologies.CALL_FOR_PROPOSALS);
        try {
            msg.setContentObject(auction);
            for (AID r : receivers) {
                msg.addReceiver(r);
            }
            myAgent.send(msg);
            System.out.println(myAgent.getName() + " SENDING message: "
                    + msg.getOntology() + " " + auction);

            myAgent.addBehaviour(new WakerBehaviour(myAgent, timeout) {
                int cfpcounter = auction.CFPCounter;

                @Override
                public void onWake() {
                    if (cfpcounter == auction.CFPCounter) {
                        try {
                            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                            msg.setOntology(Ontologies.CALL_FOR_PROPOSALS_TIMEOUT);
                            Object[] objs = new Object[2];
                            objs[0] = auction.getArtifact().getId();
                            objs[1] = -1;
                            msg.setContentObject(objs);
                            msg.addReceiver(myAgent.getAID());
                            myAgent.send(msg);
                        } catch (IOException ex) {
                            Logger.getLogger(CFPBehaviour.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            });
        } catch (IOException ex) {
            System.err.println("Error occurred when " + myAgent.getName()
                    + " created auction start message.");
            block();
        }
    }

}
