package org.poo.bank;

import java.util.HashMap;
import java.util.Map;

public class Card {
    private final String cardNumber;
    private String status = "active";

    public Card(String cardNumber, String status) {
        this.cardNumber = cardNumber;
        this.status = status;
    }

    public void addTransaction(String description, String commerciant, double amount, String currency) {
        Map<String, Object> transaction = new HashMap<>();
        transaction.put("type", "payment");
        transaction.put("description", description);
        transaction.put("commerciant", commerciant);
        transaction.put("amount", amount);
        transaction.put("currency", currency);
        transaction.put("timestamp", System.currentTimeMillis());
        System.out.println("Transaction added: " + transaction);
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("cardNumber", cardNumber);
        map.put("status", status);
        return map;
    }
}
