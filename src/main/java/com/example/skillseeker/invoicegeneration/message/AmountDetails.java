package com.example.skillseeker.invoicegeneration.message;

/**
 * @author I077659
 */
public class AmountDetails {

    private float price;
    private float totalHours;

    public AmountDetails(float price, float totalHours){
        this.price = price;
        this.totalHours = totalHours;
    }
    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public float getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(float totalHours) {
        this.totalHours = totalHours;
    }
}
