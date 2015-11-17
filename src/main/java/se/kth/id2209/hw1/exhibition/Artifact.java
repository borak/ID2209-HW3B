/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.id2209.hw1.exhibition;

import java.io.Serializable;
import java.util.Date;

/**
 *  id, name, creator, date of creation, place of creation, genre etc.
 * @author Kim
 */
public class Artifact implements Serializable {
	private static final long serialVersionUID = 1L;
	private int id;
    private String name, creator, placeofCreation;
    private Date creationDate;
    private GENRE genre;
    enum GENRE {
    	LITERATURE, MUSIC, PAINTING, SCULPTURE, FASHION, JEWELRY;
    }

    public Artifact(int id, String name, String creator, String placeofCreation, Date creationDate, GENRE genre) {
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

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public String getPlaceofCreation() {
		return placeofCreation;
	}

	public void setPlaceofCreation(String placeofCreation) {
		this.placeofCreation = placeofCreation;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public GENRE getGenre() {
		return genre;
	}

	public void setGenre(GENRE genre) {
		this.genre = genre;
	}

    
}


