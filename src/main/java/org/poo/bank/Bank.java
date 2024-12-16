package org.poo.bank;

import org.poo.bank.commands.*;
import org.poo.fileio.CommandInput;
import org.poo.fileio.ObjectInput;
import org.poo.users.User;


import java.util.*;


public class Bank {
    private final List<User> users = new ArrayList<>();
    private Map<String, Account> accounts = new HashMap<>();

    public Bank(ObjectInput inputData) {
        if (inputData.getUsers() != null) {
            for (var userInput : inputData.getUsers()) {
                User user = new User(userInput);
                users.add(user);
            }
        }
    }

    public List<Map<String, Object>> processCommand(CommandInput command) {
        List<Map<String, Object>> output = new ArrayList<>();
        switch (command.getCommand()) {
            case "printUsers":
                PrintUsers printUsers = new PrintUsers(users);
                return printUsers.execute();
            case "addAccount":
                AddAccount addAccount = new AddAccount(users);
                addAccount.addAccount(command);
                return Collections.emptyList();
            case "createCard":
                CreateCard createCard = new CreateCard(users);
                createCard.createCard(command);
                return Collections.emptyList();
            case "createOneTimeCard":
                CreateOneTimeCard createOneTimeCard = new CreateOneTimeCard(users);
                createOneTimeCard.createOneTimeCard(command);
                return Collections.emptyList();
            case "addFunds":
                AddFunds addFunds = new AddFunds(users);
                addFunds.addFunds(command);
                return Collections.emptyList();
            case "deleteAccount":
                DeleteAccount deleteAccount = new DeleteAccount(getUsers());
                Map<String, Object> deleteResponse = deleteAccount.deleteAccount(command);
                return Collections.singletonList(deleteResponse);
            case "deleteCard":
                DeleteCard deleteCard = new DeleteCard(users);
                deleteCard.deleteCard(command);
                return Collections.emptyList();
            case "setMinimumBalance":
                SetMinimumBalance setMinimumBalance = new SetMinimumBalance(users);
                setMinimumBalance.setMinimumBalance(command);
                return Collections.emptyList();
            case "payOnline":
                PayOnline payOnlineProcessor = new PayOnline(users);
                List<Map<String, Object>> response = payOnlineProcessor.payOnline(command, this);
                if (!response.isEmpty()) {
                    output.addAll(response);
                }
                return output;
            case "sendMoney":
                SendMoney sendMoneyProcessor = new SendMoney(users);
                return sendMoneyProcessor.sendMoney(command);
            case "setAlias":
                SetAlias setAliasProcessor = new SetAlias(users);
                setAliasProcessor.setAlias(command);
                return Collections.emptyList();
            case "printTransactions":
                PrintTransactions printTransactions = new PrintTransactions(users);
                return printTransactions.printTransactions(command);
            case "checkCardStatus":
                CheckCardStatus checkCardStatus = new CheckCardStatus();
                Map<String, Object> checkCardStatusResponse = checkCardStatus.execute(command, getUsers());
                return checkCardStatusResponse.isEmpty() ? Collections.emptyList() : List.of(checkCardStatusResponse);
            case "changeInterestRate":
                ChangeInterestRate changeInterestRate = new ChangeInterestRate();
                List<Map<String, Object>> changeInterestRateResponse = changeInterestRate.execute(command, getUsers());
                return changeInterestRateResponse.isEmpty() ? Collections.emptyList() : changeInterestRateResponse;
            case "splitPayment":
                SplitPayment splitPaymentProcessor = new SplitPayment(users);
                return splitPaymentProcessor.splitPayment(command);
            case "report":
                Report report = new Report(users);
                Map<String, Object> reportResponse = report.report(command);
                return Collections.singletonList(reportResponse);
            case "spendingsReport":
                SpendingsReport spendingsReport = new SpendingsReport();
                return spendingsReport.generateSpendingsReport(command, getUsers());
            case "addInterest":
                AddInterest addInterestProcessor = new AddInterest(users);
                return addInterestProcessor.addInterest(command);
            default:
                throw new IllegalArgumentException("Unknown command: " + command.getCommand());
        }
    }

    public List<User> getUsers() {
        return users;
    }

    public Map<String, Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(Map<String, Account> accounts) {
        this.accounts = accounts;
    }

}