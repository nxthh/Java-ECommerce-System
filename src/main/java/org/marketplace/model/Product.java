package org.marketplace.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private Long id;
    private Long sellerId;
    private Long categoryId;
    private String name;
    private BigDecimal price;
    private int stock;
    private boolean active = true;
}
