package se.kth.id2209.hw2.auctionstrategies;

import jade.lang.acl.ACLMessage;
import se.kth.id2209.hw2.exhibition.CuratorAgent;

/**
 * Created by Rickard on 2015-11-25.
 */
public class StrategyFour extends Strategy
{
    public StrategyFour(ACLMessage msg, CuratorAgent curatorAgent, BidSettings bidSettings)
    {
        super(msg, curatorAgent, bidSettings);
    }


    @Override
    public void action()
    {
        //Accept once we reach the preferred price of the bidder
        if (getAuction().getCurrentPrice() <= getBidSettings().getMaxPrice())
        {

            setShouldBuy(true);
            setSuggestPrice(getAuction().getCurrentPrice());
        } else
        {
            setShouldBuy(false);
            setSuggestPrice(getBidSettings().getMaxPrice());
        }


        proceed();
    }
}
