package org.marketplace.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain model that mirrors the {@code users} table.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    private Long   id;
    private String username;
    private String passwordHash;
    private String fullName;
    private Role   role;

    /** The three roles supported by the marketplace. */
    public enum Role {
        ADMIN, SELLER, CUSTOMER
    }
}
