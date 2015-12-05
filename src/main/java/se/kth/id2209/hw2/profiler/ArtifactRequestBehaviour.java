package se.kth.id2209.hw2.profiler;

import java.io.IOException;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import se.kth.id2209.hw2.exhibition.CuratorAgent;
import se.kth.id2209.hw2.util.DFUtilities;
import se.kth.id2209.hw2.util.Ontologies;

/**
 * This is a behavior that sends a request for an artifact to a Curator agent.
 * 
 * TODO: Rename the curator agent type to something more unique.
 * 
 *
 */
class ArtifactRequestBehaviour extends OneShotBehaviour {
    private AID cAgent;
    private Integer artifactId;

    public ArtifactRequestBehaviour(ProfilerAgent a, Integer artifactId) {
        super(a);
        this.artifactId = artifactId;
        cAgent = a.getCurator();
    }

    private void sendRequest(Integer artifactId) {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(cAgent);
        msg.setLanguage(ProfilerAgent.ACL_LANGUAGE);
        msg.setOntology(Ontologies.ARTIFACT_REQUEST_INFO);
        try {
            msg.setContentObject(artifactId);
            System.out.println(myAgent.getName() + " SENDING msg: " + msg.getOntology() + " to " + cAgent.getName());
            myAgent.send(msg);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void action() {
        sendRequest(artifactId);
    }

}
