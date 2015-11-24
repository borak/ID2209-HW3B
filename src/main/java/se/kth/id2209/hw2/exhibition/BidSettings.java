package se.kth.id2209.hw2.exhibition;

/**
 * Created by Rickard on 2015-11-24.
 */
public class BidSettings
{

    private int maxPrice;
    private int preferredPrice;


    BidSettings(int maxPrice, int preferredPrice)
    {
        this.maxPrice = maxPrice;
        this.preferredPrice = preferredPrice;
    }

    public int getMaxPrice()
    {
        return maxPrice;
    }

    public int getPreferredPrice()
    {
        return preferredPrice;
    }
}
