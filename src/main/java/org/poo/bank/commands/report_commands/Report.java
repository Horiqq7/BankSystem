package org.poo.bank.commands.report_commands;

import org.poo.bank.account.Account;
import org.poo.bank.transaction.Transaction;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Report extends AbstractReportCommand {

    @Override
    protected String getCommandName() {
        return "report";
    }

    @Override
    protected Map<String, Object> generateReport(Account account, CommandInput command) {
        int startTimestamp = command.getStartTimestamp();
        int endTimestamp = command.getEndTimestamp();

        // Filtrăm tranzacțiile
        List<Transaction> transactions = account.getTransactions();
        List<Map<String, Object>> filteredTransactions = new ArrayList<>();
        for (Transaction transaction : transactions) {
            if (transaction.getTimestamp() >= startTimestamp && transaction.getTimestamp() <= endTimestamp) {
                filteredTransactions.add(transaction.toMap());
            }
        }

        // Creăm structura raportului
        Map<String, Object> reportDetails = new HashMap<>();
        reportDetails.put("IBAN", account.getIBAN());
        reportDetails.put("balance", account.getBalance());
        reportDetails.put("currency", account.getCurrency());
        reportDetails.put("transactions", filteredTransactions);

        return reportDetails;
    }
}
