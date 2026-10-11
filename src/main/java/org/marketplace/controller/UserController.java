package org.marketplace.controller;

import lombok.RequiredArgsConstructor;
import org.marketplace.model.User;
import org.marketplace.service.UserService;
import org.marketplace.util.InputUtil;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.CellStyle;
import org.nocrala.tools.texttablefmt.ShownBorders;
import org.nocrala.tools.texttablefmt.Table;

import java.sql.SQLException;
import java.util.List;

@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    public void users(User currentUser) throws SQLException {
        List<User> users = userService.users(currentUser);

        Table table = new Table(
                4,
                BorderStyle.UNICODE_BOX_DOUBLE_BORDER, ShownBorders.ALL
        );

        table.setColumnWidth(0, 8, 12);
        table.setColumnWidth(1, 15, 30);
        table.setColumnWidth(2, 25, 40);
        table.setColumnWidth(3, 15, 20);

        CellStyle center = new CellStyle(CellStyle.HorizontalAlign.CENTER);

        table.addCell("ID", center);
        table.addCell("USERNAME", center);
        table.addCell("FULL NAME", center);
        table.addCell("ROLE", center);

        for (User user : users) {
            table.addCell(String.valueOf(user.getId()));
            table.addCell(clean(user.getUsername()));
            table.addCell(clean(user.getFullName()));
            table.addCell(clean(user.getRole()));
        }

        System.out.println("\n=== USERS ===");
        System.out.println(table.render());

        if (users.isEmpty()) {
            System.out.println("No users found.");
        } else {
            System.out.println("Total users: " + users.size());
        }
    }

    public void profile(User currentUser) throws SQLException {
        InputUtil.table(userService.profile(currentUser));

        if (!InputUtil.yes("Update full name?")) {
            return;
        }

        String fullName = InputUtil.required(
                "New full name: ",
                100
        );

        userService.updateName(currentUser, fullName);

        InputUtil.message("Profile updated successfully.");

        InputUtil.table(userService.profile(currentUser));
    }

    private static String clean(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace('\n', ' ')
                .replace('\r', ' ')
                .replace('\t', ' ');
    }
}