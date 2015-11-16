/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.id2209.hw1.exhibition;

import jade.core.Agent;
import se.kth.id2209.hw1.profiler.ProfilerAgent;
import se.kth.id2209.hw1.smartmuseum.TourGuideAgent;

/**
 *  Curator Agent monitors the gallery/museum.
 *  A gallery/museum contains detailed information of artifacts such as:
 *       id, name, creator, date of creation, place of creation, genre etc.
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
public class CuratorAgent extends Agent {
    private ProfilerAgent pAgent; // temporary - register at DF instead
    private TourGuideAgent tgAgent; // temporary - register at DF instead
    private ArtGallery artGallery;
}
