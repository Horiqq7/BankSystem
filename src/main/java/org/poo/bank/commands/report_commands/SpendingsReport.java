package org.poo.bank.commands.report_commands;

import org.poo.bank.account.Account;
import org.poo.fileio.CommandInput;

import java.util.List;
import java.util.Map;

public class SpendingsReport extends AbstractReportCommand {

    @Override
    protected String getCommandName() {
        return "spendingsReport";
    }

    @Override
    protected Map<String, Object> generateReport(Account account, CommandInput command) {
        int startTimestamp = command.getStartTimestamp();
        int endTimestamp = command.getEndTimestamp();

        // Verificăm dacă este un cont de tip "savings"
        if ("savings".equalsIgnoreCase(account.getType())) {
            return Map.of(
                    "error", "This kind of report is not supported for a saving account"
            );
        }

        Map<String, Object> report = account.generateSpendingsReport(startTimestamp, endTimestamp);

        List<Map<String, Object>> commerciants = (List<Map<String, Object>>) report.get("commerciants");
        if (commerciants != null) {
            commerciants.sort((a, b) -> {
                String firstCommerciant = (String) a.get("commerciant");
                String secondCommerciant = (String) b.get("commerciant");
                return firstCommerciant.compareTo(secondCommerciant);
            });
        }

        return report;
    }
}
