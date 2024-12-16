package org.poo.bank.commands;

import org.poo.bank.Account;
import org.poo.bank.Card;
import org.poo.bank.Transaction;
import org.poo.fileio.CommandInput;
import org.poo.users.User;

import java.util.List;

public class DeleteCard {
    private final List<User> users;

    public DeleteCard(List<User> users) {
        this.users = users;
    }

    public void deleteCard(CommandInput command) {
        // Găsim utilizatorul pe baza email-ului
        User user = findUserByEmail(command.getEmail());
        if (user == null) {
            return; // Ieșim dacă utilizatorul nu există
        }

        // Iterăm prin toate conturile utilizatorului
        for (Account account : user.getAccounts()) {
            // Găsim cardul pe baza numărului de card
            Card card = account.getCardByNumber(command.getCardNumber());
            if (card != null) {
                // Eliminăm cardul din cont
                account.removeCard(card);

                // Creăm tranzacția asociată ștergerii cardului
                Transaction transaction = new Transaction(
                        command.getTimestamp(),
                        "The card has been destroyed", // Descrierea tranzacției
                        account.getIBAN(), // Sender IBAN
                        null, // Receiver IBAN
                        0, // Suma tranzacției
                        account.getCurrency(), // Moneda tranzacției
                        "other", // Tipul transferului
                        card.getCardNumber(), // Numărul cardului
                        user.getEmail(), // Email-ul utilizatorului
                        null, // Comerciant (nu este aplicabil)
                        null, // Conturi implicate (nu este aplicabil)
                        null, // Alte informații
                        "deleteCard" // Tipul tranzacției
                );

                // Adăugăm tranzacția la utilizator
                user.addTransaction(transaction);
                return; // Ieșim după ștergerea cardului
            }
        }
    }

    private User findUserByEmail(String email) {
        for (User user : users) {
            if (user.getEmail().equals(email)) {
                return user;
            }
        }
        return null;
    }
}
