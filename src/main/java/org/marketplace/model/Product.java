package org.marketplace.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Domain model that mirrors the {@code products} table.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    private Long       id;
    private Long       sellerId;
    private Long       categoryId;

    /** Category name – populated by JOIN queries for display convenience. */
    private String     categoryName;

    /** Seller username – populated by JOIN queries for display convenience. */
    private String     sellerUsername;

    private String     name;
    private BigDecimal price;
    private int        stock;
    private boolean    active;
}
