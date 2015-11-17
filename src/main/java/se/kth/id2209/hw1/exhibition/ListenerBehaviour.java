package se.kth.id2209.hw1.exhibition;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

@SuppressWarnings("serial")
public class ListenerBehaviour  extends CyclicBehaviour {
	CuratorAgent curator;

	public ListenerBehaviour(CuratorAgent currator) {
		this.curator = currator;
	}

	@Override
	public void action() {
		String content;
		ACLMessage msg = curator.receive();
		if(msg != null) {			
			if (msg.getOntology().equalsIgnoreCase(Ontologies.ARTIFACT_RECOMMENDATION)) {					
				AID senderID = msg.getSender();				
				if(senderID == AGENTS.PROFILER){ // TODO hämta id från DF	
					reply(msg, senderID);
				}
			} else if (msg.getOntology().equalsIgnoreCase(Ontologies.ARTIFACT_RECOMMENDATION)) {
				// TODO
			}		
		}
	}

	private void reply(ACLMessage msg, AID senderID) {		
		ACLMessage reply = msg.createReply();		
		if(senderID == AGENTS.PROFILER) { // TODO hämta id från DF			
			int artifactID = (int) msg.getContentObject();
			reply.setContentObject(curator.getArtifact(artifactID));
		}
		else if(senderID == AGENTS.GUIDE) {
			// TODO
		}
	}
}
