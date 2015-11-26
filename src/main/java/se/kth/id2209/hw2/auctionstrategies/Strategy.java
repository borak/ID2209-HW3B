package se.kth.id2209.hw2.auctionstrategies;

import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import se.kth.id2209.hw2.auction.Auction;
import se.kth.id2209.hw2.exhibition.CuratorAgent;
import se.kth.id2209.hw2.util.Ontologies;

import java.io.IOException;

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
        try {
            if (msg.getContentObject() != null && msg.getContentObject() instanceof Auction) {
                this.auction = (Auction) msg.getContentObject();


            } else {
                block();
            }
        } catch (UnreadableException ex) {
            block();
        }
    }

    @Override
    public abstract void action();

    //Responds with a bid
    protected void proceed()
    {
        if (msg != null)
        {
            String ontology = msg.getOntology();
           
            if (ontology.equalsIgnoreCase(Ontologies.CALL_FOR_PROPOSALS))
            {
                ACLMessage reply = msg.createReply();
                reply.addReceiver(msg.getSender());
                try
                {
                    reply.setContentObject(suggestPrice);
                    reply.setContent(msg.getContent());


                myAgent.send(reply);
                    System.out.println(myAgent.getName() + " SENDING message: "
                        + reply.getOntology());
                } catch (IOException e)
                {
                     System.err.println(myAgent.getName() + " COULD NOT SEND: "
                        + msg.getOntology());
                     block();
                }
                
            } else {
                block();
            }
        }
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
