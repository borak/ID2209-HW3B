package se.kth.id2209.hw1.exhibition;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import se.kth.id2209.hw1.exhibition.Artifact.GENRE;
import se.kth.id2209.hw1.util.DFUtilities;
import se.kth.id2209.hw1.util.Ontologies;

@SuppressWarnings("serial")
class ListenerBehaviour extends CyclicBehaviour {
	AID travelGuide, profiler;
	CuratorAgent curator;
	
	ListenerBehaviour(CuratorAgent curator) {
		this.curator = curator;
		profiler = DFUtilities.searchDF(myAgent, "Profiler-Agent");
		travelGuide = DFUtilities.searchDF(myAgent, "Travel-Guide-Agent");
	}

	@Override
	public void action() {
		ACLMessage msg = curator.receive();		

		if(msg != null) {	
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
			} 
			// returns the artifact names (strings) instead of ids
			else if (ontology.equalsIgnoreCase(Ontologies.ARTIFACT_RECOMMENDATION_NAME)) {		
				try {
					GENRE genre = (GENRE) msg.getContentObject();			
					reply.setOntology(Ontologies.ARTIFACT_RESPONSE_RECOMMENDATION_NAMES);	
					reply.setContentObject(curator.getArtifactNameList(genre));					
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
			reply.addReceiver(msg.getSender());
			curator.send(reply);
		} else {
			block();
		}
	}
}
