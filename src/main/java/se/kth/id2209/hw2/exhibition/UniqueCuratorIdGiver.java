package se.kth.id2209.hw2.exhibition;

/**
 * Gives a curator a unique ID
 *
 */
class UniqueCuratorIdGiver {
    private static int curatorCounter = 0;
    static final int FIRST_ID = 1;
    
    synchronized static int createUniqueId() {
        return ++curatorCounter;
    }
}
