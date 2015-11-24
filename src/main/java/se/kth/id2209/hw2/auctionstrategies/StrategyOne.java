package se.kth.id2209.hw2.auctionstrategies;

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import se.kth.id2209.hw2.auction.Auction;
import se.kth.id2209.hw2.exhibition.BidSettings;
import se.kth.id2209.hw2.exhibition.CuratorAgent;

/**
 *
 * @author Kim
 */
public class StrategyOne extends Strategy {


    StrategyOne(Auction auction, CuratorAgent curatorAgent, BidSettings bidSettings)
    {
        super(auction, curatorAgent, bidSettings);
    }


    @Override
    public void action() {

        if( getAuction().getCurrentPrice() <= getBidSettings().getMaxPrice())
        {
            setShouldBuy(true);
        }

        //TODO Add new behaivour to Agent, (send bid to artistmanagementagent)

//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
