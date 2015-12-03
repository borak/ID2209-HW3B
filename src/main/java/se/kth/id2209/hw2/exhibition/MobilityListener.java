package se.kth.id2209.hw2.exhibition;

import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Result;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import static se.kth.id2209.hw2.exhibition.AuctionListenerBehaviour.mt;
import se.kth.id2209.hw2.util.Ontologies;

/**
 *
 * @author Kim
 */
public class MobilityListener extends OneShotBehaviour
{
    List loc;

    MobilityListener(Agent agent, List locations) {
        super(agent);
        loc = locations;
    }

    @Override
    public void action() {
        ACLMessage msg = myAgent.receive(MessageTemplate.MatchSender(myAgent.getAMS()));

        if (msg != null) {
            try {
                Result r = (Result) myAgent.getContentManager().extractContent(msg);
                loc = (List) r.getValue();
            } catch (Codec.CodecException ex) {
                Logger.getLogger(MobilityListener.class.getName()).log(Level.SEVERE, null, ex);
            } catch (OntologyException ex) {
                Logger.getLogger(MobilityListener.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
