package se.kth.id2209.hw2.auction;

import jade.core.AID;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import se.kth.id2209.hw2.exhibition.Artifact;

/**
 *
 * @author Kim
 */
public class Auction implements Serializable {
    private List<AID> participants;
    private ArrayList<AID> participantsWhichRejected = new ArrayList();
    private int currentPrice, lowestPrice;
    private Object item;
    private boolean isDone;
    private Artifact.Quality quality;
    private AID winner = null;

    Auction(List<AID> participants, int currentPrice, int lowestPrice, 
            Object item, boolean isDone, Artifact.Quality quality) {
        this.participants = participants;
        this.currentPrice = currentPrice;
        this.item = item;
        this.isDone = isDone;
        this.quality = quality;
    }
    
    @Override
    public String toString() {
        return "Auction[item="+item+", price="+currentPrice+", isDone="+isDone
                +", numberOfParticipants="+participants.size()+"]";
    }
    
    public AID getWinner() {
        return winner;
    }
    
    void setWinner(AID winner) {
        this.winner = winner;
    }
    
    public Artifact.Quality getQuality() {
        if(isDone) {
            return quality;
        } else {
            return Artifact.Quality.UNKNOWN_QUALITY;
        }
    }

    void setQuality(Artifact.Quality quality) {
        this.quality = quality;
    }

    public List<AID> getParticipants() {
        return participants;
    }

    public boolean isParticipant(AID aid) {
        return participants.contains(aid);
    }
    
    void addParticipants(AID participant) {
        if(participant != null) {
            this.participants.add(participant);
        }
    }

    public int getCurrentPrice() {
        return currentPrice;
    }

    void setCurrentPrice(int price) {
        this.currentPrice = price;
    }
    
    int getLowestPrice() {
        return lowestPrice;
    }

    void setLowestPrice(int price) {
        this.lowestPrice = price;
    }

    public Object getItem() {
        return item;
    }

    void setItem(Object item) {
        this.item = item;
    }
    
    public boolean isDone() {
        return isDone;
    }

    void setIsDone(boolean isDone) {
        this.isDone = isDone;
    }

    void removeParticipant(AID sender) {
        participants.remove(sender);
    }

    void addParticipantWhoRejected(AID agent) {
        participantsWhichRejected.add(agent);
    }
    
    void removeParticipantWhoRejected(AID agent) {
        participantsWhichRejected.remove(agent);
    }
    
    List<AID> getParticipantsWhichRejected() {
        return participantsWhichRejected;
    }
}
