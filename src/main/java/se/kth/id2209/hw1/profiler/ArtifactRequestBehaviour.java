package se.kth.id2209.hw1.profiler;

import java.io.IOException;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import se.kth.id2209.hw1.util.DFUtilities;
import se.kth.id2209.hw1.util.Ontologies;

@SuppressWarnings("serial")
public class ArtifactRequestBehaviour extends OneShotBehaviour {

    private AID cAgent;
    private Integer artifactId;

    public ArtifactRequestBehaviour(Agent a, Integer artifactId) {
        super(a);
        this.artifactId = artifactId;
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("service");
        dfd.addServices(sd);
        AID[] aids = DFUtilities.searchDF(a, dfd);
        if (aids.length < 1) {
            throw new RuntimeException("Cannot find agent");
        }
        cAgent = aids[0];
    }

    private void sendRequest(Integer artifactId) {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(cAgent);
        msg.setLanguage(ProfilerAgent.ACL_LANGUAGE);
        msg.setOntology(Ontologies.ARTIFACT_REQUEST_INFO);
        try {
            msg.setContentObject(artifactId);
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
