package se.kth.id2209.hw2.auctionstrategies;

import jade.core.behaviours.OneShotBehaviour;
import se.kth.id2209.hw2.auction.Auction;
import se.kth.id2209.hw2.exhibition.BidSettings;
import se.kth.id2209.hw2.exhibition.CuratorAgent;

/**
 * Created by Rickard on 2015-11-24.
 */
public class Strategy extends OneShotBehaviour
{
    private Auction auction;
    private CuratorAgent curatorAgent;
    private BidSettings bidSettings;
    private boolean shouldBuy = false;

    Strategy(Auction auction, CuratorAgent curatorAgent, BidSettings bidSettings)
    {
        super(curatorAgent);
        this.auction = auction;
        this.curatorAgent = curatorAgent;
        this.bidSettings = bidSettings;
    }



    @Override
    public void action() {

        if( auction.getPrice() <= bidSettings.getMaxPrice())
        {
            shouldBuy = true;
        }




        //TODO Add new behaivour to Agent, (send bid to artistmanagementagent)

//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean isShouldBuy()
    {
        return shouldBuy;
    }

    public void setShouldBuy(boolean shouldBuy)
    {
        this.shouldBuy = shouldBuy;
    }

    public Auction getAuction()
    {
        return auction;
    }

    public void setAuction(Auction auction)
    {
        this.auction = auction;
    }

    public CuratorAgent getCuratorAgent()
    {
        return curatorAgent;
    }

    public void setCuratorAgent(CuratorAgent curatorAgent)
    {
        this.curatorAgent = curatorAgent;
    }

    public BidSettings getBidSettings()
    {
        return bidSettings;
    }

    public void setBidSettings(BidSettings bidSettings)
    {
        this.bidSettings = bidSettings;
    }

}
