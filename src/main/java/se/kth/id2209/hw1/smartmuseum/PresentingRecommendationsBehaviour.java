package se.kth.id2209.hw1.smartmuseum;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import se.kth.id2209.hw1.profiler.UserProfile;

/**
 * On each tick this behavior will go through the list of recommended items 
 * contained in the tour guide agent and send out messages to each corresponding
 * user. On completion, it will clear that list.
 * 
 * @author Kim
 */
@SuppressWarnings("serial")
class PresentingRecommendationsBehaviour extends TickerBehaviour {
    private TourGuideAgent tourGuide;

    public PresentingRecommendationsBehaviour(Agent a, long period) {
        super(a, period);
        this.tourGuide = (TourGuideAgent) a;
    }

    @Override
    protected void onTick() {
        TourGuideAgent.usersLock.lock();
        HashMap<AID, UserProfile> users = tourGuide.getUsers();
        Map<AID, List<ACLMessage>> responses = tourGuide.getResponses();

        if (users.isEmpty()) {
            block();
        }

        ParallelBehaviour par = new ParallelBehaviour(ParallelBehaviour.WHEN_ALL);

        try {
            Iterator<Entry<AID, UserProfile>> it = users.entrySet().iterator();
            while (it.hasNext()) {
                Entry<AID, UserProfile> entry = it.next();
                AID aid = entry.getKey();
                List<ACLMessage> messages = responses.get(aid);

                for (ACLMessage message : messages) {
                    par.addSubBehaviour(new SendBehavior(tourGuide, aid, message));
                }
            }
            responses.clear();
        } finally {
            TourGuideAgent.usersLock.unlock();
        }
        tourGuide.addBehaviour(par);
    }
}
