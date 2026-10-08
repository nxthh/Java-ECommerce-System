package org.marketplace.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Domain model that mirrors the {@code order_items} table.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    private Long       id;
    private Long       orderId;
    private Long       productId;
    private String     productName;
    private int        quantity;
    private BigDecimal unitPrice;

    /** @return total price for this line: unitPrice × quantity. */
    public BigDecimal getLineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
