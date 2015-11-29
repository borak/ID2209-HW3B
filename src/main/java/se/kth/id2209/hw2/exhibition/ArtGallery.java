package se.kth.id2209.hw2.exhibition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import se.kth.id2209.hw2.exhibition.Artifact.GENRE;

/**
 * This class represents a gallery of the applications art museum which contains
 * information about the museums artifacts.
 * 
 * The number of instances is controlled by being implemented as a singleton. An
 * outside entity will have to put appropriate artifacts in this class in order
 * for any lookup to make hits.
 *
 */
class ArtGallery {

    HashMap<Integer, Artifact> map = new HashMap<Integer, Artifact>();
    private static ArtGallery instance = new ArtGallery();

    private ArtGallery() {
    }

    ArrayList<Artifact> getArtList() {
        ArrayList<Artifact> list = new ArrayList<>();

        Iterator<Entry<Integer, Artifact>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            list.add(it.next().getValue());
        }
        return list;
    }

    void add(Artifact artifact) {
        int id = artifact.getId();
        map.put(id, artifact);
    }

    Artifact getArtifact(int id) {
        if (map.containsKey(id)) {
            return map.get(id);
        }
        return null;
    }

    static ArtGallery getInstance() {
        if (instance == null) {
            instance = new ArtGallery();
        }
        return instance;
    }
        
    ArrayList<Integer> getArtifactIdList(GENRE genre) {
        ArrayList<Integer> list = new ArrayList<>();

        Iterator<Entry<Integer, Artifact>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Artifact artifact = it.next().getValue();
            if (genre instanceof Artifact.GENRE) {
                list.add(artifact.getId());
            }
        }
        return list;
    }

    ArrayList<String> getArtifactNameList(GENRE genre) {
        ArrayList<String> list = new ArrayList<>();

        Iterator<Entry<Integer, Artifact>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            String name = it.next().getValue().getName();
            if (genre instanceof Artifact.GENRE) {
                list.add(name);
            }
        }
        return list;
    }

}
