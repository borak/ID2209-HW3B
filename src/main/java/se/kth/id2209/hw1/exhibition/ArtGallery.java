/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.id2209.hw1.exhibition;

import java.util.Date;

/**
 *  A gallery/museum contains detailed information of artifacts such as:
 *     id, name, creator, date of creation, place of creation, genre etc.
 * 
 * @author Kim
 */
class ArtGallery {
    int id;
    String name, creator, placeofCreation;
    Date creationDate;
    Genre genre;
    enum Genre {
        
    }

    public ArtGallery(int id, String name, String creator, String placeofCreation, Date creationDate, Genre genre) {
        this.id = id;
        this.name = name;
        this.creator = creator;
        this.placeofCreation = placeofCreation;
        this.creationDate = creationDate;
        this.genre = genre;
    }
    
}
