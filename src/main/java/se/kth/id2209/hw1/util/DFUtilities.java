package se.kth.id2209.hw1.util;

import static jade.tools.sniffer.Agent.i;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;

/**
 * This class provides some utilities for communicating via the DFService. More
 * specifically, searching for agents.
 * 
 * @author Kim
 */
public class DFUtilities {

    /**
     * Searches for all agents with the provided description in the DFService.
     * 
     * @param agent The agent that wishes to perform the search.
     * @param dfd A description of the agent(s).
     * @return The agents which the DFService matched to the description.
     */
    public static AID[] searchDF(Agent agent, DFAgentDescription dfd) {
        try {
            DFAgentDescription[] result = DFService.search(agent, dfd);
            AID[] agents = new AID[result.length];
            for (i = 0; i < result.length; i++) {
                agents[i] = result[i].getName();
            }

            return agents;
        } catch (Exception fe) {
            fe.printStackTrace();
        }

        return null;
    }

    /**
     * Searches for any agent with the provided type in the DFService.
     * 
     * @param agent The agent that wishes to perform the search.
     * @param type The name of the agent type to look for.
     * @return The agent which the DFService matched to the description.
     */
    public static AID searchDF(Agent agent, String type) {
        DFAgentDescription dfdTGA = new DFAgentDescription();
        ServiceDescription sdTGA = new ServiceDescription();
        sdTGA.setType(type);
        dfdTGA.addServices(sdTGA);
        try {
            DFAgentDescription[] result = DFService.search(agent, dfdTGA);
            AID[] agents = new AID[result.length];
            for (i = 0; i < result.length; i++) {
                agents[i] = result[i].getName();
            }

            return agents[0];
        } catch (Exception fe) {
            fe.printStackTrace();
        }

        return null;
    }

    /**
     * Searches for all agents with the provided service in the DFService.
     * 
     * @param agent The agent that wishes to perform the search.
     * @param type The name of the agent type to look for.
     * @return The agents which the DFService matched to the description.
     */
    public static AID[] searchAllDF(Agent agent, String type) {
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(type);
        dfd.addServices(sd);
        SearchConstraints ALL = new SearchConstraints();
        ALL.setMaxResults(new Long(-1));
        try {
            DFAgentDescription[] result = DFService.search(agent, dfd, ALL);
            AID[] agents = new AID[result.length];
            for (i = 0; i < result.length; i++) {
                agents[i] = result[i].getName();
            }

            return agents;
        } catch (Exception fe) {
            fe.printStackTrace();
        }

        return null;
    }

}
