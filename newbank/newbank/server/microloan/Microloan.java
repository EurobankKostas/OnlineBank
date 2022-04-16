package newbank.server.microloan;

import newbank.server.Customer;

public class Microloan {

    private Customer loaner;
    private Customer taker;
    private float amount;
    private float interest;
    private String dateOfExpiry;
    private  boolean repaid;

    public Customer getLoaner() {
        return loaner;
    }

    public void setLoaner(Customer loaner) {
        this.loaner = loaner;
    }

    public Customer getTaker() {
        return taker;
    }

    public void setTaker(Customer taker) {
        this.taker = taker;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public float getInterest() {
        return interest;
    }

    public void setInterest(float interest) {
        this.interest = interest;
    }

    public String getDateOfExpiry() {
        return dateOfExpiry;
    }

    public void setDateOfExpiry(String dateOfExpiry) {
        this.dateOfExpiry = dateOfExpiry;
    }

    public boolean getRepaid() {
        return repaid;
    }

    public void setRepaid(boolean repaid) {
        this.repaid = repaid;
    }


    @Override
    public String toString() {
        return "Microloan{" +
                " amount=" + amount +
                ", interest=" + interest +
                ", dateOfExpiry='" + dateOfExpiry + '\'' +
                ", repaid=" + repaid +
                '}';
    }
}
