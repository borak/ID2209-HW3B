/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.id2209.hw1.util;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import static jade.tools.sniffer.Agent.i;
import static jdk.nashorn.internal.objects.NativeString.search;

/**
 *
 * @author Kim
 */
public class DFUtilities {

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

    public static AID[] searchAllDF(Agent agent, String service) {
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(service);
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

    AID getService(Agent a, String service) {
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(service);
        dfd.addServices(sd);
        try {
            DFAgentDescription[] result = DFService.search(a, dfd);
            if (result.length > 0) {
                return result[0].getName();
            }
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        return null;
    }
}
