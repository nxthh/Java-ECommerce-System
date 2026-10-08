package org.marketplace.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Generic container used to render tabular data in console views.
 * Headers define column names; rows contain the string values per row.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableData {

    private List<String>       headers;
    private List<List<String>> rows;

    /** @return the number of data rows (excluding the header row). */
    public int rowCount() {
        return rows == null ? 0 : rows.size();
    }
}
