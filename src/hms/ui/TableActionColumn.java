package hms.ui;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;

/**
 * Renders two buttons (Edit / Delete) inside a JTable cell and reports
 * clicks back through a listener. Satisfies the "buttons inside table"
 * UI requirement.
 */
public class TableActionColumn extends AbstractCellEditor
        implements TableCellRenderer, TableCellEditor {

    public interface RowActionListener {
        void onEdit(int modelRow);
        void onDelete(int modelRow);
    }

    private final JTable table;
    private final RowActionListener listener;
    private final JPanel rendererPanel = build(false);
    private final JPanel editorPanel;
    private final JButton editBtn = new JButton("Edit");
    private final JButton delBtn = new JButton("Delete");
    private int editingRow;

    public TableActionColumn(JTable table, RowActionListener listener) {
        this.table = table;
        this.listener = listener;

        editorPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 1));
        editorPanel.setOpaque(true);
        style(editBtn, new Color(41, 128, 185));
        style(delBtn, new Color(192, 57, 43));
        editorPanel.add(editBtn);
        editorPanel.add(delBtn);

        editBtn.addActionListener(e -> {
            fireEditingStopped();
            listener.onEdit(editingRow);
        });
        delBtn.addActionListener(e -> {
            fireEditingStopped();
            listener.onDelete(editingRow);
        });
    }

    private static JPanel build(boolean ignore) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 1));
        JButton e = new JButton("Edit");
        JButton d = new JButton("Delete");
        style(e, new Color(41, 128, 185));
        style(d, new Color(192, 57, 43));
        p.add(e);
        p.add(d);
        return p;
    }

    private static void style(JButton b, Color c) {
        b.setBackground(c);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(b.getFont().deriveFont(11f));
        b.setMargin(new java.awt.Insets(2, 8, 2, 8));
    }

    @Override
    public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
        rendererPanel.setBackground(isSelected ? t.getSelectionBackground() : t.getBackground());
        return rendererPanel;
    }

    @Override
    public Component getTableCellEditorComponent(JTable t, Object value, boolean isSelected,
                                                 int row, int column) {
        this.editingRow = table.convertRowIndexToModel(row);
        editorPanel.setBackground(t.getSelectionBackground());
        return editorPanel;
    }

    @Override
    public Object getCellEditorValue() {
        return "";
    }
}
