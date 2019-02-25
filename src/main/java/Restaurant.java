class RestaurantBill {
    private double mealCharge = 0;
    private double tax = 0;
    private double tip = 0;
    private double total = 0;

    public void setMealCharge(double price) {
        this.mealCharge = price;
        this.tax = 0.08 * price;
        this.tip = 0.12 * price;
        this.total = mealCharge + tax + tip;
    }

    public double getTotal() {
        return this.total;
    }
}

public class Restaurant {
    public static void main(String[] args) {
    }
}
