package org.poo.bank;

import java.util.HashMap;
import java.util.Map;

public class Transaction {
    private final int timestamp;
    private final String description;
    private final String senderIBAN;
    private final String receiverIBAN;
    private final double amount;
    private final String currency;
    private final String transferType;
    private final String card; // Numărul cardului
    private final String cardHolder; // Deținătorul cardului
    private final String transactionType;
    private final String commerciant;

    public Transaction(int timestamp, String description, String senderIBAN, String receiverIBAN,
                       double amount, String currency, String transferType, String card, String cardHolder, String commerciant,
                       String transactionType) {
        this.timestamp = timestamp;
        this.description = description;
        this.senderIBAN = senderIBAN;
        this.receiverIBAN = receiverIBAN;
        this.amount = amount;
        this.currency = currency;
        this.transferType = transferType;
        this.card = card;
        this.cardHolder = cardHolder;
        this.commerciant = commerciant;
        this.transactionType = transactionType;
    }

    public String getCommerciant() {
        return commerciant;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();

        switch (transactionType) {
            case "createCard":
                map.put("timestamp", timestamp);
                map.put("description", description);
                map.put("card", card);
                map.put("cardHolder", cardHolder);
                map.put("account", receiverIBAN);
                break;
            case "addAccount":
                map.put("timestamp", timestamp);
                map.put("description", description);
                break;
            case "sendMoney":
                map.put("timestamp", timestamp);
                map.put("description", description);
                map.put("senderIBAN", senderIBAN);
                map.put("receiverIBAN", receiverIBAN);
                map.put("amount", String.format("%.1f %s", amount, currency));
                map.put("transferType", transferType);
                break;
            case "sendMoneyInsufficientFunds":
                map.put("timestamp", timestamp);
                map.put("description", description);
                break;
            case "payOnlineInsufficentFunds":
                map.put("timestamp", timestamp);
                map.put("description", description);
                break;
            case "payOnline":
                map.put("timestamp", timestamp);
                map.put("description", description);
                map.put("commerciant", commerciant);
                map.put("amount", amount);
                break;
            case "payOnlineCardIsFrozen":
                map.put("timestamp", timestamp);
                map.put("description", description);
                break;
            case "deleteCard":
                map.put("timestamp", timestamp);
                map.put("card", card);
                map.put("account", senderIBAN);
                map.put("cardHolder", cardHolder);
                map.put("description", description);
                break;
            case "checkCardStatusFrozen":
                map.put("timestamp", timestamp);
                map.put("description", description);
                break;
            default:
                break;
        }
        return map;
    }

    public String getCard() {
        return card;
    }

    public String getCardHolder() {
        return cardHolder;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public String getDescription() {
        return description;
    }

    public String getSenderIBAN() {
        return senderIBAN;
    }

    public String getReceiverIBAN() {
        return receiverIBAN;
    }

    public double getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getTransferType() {
        return transferType;
    }
}
