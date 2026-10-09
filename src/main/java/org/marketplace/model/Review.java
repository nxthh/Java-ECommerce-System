package org.marketplace.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Domain model that mirrors the {@code reviews} table.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    private Long          id;
    private Long          userId;
    private String        username;
    private Long          productId;
    private String        productName;
    private int           rating;    // 1–5
    private String        comment;
    private LocalDateTime createdAt;
}
