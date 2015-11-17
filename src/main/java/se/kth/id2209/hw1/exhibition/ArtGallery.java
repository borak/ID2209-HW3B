/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.id2209.hw1.exhibition;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *  A gallery/museum contains detailed information of artifacts such as:
 *     id, name, creator, date of creation, place of creation, genre etc.
 * 
 * @author Kim
 */
class ArtGallery {
    
    Map<Integer, Artifact> artifacts = new HashMap<Integer, Artifact>();
    
    ArtGallery() {
        
    }
    
}
