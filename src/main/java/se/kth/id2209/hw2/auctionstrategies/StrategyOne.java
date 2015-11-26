package se.kth.id2209.hw2.auctionstrategies;

import jade.lang.acl.ACLMessage;
import se.kth.id2209.hw2.auction.Auction;
import se.kth.id2209.hw2.exhibition.CuratorAgent;

/**
 *
 * @author Kim
 */
public class StrategyOne extends Strategy {


    public StrategyOne(ACLMessage msg, CuratorAgent curatorAgent, BidSettings bidSettings)
    {
        super(msg, curatorAgent, bidSettings);
    }

    @Override
    public void action() {
        //Accept once we reach the maximum price of the bidder
        if( getAuction().getCurrentPrice() <= getBidSettings().getMaxPrice())
        {
            setShouldBuy(true);
            setSuggestPrice(getAuction().getCurrentPrice());
        }
        else
        {
            setShouldBuy(false);
            setSuggestPrice(getBidSettings().getPreferredPrice());
        }


        proceed();

//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
