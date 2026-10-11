package org.marketplace.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class TableData {
    private final String[] header;
    private final List<String[]> row;
}
