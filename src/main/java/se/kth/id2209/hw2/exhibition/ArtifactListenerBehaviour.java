package se.kth.id2209.hw2.exhibition;

import java.io.Serializable;
import java.util.ArrayList;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.MessageTemplate.MatchExpression;
import se.kth.id2209.hw2.exhibition.Artifact.GENRE;
import se.kth.id2209.hw2.util.DFUtilities;
import se.kth.id2209.hw2.util.Ontologies;

/**
 * This agent behavior is listening for artifact recommendation requests and 
 * performs appropriate responses.
 *
 */
class ArtifactListenerBehaviour extends CyclicBehaviour {
    AID travelGuide, profiler;
    CuratorAgent curator;
    MessageTemplate mt = new MessageTemplate(new MatchExpression() {
            @Override
            public boolean match(ACLMessage msg) {
                String ontology = msg.getOntology();
                return ontology.equalsIgnoreCase(Ontologies.ARTIFACT_RECOMMENDATION_ID)
                        || ontology.equalsIgnoreCase(Ontologies.ARTIFACT_RECOMMENDATION_NAME)
                        || ontology.equalsIgnoreCase(Ontologies.QUERY_ARTIFACTS);
            }
        });

    /**
     * Fetches the agents from the DFService it needs in order to perform its
     * service.
     * 
     * @param curator 
     */
    ArtifactListenerBehaviour(CuratorAgent curator) {
        this.curator = curator;
    }

    @Override
    public void action() {
        ACLMessage msg = curator.receive(mt);

        if (msg != null) {
//        	System.out.println(curator.getName() + " RECIEVED message: " + msg.getOntology());
            ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
            String ontology = msg.getOntology();
            if (ontology.equalsIgnoreCase(Ontologies.ARTIFACT_RECOMMENDATION_ID)) {
                try {
                    GENRE genre = (GENRE) msg.getContentObject();
                    reply.setOntology(Ontologies.ARTIFACT_RESPONSE_RECOMMENDATION_ID);
                    reply.setContentObject(curator.getArtifactIdList(genre));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (ontology.equalsIgnoreCase(Ontologies.ARTIFACT_RECOMMENDATION_NAME)) {
                try {
                    GENRE genre = (GENRE) msg.getContentObject();
                    reply.setOntology(Ontologies.ARTIFACT_RESPONSE_RECOMMENDATION_NAMES);
                    reply.setContentObject(curator.getArtifactNameList(genre));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (ontology.equalsIgnoreCase(Ontologies.QUERY_ARTIFACTS)) {
                try {
                    ArrayList<GENRE> genres = (ArrayList<GENRE>) msg.getContentObject();
                    reply.setOntology(Ontologies.QUERY_ARTIFACTS);
                    reply.setContentObject((Serializable) curator.getArtifactIdList(genres)); 
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            reply.addReceiver(msg.getSender());
//            System.out.println(curator.getName() + " SENDING msg: " + msg + " to " + msg.getSender().getName());
            curator.send(reply);
        } else {
            block();
        }
    }
}
