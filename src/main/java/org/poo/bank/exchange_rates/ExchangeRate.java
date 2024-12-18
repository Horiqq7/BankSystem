package org.poo.bank.exchange_rates;

import org.poo.fileio.ExchangeInput;

public class ExchangeRate {
    private String from;
    private String to;
    private double rate;
    private int timestamp;

    public ExchangeRate(ExchangeInput exchangeInput) {
        this.from = exchangeInput.getFrom();
        this.to = exchangeInput.getTo();
        this.rate = exchangeInput.getRate();
        this.timestamp = exchangeInput.getTimestamp();
    }

    @Override
    public String toString() {
        return "ExchangeRate{" +
                "from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", rate=" + rate +
                ", timestamp=" + timestamp +
                '}';
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }
}
