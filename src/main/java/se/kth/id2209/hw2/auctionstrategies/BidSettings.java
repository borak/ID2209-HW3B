package se.kth.id2209.hw2.auctionstrategies;

/**
 * Created by Rickard on 2015-11-24.
 */
public class BidSettings
{

    private int maxPrice;
    private int preferredPrice;
    private int strategy;


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
