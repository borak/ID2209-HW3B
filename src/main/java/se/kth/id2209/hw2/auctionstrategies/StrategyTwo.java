package se.kth.id2209.hw2.auctionstrategies;

import se.kth.id2209.hw2.auction.Auction;
import se.kth.id2209.hw2.exhibition.CuratorAgent;

/**
 * Created by Rickard on 2015-11-24.
 */
public class StrategyTwo extends Strategy
{
    public StrategyTwo(Auction auction, CuratorAgent curatorAgent, BidSettings bidSettings)
    {
        super(auction, curatorAgent, bidSettings);
    }


    @Override
    public void action() {
        //Accept once we reach the preferred price of the bidder
        if( getAuction().getCurrentPrice() <= getBidSettings().getPreferredPrice())
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
