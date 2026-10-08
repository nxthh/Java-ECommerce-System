package org.marketplace.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Domain model that mirrors the {@code payments} table.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    private Long          id;
    private Long          orderId;
    private BigDecimal    amount;
    private Method        method;
    private Status        status;
    private LocalDateTime createdAt;

    /** Supported payment methods. */
    public enum Method {
        CASH, CARD, QR
    }

    /** Payment outcome. */
    public enum Status {
        SUCCESS, FAILED
    }
}
