/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package se.kth.id2209.hw1.exhibition;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import se.kth.id2209.hw1.exhibition.Artifact;
import se.kth.id2209.hw1.exhibition.Artifact.GENRE;

/**
 * ïƒ˜ A gallery/museum contains detailed information of artifacts such as:
 *    ï‚§ id, name, creator, date of creation, place of creation, genre etc.
 * 
 * @author Kim
 */
class ArtGallery {

	Map<Integer, Artifact> artifacts = new HashMap<Integer, Artifact>();
	private static ArtGallery instance;
	
	private ArtGallery(){
	}

	ArrayList<Artifact> getArtList() {
		ArrayList<Artifact> list = new ArrayList<>();

		Iterator<Entry<Integer, Artifact>> it = artifacts.entrySet().iterator();
		while(it.hasNext()) {			
			list.add(it.next().getValue());
		}
		return list;		
	}

	void add(Artifact artifact) {
		int id = artifact.getId();
		artifacts.put(id, artifact);
	}

	void add(int id, String name, String creator, String placeofCreation, Date creationDate, GENRE genre) {
		Artifact artifact = new Artifact(id, placeofCreation, placeofCreation, placeofCreation, creationDate, genre);
		artifacts.put(id, artifact);
	}

	Artifact getArtifact(int id) {
		if(artifacts.containsKey(id)) { 
			return artifacts.get(id);
		}
		return null;
	}

	ArtGallery getInstance() {
		if(instance == null) {
			instance = new ArtGallery();
		}
		return instance;
	}

	ArrayList<Integer> getArtifactList(GENRE genre) {
		ArrayList<Integer> list = new ArrayList<>();

		Iterator<Entry<Integer, Artifact>> it = artifacts.entrySet().iterator();
		while(it.hasNext()) {		
			Artifact artifact = it.next().getValue();
			if(genre instanceof Artifact.GENRE) {
				list.add(artifact.getId());
			}
		}
		return list;
	}

}
