/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.id2209.hw2.exhibition;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import se.kth.id2209.hw2.util.Ontologies;

/**
 *
 * @author Kim
 */
public class AuctionListenerBehaviour extends CyclicBehaviour {

    AID travelGuide, profiler;
    CuratorAgent curator;
    MessageTemplate mt = new MessageTemplate(new MessageTemplate.MatchExpression() {
        @Override
        public boolean match(ACLMessage msg) {
            String ontology = msg.getOntology();
            return ontology.equalsIgnoreCase(Ontologies.AUCTION_START)
                    || ontology.equalsIgnoreCase(Ontologies.CALL_FOR_PROPOSALS)
                    || ontology.equalsIgnoreCase(Ontologies.AUCTION_NO_BIDS)
                    || msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL
                    || msg.getPerformative() == ACLMessage.REJECT_PROPOSAL;
        }
    });

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

            } else if (ontology.equalsIgnoreCase(Ontologies.CALL_FOR_PROPOSALS)) {

            } else if (ontology.equalsIgnoreCase(Ontologies.AUCTION_NO_BIDS)) {

            } else if (msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {

            } else if (msg.getPerformative() == ACLMessage.REJECT_PROPOSAL) {

            }
        } else {
            block();
        }
    }
}
