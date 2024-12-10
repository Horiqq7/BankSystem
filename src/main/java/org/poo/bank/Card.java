package org.poo.bank;

import java.util.HashMap;
import java.util.Map;

public class Card {
    private final String cardNumber;
    private String status;

    public Card(String cardNumber, String status) {
        this.cardNumber = cardNumber;
        this.status = status;
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
