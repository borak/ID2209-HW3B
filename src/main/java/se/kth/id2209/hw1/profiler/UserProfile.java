/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.id2209.hw1.profiler;

import java.io.Serializable;
import java.util.List;

/**
 * The profile contains basic user information (age, occupation, gender,
 * interest, etc) and visited items (in our case museum artifacts).
 * 
 * TODO: Change getters & setters of list to be adding & removing elements.
 * 
 * @author Kim
 */
@SuppressWarnings("serial")
public class UserProfile implements Serializable {
    private int age;
    private String occupation;
    private GENDER gender;
    private List<String> interests;
    private List<Integer> visitedItems;
    public enum GENDER {
        male, female;
    }

    public UserProfile(int age, String occupation, GENDER gender, 
            List<String> interests, List<Integer> visitedItems) {
        this.age = age;
        this.occupation = occupation;
        this.gender = gender;
        this.interests = interests;
        this.visitedItems = visitedItems;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public GENDER getGender() {
        return gender;
    }

    public void setGender(GENDER gender) {
        this.gender = gender;
    }

    public List getInterests() {
        return interests;
    }

    public void setInterests(List interests) {
        this.interests = interests;
    }

    public List getVisitedItems() {
        return visitedItems;
    }

    public void addVisitedItem(Integer visitedItem) {
        this.visitedItems.add(visitedItem);
    }
    
    
}
