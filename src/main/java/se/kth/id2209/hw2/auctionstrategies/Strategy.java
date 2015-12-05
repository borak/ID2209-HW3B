package se.kth.id2209.hw2.auctionstrategies;

import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import se.kth.id2209.hw2.auction.Auction;
import se.kth.id2209.hw2.exhibition.CuratorAgent;
import se.kth.id2209.hw2.util.Ontologies;

import java.io.IOException;

/**
 * Strategy is a OneShotBehaviour that will decide what bid to put, and send a message to the auctioneer with the bid
 */
public abstract class Strategy extends OneShotBehaviour
{
    private Auction auction;
    private ACLMessage msg;
    private CuratorAgent curatorAgent;
    private BidSettings bidSettings;
    private boolean shouldBuy = false;
    private int suggestPrice;

    /**
     *
     * The constructor is called with the CFP message, the agent that received the message,
     *  and settings concerning preferred price etc
     *
     * The received CFP message
     * @param msg
     * The agent that received the message
     * @param curatorAgent
     * An object containing preferred price, maximum price and selected strategy. Unique for each auction and bidder
     * @param bidSettings
     */

    Strategy(ACLMessage msg, CuratorAgent curatorAgent, BidSettings bidSettings) {
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

    /**
     * Action is implemented by each of the strategies. Uses a strategy to decide what to bid,
     *  and then calls the proceed method of the Strategy class.
     */
    @Override
    public abstract void action();


    /**
     * Called by the action method of implementing classes. Proceed sends a message with a bid to the auctioneer
     */
    protected void proceed()
    {
        if (msg != null)
        {
            String ontology = msg.getOntology();

            if (ontology.equalsIgnoreCase(Ontologies.CALL_FOR_PROPOSALS)) {
                ACLMessage reply = new ACLMessage(msg.getPerformative());
                reply.setOntology(Ontologies.AUCTION_BID);
                reply.addReceiver(msg.getSender());
                System.out.println("SUGGESTING PRICE " + suggestPrice 
                        + " shouldbuy=" +shouldBuy() + " isClone=" 
                        + curatorAgent.isClone());
                try {
                    if (suggestPrice > 0 && shouldBuy()) {
                        reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                    } else {
                        reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                    }
                    Object[] objs = new Object[2];
                    objs[0] = auction.getArtifact().getId();
                    objs[1] = suggestPrice;
                    reply.setContentObject(objs);
                    myAgent.send(reply);
                } catch (IOException e) {
                    System.err.println(myAgent.getName() + " COULD NOT SEND: "
                            + msg.getOntology() + "" + e.getMessage());
                    e.printStackTrace();
                    block();
                }

            } else {
                block();
            }
        }
    }

    public boolean shouldBuy() {
        return shouldBuy;
    }

    public void setShouldBuy(boolean shouldBuy) {
        this.shouldBuy = shouldBuy;
    }

    public Auction getAuction() {
        return auction;
    }

    public void setAuction(Auction auction) {
        this.auction = auction;
    }

    public int getSuggestPrice() {
        return suggestPrice;
    }

    public ACLMessage getMsg() {
        return msg;
    }

    public void setMsg(ACLMessage msg) {
        this.msg = msg;
    }

    public void setSuggestPrice(int suggestPrice) {
        this.suggestPrice = suggestPrice;
    }

    public CuratorAgent getCuratorAgent() {
        return curatorAgent;
    }

    public void setCuratorAgent(CuratorAgent curatorAgent) {
        this.curatorAgent = curatorAgent;
    }

    public BidSettings getBidSettings() {
        return bidSettings;
    }

    public void setBidSettings(BidSettings bidSettings) {
        this.bidSettings = bidSettings;
    }

}
