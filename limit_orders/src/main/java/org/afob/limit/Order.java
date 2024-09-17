package org.afob.limit;


public class Order {
    //if buyOrSell ==true then Buy
    //if buyOrSell ==false then Sell
    private Boolean buyOrSell;
    private String productId;
    private int amount;
    private Double limit;

    @Override
    public String toString() {
        return "Order{" +
                "buyOrSell=" + buyOrSell +
                ", productId='" + productId + '\'' +
                ", amount=" + amount +
                ", limit=" + limit +
                '}';
    }

    public Order(Boolean buyOrSell , String productId , int amount , Double limit){
        this.buyOrSell = buyOrSell;
        this.productId= productId;
        this.limit= limit;
        this.amount= amount;
    }
    public Boolean getBuyOrSell() {
        return buyOrSell;
    }

    public void setBuyOrSell(Boolean buyOrSell) {
        this.buyOrSell = buyOrSell;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public Double getLimit() {
        return limit;
    }

    public void setLimit(Double limit) {
        this.limit = limit;
    }
}
