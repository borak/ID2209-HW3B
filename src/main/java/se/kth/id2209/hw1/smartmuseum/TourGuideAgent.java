/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.id2209.hw1.smartmuseum;

import jade.core.Agent;
import se.kth.id2209.hw1.exhibition.CuratorAgent;

/**
 * Tour Guide Agent retrieves the information about artifacts in the
 * gallery/museum and builds a virtual tour (upon the request) for profiler 
 * agent.
 * 
 * –The virtual tour contains list of related items (based on user’s interest, 
 * age, etc..)
 * 
 *  Tour Guide agent interacts with Curator Agent in order to build the virtual 
 * tour.
 * 
 * TODO: Implement behaviours. 
 *       Behaviors should correspond to each category below:
 *           Simple Behavior (at least 5 different behaviors):
 *              – CyclicBehaviour, MsgReceiver, OneShotBehaviour,
 *                SimpleAchieveREInitiator, SimpleAchieveREResponder,
 *                TickerBehaviour, WakerBehaviour
 *           Composite Behaviors (at least 2 different behaviors):
 *              – ParallelBehaviour, FSMBehaviour, SequentialBehaviour
 * 
 * @author Kim
 */
public class TourGuideAgent extends Agent {
    private CuratorAgent cAgent; // temporary - register at DF instead
}
