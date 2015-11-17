package se.kth.id2209.hw1.exhibition;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import se.kth.id2209.hw1.exhibition.Artifact.GENRE;
import se.kth.id2209.hw1.util.Ontologies;

@SuppressWarnings("serial")
public class ListenerBehaviour extends CyclicBehaviour {
	CuratorAgent curator;

	public ListenerBehaviour(CuratorAgent currator) {
		this.curator = currator;
	}

	@Override
	public void action() {
		ACLMessage msg = curator.receive();		

		if(msg != null) {	
			ACLMessage reply = msg.createReply();	
			String ontology = msg.getOntology();
			if (ontology.equalsIgnoreCase(Ontologies.ARTIFACT_RECOMMENDATION)) {		
				try {
					GENRE genre = (GENRE) msg.getContentObject();			
					reply.setOntology(Ontologies.ARTIFACT_RESPONSE_RECOMMENDATION);	
					reply.setContentObject(curator.getArtifactIdList(genre));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}				
			else if (ontology.equalsIgnoreCase(Ontologies.ARTIFACT_REQUEST_INFO)) {
				try {
					int artifactID = (int) msg.getContentObject();			
					reply.setOntology(Ontologies.ARTIFACT_RESPONSE_INFO);	
					reply.setContentObject(curator.getArtifact(artifactID));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			curator.send(reply);
		} else {
			block();
		}
	}
}
