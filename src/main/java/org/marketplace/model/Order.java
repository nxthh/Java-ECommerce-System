package org.marketplace.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Domain model that mirrors the {@code orders} table.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    private Long          id;
    private Long          customerId;
    private String        customerUsername;
    private Status        status;
    private BigDecimal    totalAmount;
    private LocalDateTime createdAt;
    private List<OrderItem> items;

    /** Order lifecycle states. */
    public enum Status {
        PENDING, PAID, CANCELLED, COMPLETED
    }
}
