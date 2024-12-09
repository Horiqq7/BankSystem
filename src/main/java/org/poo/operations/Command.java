package org.poo.operations;

import org.poo.fileio.CommandInput;

import java.util.List;

public class Command {
    private String command;
    private String email;
    private String account;
    private String currency;
    private double amount;
    private double minBalance;
    private String target;
    private String description;
    private String cardNumber;
    private String commerciant;
    private int timestamp;
    private int startTimestamp;
    private int endTimestamp;
    private String receiver;
    private String alias;
    private String accountType;
    private double interestRate;
    private List<String> accounts;

    public Command(CommandInput commandInput) {
        this.account = commandInput.getAccount();
        this.command = commandInput.getCommand();
        this.email = commandInput.getEmail();
        this.currency = commandInput.getCurrency();
        this.amount = commandInput.getAmount();
        this.minBalance = commandInput.getMinBalance();
        this.target = commandInput.getTarget();
        this.description = commandInput.getDescription();
        this.cardNumber = commandInput.getCardNumber();
        this.commerciant = commandInput.getCommerciant();
        this.timestamp = commandInput.getTimestamp();
        this.endTimestamp = commandInput.getEndTimestamp();
        this.receiver = commandInput.getReceiver();
        this.alias = commandInput.getAlias();
        this.accountType = commandInput.getAccountType();
        this.interestRate = commandInput.getInterestRate();
        this.accounts = commandInput.getAccounts();
    }

}
