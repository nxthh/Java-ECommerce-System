package org.marketplace.controller;

import lombok.RequiredArgsConstructor;
import org.marketplace.model.User;
import org.marketplace.service.*;
import org.marketplace.util.InputUtil;

import java.io.IOException;
import java.sql.SQLException;

@RequiredArgsConstructor
public class ReportController {

    private final ReportService service;
    private final ImportExportService files;

    private String chooseType() {
        int choice = InputUtil.menu(
                "REPORT TYPE", "Back",
                "Revenue summary", "Daily sales", "Top products"
        );

        if (choice == 1) {
            return "SUMMARY";
        }

        if (choice == 2) {
            return "DAILY";
        }

        if (choice == 3) {
            return "TOP";
        }

        return null;
    }

    public void show(User user) throws SQLException {
        String type = chooseType();

        if (type != null) {
            InputUtil.table(service.generate(user, type));
        }
    }

    public void export(User user) throws SQLException, IOException {
        String type = chooseType();

        if (type == null) {
            return;
        }

        String filename = InputUtil.text(
                "Output CSV/XLSX path (Enter to cancel): "
        );

        if (!filename.isEmpty()
                && InputUtil.yes("Write file? Existing file will be replaced.")) {
            InputUtil.message(
                    "Exported to: "
                            + files.exportReport(user, type, filename)
            );
        }
    }
}