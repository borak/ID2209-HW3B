package se.kth.id2209.hw1.profiler;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import java.io.IOException;
import static se.kth.id2209.hw1.profiler.ProfilerAgent.ACL_LANGUAGE;
import se.kth.id2209.hw1.util.DFUtilities;
import se.kth.id2209.hw1.util.Ontologies;

/**
 *
 * @author Kim
 */
class SendTourGuideRequestBehaviour extends Behaviour {

        private boolean isDone = false;
        private UserProfile profile;
        
        SendTourGuideRequestBehaviour(Agent agent, UserProfile profile) {
            super(agent);
            this.profile = profile;
        }
        
        @Override
        public void action() {
            isDone = sendTourGuideRequest();
        }

        @Override
        public boolean done() {
            return isDone;
        }

        private boolean sendTourGuideRequest() {
            DFAgentDescription dfdTGA = new DFAgentDescription();
            ServiceDescription sdTGA = new ServiceDescription();
            sdTGA.setType("Tour-Guide-agent");
            dfdTGA.addServices(sdTGA);
            AID[] aids = DFUtilities.searchDF(myAgent, dfdTGA);
            if (aids.length < 1) {
                return false;
            }

            AID tgAgent = (AID) aids[0];
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.addReceiver(tgAgent);
            msg.setLanguage(ACL_LANGUAGE);
            msg.setOntology(Ontologies.PROFILER_REQUEST_TOUR_AGENT);
            try {
                msg.setContentObject(profile);
                myAgent.send(msg);
                return true;
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return false;
        }

    }
