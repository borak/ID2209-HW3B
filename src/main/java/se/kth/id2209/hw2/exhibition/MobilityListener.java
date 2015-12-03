package se.kth.id2209.hw2.exhibition;

import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Result;
import jade.core.Agent;
import jade.core.Location;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kim
 */
public class MobilityListener extends OneShotBehaviour
{
    Map<String, Location> locations;

    MobilityListener(Agent agent, Map<String, Location> locations) {
        super(agent);
        this.locations = locations;
    }

    @Override
    public void action() {
        ACLMessage msg = myAgent.receive(MessageTemplate.MatchSender(myAgent.getAMS()));

        if (msg != null) {

            Result r = null;
            try
            {
                r = (Result) myAgent.getContentManager().extractContent(msg);
            } catch (Codec.CodecException e)
            {
                e.printStackTrace();
            } catch (OntologyException e)
            {
                e.printStackTrace();
            }
            jade.util.leap.Iterator it = r.getItems().iterator();
                while (it.hasNext()) {
                    Location loc = (Location)it.next();
                    locations.put(loc.getName(), loc);
                }
            }

        }


}
