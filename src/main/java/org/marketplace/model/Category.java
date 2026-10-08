package org.marketplace.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain model that mirrors the {@code categories} table.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    private Long   id;
    private String name;
}
