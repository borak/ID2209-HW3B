package se.kth.id2209.hw1.profiler;

import java.io.Serializable;
import java.util.List;

/**
 * The User Profile contains basic user information (age, occupation, gender,
 * interest, etc) and visited items (artifacts from the museum).
 * 
 * This class is protected to only be manipulated from an entity in the package
 * but has public access to read its content.
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

    UserProfile(int age, String occupation, GENDER gender, 
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

    void setAge(int age) {
        this.age = age;
    }

    public String getOccupation() {
        return occupation;
    }

    void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public GENDER getGender() {
        return gender;
    }

    void setGender(GENDER gender) {
        this.gender = gender;
    }

    public List<String> getInterests() {
        return interests;
    }

    void setInterests(List<String> interests) {
        this.interests = interests;
    }

    public List<Integer> getVisitedItems() {
        return visitedItems;
    }

    public void addVisitedItem(Integer visitedItem) {
        this.visitedItems.add(visitedItem);
    }
    
    
}
