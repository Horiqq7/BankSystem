package org.poo.bank.commands.report_commands;

import org.poo.bank.account.Account;
import org.poo.fileio.CommandInput;
import org.poo.bank.users.User;

import java.util.List;
import java.util.Map;

public abstract class AbstractReportCommand {

    protected abstract String getCommandName();

    protected abstract Map<String, Object> generateReport(Account account, CommandInput command);

    public Map<String, Object> process(CommandInput command, List<User> users) {
        String accountIBAN = command.getAccount();
        int currentTimestamp = command.getTimestamp();

        // Căutăm contul corespunzător
        Account account = null;
        for (User user : users) {
            account = user.getAccountByIBAN(accountIBAN);
            if (account != null) {
                break;
            }
        }

        if (account == null) {
            // Dacă contul nu există, returnăm eroarea cerută
            return Map.of(
                    "command", getCommandName(),
                    "output", Map.of(
                            "description", "Account not found",
                            "timestamp", currentTimestamp
                    ),
                    "timestamp", currentTimestamp
            );
        }

        // Delegăm generarea raportului clasei concrete
        return Map.of(
                "command", getCommandName(),
                "output", generateReport(account, command),
                "timestamp", currentTimestamp
        );
    }
}