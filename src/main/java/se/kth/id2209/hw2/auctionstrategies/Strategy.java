package se.kth.id2209.hw2.auctionstrategies;

import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import se.kth.id2209.hw2.auction.Auction;
import se.kth.id2209.hw2.exhibition.CuratorAgent;

/**
 * Created by Rickard on 2015-11-24.
 */
public abstract class Strategy extends OneShotBehaviour
{
    private Auction auction;


    private ACLMessage msg;
    private CuratorAgent curatorAgent;
    private BidSettings bidSettings;
    private boolean shouldBuy = false;
    private int suggestPrice;

    Strategy(ACLMessage msg, CuratorAgent curatorAgent, BidSettings bidSettings)
    {
        super(curatorAgent);
        this.msg = msg;
        this.curatorAgent = curatorAgent;
        this.bidSettings = bidSettings;
    }

    @Override
    public abstract void action();

    //TODO Add new behaivour to Agent, (send bid to artistmanagementagent)
    protected void proceed()
    {
//        myAgent.addBehaviour();
    }


    public boolean shouldBuy()
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

    public int getSuggestPrice()
    {
        return suggestPrice;
    }

    public ACLMessage getMsg()
    {
        return msg;
    }

    public void setMsg(ACLMessage msg)
    {
        this.msg = msg;
    }
    public void setSuggestPrice(int suggestPrice)
    {
        this.suggestPrice = suggestPrice;
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
