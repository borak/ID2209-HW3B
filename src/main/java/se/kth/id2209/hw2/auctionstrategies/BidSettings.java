package se.kth.id2209.hw2.auctionstrategies;


/**
 * Bidsettings contains settings for a specific auction for a specific bidder.
 */
public class BidSettings
{

    private int maxPrice;
    private int preferredPrice;
    private int strategy;

    /**
     *
     * The highest price that the bidder will accept
     * @param maxPrice
     * The preferred price that the bidder would like
     * @param preferredPrice
     * Which strategy the bidder should use. 0 means that strategies will be evenly distributed.
     *      1-4 selects a specific strategy.
     * @param strategy
     */
    public BidSettings(int maxPrice, int preferredPrice, int strategy)
    {
        this.maxPrice = maxPrice;
        this.preferredPrice = preferredPrice;
        this.strategy = strategy;
    }

    public int getMaxPrice()
    {
        return maxPrice;
    }

    public int getStrategy()
    {
        return strategy;
    }

    public void setPreferredPrice(int preferredPrice)
    {
        this.preferredPrice = preferredPrice;
    }

    public void setStrategy(int strategy)
    {
        this.strategy = strategy;
    }

    public int getPreferredPrice()
    {
        return preferredPrice;
    }



}
