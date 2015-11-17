package se.kth.id2209.hw1.exhibition;

import java.io.IOException;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import util.Ontologies;

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
				//	if(senderID == AGENTS.PROFILER){ // TODO hämta id från DF	
				try {
					reply(msg, senderID);
				} catch(Exception e) {
					e.printStackTrace();
				}
			} else if (msg.getOntology().equalsIgnoreCase(Ontologies.ARTIFACT_RECOMMENDATION)) {
				// TODO
			}		
		}
	}

	private void reply(ACLMessage msg, AID senderID) throws UnreadableException, IOException {	
		AID dummy = new AID();
		ACLMessage reply = msg.createReply();		
		if(senderID == dummy) { // TODO hämta id från DF			
			int artifactID = (int) msg.getContentObject();		
			reply.setContentObject(curator.getArtifact(artifactID));
		}
		else if(senderID == dummy) {
			// TODO
		}
	}
}
