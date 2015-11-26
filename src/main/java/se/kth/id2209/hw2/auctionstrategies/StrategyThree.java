package se.kth.id2209.hw2.auctionstrategies;

import jade.lang.acl.ACLMessage;
import se.kth.id2209.hw2.exhibition.CuratorAgent;


public class StrategyThree extends Strategy
{
    public StrategyThree(ACLMessage msg, CuratorAgent curatorAgent, BidSettings bidSettings)
    {
        super(msg, curatorAgent, bidSettings);
    }


    @Override
    public void action()
    {
        //Accept once we reach the preferred price of the bidder
        if (getAuction().getCurrentPrice() <= getBidSettings().getPreferredPrice() * 0.7)
        {
            setShouldBuy(true);
            setSuggestPrice(getAuction().getCurrentPrice());
        } else
        {
            setShouldBuy(false);
            setSuggestPrice(getBidSettings().getPreferredPrice());
        }


        proceed();
    }
}
