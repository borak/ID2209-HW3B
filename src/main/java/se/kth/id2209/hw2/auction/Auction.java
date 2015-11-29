package se.kth.id2209.hw2.auction;

import jade.core.AID;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import se.kth.id2209.hw2.exhibition.Artifact;

/**
 * An auction object contains information about a specific auction, and is sent back and forth between
 *   the auctioneer and the bidders. Some information is hidden from bidders.
 */

public class Auction implements Serializable {

    private List<AID> participants;
    private ArrayList<AID> participantsWhichRejected = new ArrayList();
    private int currentPrice, lowestPrice;
    private Artifact artifact;
    private boolean isDone;
    private Artifact.Quality quality;
    private AID winner = null;
    int CFPCounter = 0;

    Auction(List<AID> participants, int currentPrice, int lowestPrice,
            Artifact item, boolean isDone, Artifact.Quality quality) {
        this.participants = participants;
        this.currentPrice = currentPrice;
        this.lowestPrice = lowestPrice;
        this.artifact = item;
        this.isDone = isDone;
        this.quality = quality;
    }

    @Override
    public String toString() {
        String winnerstr = "";
        if (winner == null) {
            winnerstr = "null";
        } else {
            winnerstr = winner.getName();
        }
        return "Auction[item=" + artifact
                + ", price=" + currentPrice
                + ", isDone=" + isDone
                + ", numberOfParticipants=" + participants.size()
                + ", winner=" + winnerstr
                + ", quality=" + getQuality()
                + ", CFPcounter=" + CFPCounter
                + "]";
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof Auction && 
                ((Auction)o).getArtifact() instanceof Artifact &&
                getArtifact() instanceof Artifact &&
                ((Artifact)((Auction)o).getArtifact()).getId() == getArtifact().getId()) {
            return true;            
        }
        return false;
    }

    public AID getWinner() {
        return winner;
    }

    void setWinner(AID winner) {
        this.winner = winner;
    }


    /**
     * Returns the quality if the auction is done, otherwise an unknown quality
     * @return
     */
    public Artifact.Quality getQuality() {
        if (isDone) {
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
        if (participant != null) {
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

    public Artifact getArtifact() {
        return artifact;
    }

    void setArtifact(Artifact item) {
        this.artifact = item;
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
