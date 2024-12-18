package org.poo.bank.commands.account_commands.card_commands;

import org.poo.bank.account.Account;
import org.poo.bank.cards.Card;
import org.poo.bank.transaction.Transaction;
import org.poo.fileio.CommandInput;
import org.poo.bank.users.User;
import org.poo.utils.Utils;

import java.util.List;

public class CreateCard {
    private final List<User> users;

    public CreateCard(List<User> users) {
        this.users = users;
    }

    public void createCard(CommandInput command) {
        // Găsim utilizatorul pe baza email-ului
        User user = findUserByEmail(command.getEmail());
        if (user == null) {
            return; // Ieșim dacă utilizatorul nu există
        }

        // Găsim contul asociat utilizatorului
        Account account = user.getAccountByIBAN(command.getAccount());
        if (account == null) {
            return; // Ieșim dacă contul nu există
        }

        // Generăm detaliile cardului
        String cardNumber = Utils.generateCardNumber();
        Card card = new Card(cardNumber, "active");

        // Adăugăm cardul la cont
        account.addCard(card);

        // Creăm tranzacția asociată adăugării cardului
        Transaction transaction = new Transaction(
                command.getTimestamp(),
                "New card created", // Descrierea tranzacției
                null, // Sender IBAN (nu există)
                account.getIBAN(), // Receiver IBAN (contul asociat)
                0, // Suma tranzacției (0 pentru crearea cardului)
                account.getCurrency(),
                "other", // Tipul transferului
                cardNumber, // Numărul cardului
                user.getEmail(), // Deținătorul cardului
                null, // Comerciant (nu este aplicabil)
                null, // Conturi implicate
                null,
                "createCard" // Tipul tranzacției
        );

        // Adăugăm tranzacția atât la utilizator, cât și la cont
        user.addTransaction(transaction);
        account.addTransaction(transaction);
    }

    private User findUserByEmail(String email) {
        return users.stream()
                .filter(user -> user.getEmail().equals(email))
                .findFirst()
                .orElse(null);
    }
}
