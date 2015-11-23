package se.kth.id2209.hw2.exhibition;

import java.io.Serializable;
import java.util.StringTokenizer;

/**
 * An Artifact contains detailed information about an artifact such as: id, 
 * name, creator, date of creation, place of creation, genre etc.
 *
 * To the outside of this package it will serve as an DTO for protecting its 
 * attributes and should only only be initialized from a class within its 
 * package.
 * 
 * @author Kim
 */
public class Artifact implements Serializable {
    private int id;
    private String name, creator, placeofCreation, creationDate;
    private GENRE genre;

    public enum GENRE {
        LITERATURE, MUSIC, PAINTING, SCULPTURE, FASHION, JEWELRY;
    }

    Artifact() {
        
    }
    
    Artifact(String dbline) {
        StringTokenizer tokenizer = new StringTokenizer(dbline, "/");

        this.id = Integer.parseInt(tokenizer.nextToken());
        this.name = tokenizer.nextToken();
        this.creator = tokenizer.nextToken();
        this.placeofCreation = tokenizer.nextToken();
        this.creationDate = tokenizer.nextToken();
        this.genre = GENRE.valueOf(tokenizer.nextToken());
    }

    @Override
    public String toString() {
        return id + "/" + name + "/" + creator + "/" + placeofCreation + "/"
                + creationDate + "/" + genre;
    }

    Artifact(int id, String name, String creator, String placeofCreation, 
            String creationDate, GENRE genre) {
        this.id = id;
        this.name = name;
        this.creator = creator;
        this.placeofCreation = placeofCreation;
        this.creationDate = creationDate;
        this.genre = genre;
    }

    public int getId() {
        return id;
    }

    void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    public String getCreator() {
        return creator;
    }

    void setCreator(String creator) {
        this.creator = creator;
    }

    public String getPlaceofCreation() {
        return placeofCreation;
    }

    void setPlaceofCreation(String placeofCreation) {
        this.placeofCreation = placeofCreation;
    }

    public String getCreationDate() {
        return creationDate;
    }

    void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

}
