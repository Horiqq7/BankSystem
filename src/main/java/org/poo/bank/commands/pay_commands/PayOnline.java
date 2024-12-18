package org.poo.bank.commands.pay_commands;

import org.poo.bank.account.Account;
import org.poo.bank.cards.Card;
import org.poo.bank.cards.OneTimeCard;
import org.poo.bank.transaction.Transaction;
import org.poo.fileio.CommandInput;
import org.poo.bank.exchange_rates.ExchangeRateManager;
import org.poo.bank.user.User;
import org.poo.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;


public final class PayOnline {
    private List<User> users;

    public PayOnline(final List<User> users) {
        this.users = users;
    }

    /**
     * Proceseaza o plata online folosind comanda furnizata.
     *
     * Metoda verifica daca utilizatorul exista, gaseste cardul specificat,
     * verifica fondurile suficiente, efectueaza conversia valutara daca este
     * necesar si actualizeaza statusul cardului si al contului dupa caz.
     *
     * Se gestioneaza mai multe scenarii, inclusiv fonduri insuficiente,
     * carduri congelate si distrugerea cardurilor de unică folosință.
     *
     * @param command Comanda care contine detaliile platii.
     *
     * @return O lista de harti de raspuns care contin statusul comenzii,
     * incluzand erorile, daca este cazul.
     *         O lista goala indica procesarea cu succes.
     */
    public List<Map<String, Object>> payOnline(final CommandInput command) {
        List<Map<String, Object>> output = new ArrayList<>();

        User user = User.findByEmail(users, command.getEmail());
        if (user == null) {
            Map<String, Object> errorNode = new HashMap<>();
            errorNode.put("description", "User not found");
            Map<String, Object> response = new HashMap<>();
            response.put("command", "payOnline");
            response.put("output", errorNode);
            output.add(response);
            return output;
        }

        Account account = null;
        Card card = null;

        for (Account acc : user.getAccounts()) {
            card = acc.getCardByNumber(command.getCardNumber());
            if (card != null) {
                account = acc;
                break;
            }
        }

        if (card == null) {
            Map<String, Object> errorNode = new HashMap<>();
            errorNode.put("description", "Card not found");

            Map<String, Object> response = new HashMap<>();
            response.put("command", "payOnline");
            response.put("output", errorNode);
            output.add(response);

            return output;
        }

        double amount = command.getAmount();
        String currency = command.getCurrency();
        double availableBalance = account.getBalance();

        if (!currency.equalsIgnoreCase(account.getCurrency())) {
            try {
                amount = ExchangeRateManager.getInstance()
                        .convertCurrency(currency, account.getCurrency(),
                                amount);
            } catch (IllegalArgumentException e) {
                Map<String, Object> errorNode = new HashMap<>();
                errorNode.put("description", "Exchange rates not available");
                Map<String, Object> response = new HashMap<>();
                response.put("command", "payOnline");
                response.put("output", errorNode);
                output.add(response);

                return output;
            }
        }

        if (availableBalance < amount && card.getStatus().equals("active")) {
            Transaction transaction1 = new Transaction(
                    command.getTimestamp(),
                    "Insufficient funds",
                    null,
                    null,
                    0,
                    account.getCurrency(),
                    "payment",
                    command.getCardNumber(),
                    user.getEmail(), null,
                    null,
                    null,
                    "payOnlineInsufficientFunds"
            );
            user.addTransaction(transaction1);
            return output;
        }

        if (amount > availableBalance - account.getMinimumBalance()) {
            card.setStatus("frozen");
        }

        if (card.getStatus().equals("frozen")) {
            Transaction transaction = new Transaction(
                    command.getTimestamp(),
                    "The card is frozen",
                    account.getIban(),
                    command.getCommerciant(),
                    0,
                    account.getCurrency(),
                    "payment",
                    command.getCardNumber(),
                    user.getEmail(),
                    command.getCommerciant(),
                    null,
                    null,
                    "payOnlineCardIsFrozen"
            );

            user.addTransaction(transaction);
            return output;
        }

        account.withdrawFunds(amount);
        Transaction transaction = new Transaction(
                command.getTimestamp(),
                "Card payment",
                account.getIban(),
                command.getCommerciant(),
                amount,
                account.getCurrency(),
                "payment",
                command.getCardNumber(),
                user.getEmail(),
                command.getCommerciant(),
                null,
                null,
                "payOnline"
        );

        user.addTransaction(transaction);
        account.addTransaction(transaction);

        if (card instanceof OneTimeCard) {
            Transaction destroyCardTransaction = new Transaction(
                    command.getTimestamp(),
                    "The card has been destroyed",
                    account.getIban(),
                    null,
                    0,
                    account.getCurrency(),
                    "other",
                    card.getCardNumber(),
                    user.getEmail(),
                    null,
                    null,
                    null,
                    "destroyOneTimeCard"
            );
            user.addTransaction(destroyCardTransaction);
            account.addTransaction(destroyCardTransaction);
            String newCardNumber = Utils.generateCardNumber();
            card.setCardNumber(newCardNumber);
            card.setStatus("active");

            Transaction newCardTransaction = new Transaction(
                    command.getTimestamp(),
                    "New card created",
                    account.getIban(),
                    null,
                    0,
                    account.getCurrency(),
                    "other",
                    newCardNumber,
                    user.getEmail(),
                    null,
                    null,
                    null,
                    "newOneTimeCard"
            );
            user.addTransaction(newCardTransaction);
            account.addTransaction(newCardTransaction);
        }

        return Collections.emptyList();
    }
}
