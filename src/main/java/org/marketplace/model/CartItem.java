package org.marketplace.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Represents one row in the {@code cart_items} table, enriched with
 * product details needed for cart display.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    private Long       userId;
    private Long       productId;
    private String     productName;
    private BigDecimal unitPrice;
    private int        stock;
    private int        quantity;

    /** @return total price for this line: unitPrice × quantity. */
    public BigDecimal getLineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
