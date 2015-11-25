/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.id2209.hw2.exhibition;

/**
 *
 * @author Kim
 */
class UniqueCuratorIdGiver {
    private static int curatorCounter = 0;
    static final int FIRST_ID = 1;
    
    synchronized static int createUniqueId() {
        return ++curatorCounter;
    }
}
