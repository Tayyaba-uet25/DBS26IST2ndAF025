package hms.dao;

import java.util.List;

/** Simple holder for a tabular report result (column headers + string rows). */
public class ReportData {
    public final String[] columns;
    public final List<String[]> rows;

    public ReportData(String[] columns, List<String[]> rows) {
        this.columns = columns;
        this.rows = rows;
    }
}
