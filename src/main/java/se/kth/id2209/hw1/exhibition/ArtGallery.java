/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package se.kth.id2209.hw1.exhibition;
import java.io.Serializable;
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

	HashMap<Integer, Artifact> map = new HashMap<Integer, Artifact>();
	private static ArtGallery instance;
	
	private ArtGallery(){
	}

	ArrayList<Artifact> getArtList() {
		ArrayList<Artifact> list = new ArrayList<>();

		Iterator<Entry<Integer, Artifact>> it = map.entrySet().iterator();
		while(it.hasNext()) {			
			list.add(it.next().getValue());
		}
		return list;		
	}

	void add(Artifact artifact) {
		int id = artifact.getId();
		map.put(id, artifact);
	}

	void add(int id, String name, String creator, String placeofCreation, Date creationDate, GENRE genre) {
		Artifact artifact = new Artifact(id, placeofCreation, placeofCreation, placeofCreation, creationDate, genre);
		map.put(id, artifact);
	}

	Artifact getArtifact(int id) {
		if(map.containsKey(id)) { 
			return map.get(id);
		}
		return null;
	}

	ArtGallery getInstance() {
		if(instance == null) {
			instance = new ArtGallery();
		}
		return instance;
	}

	ArrayList<Integer> getArtifactIdList(GENRE genre) {
		ArrayList<Integer> list = new ArrayList<>();

		Iterator<Entry<Integer, Artifact>> it = map.entrySet().iterator();
		while(it.hasNext()) {		
			Artifact artifact = it.next().getValue();
			if(genre instanceof Artifact.GENRE) {
				list.add(artifact.getId());
			}
		}
		return list;
	}

	ArrayList<String> getArtifactNameList(GENRE genre) {
		ArrayList<String> list = new ArrayList<>();

		Iterator<Entry<Integer, Artifact>> it = map.entrySet().iterator();
		while(it.hasNext()) {		
			String name = it.next().getValue().getName();
			if(genre instanceof Artifact.GENRE) {
				list.add(name);
			}
		}
		return list;
	}

}
