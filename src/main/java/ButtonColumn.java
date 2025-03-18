import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class ButtonColumn extends AbstractCellEditor implements TableCellRenderer, TableCellEditor {
    private JButton renderButton;
    private JButton editButton;
    private String text;
    private Action action;

    public ButtonColumn(JTable table, Action action, int column) {
        this.action = action;
        renderButton = new JButton("Details");
        editButton = new JButton("Details");

        table.getColumnModel().getColumn(column).setCellRenderer(this);
        table.getColumnModel().getColumn(column).setCellEditor(this);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {
        return renderButton;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row, int column) {
        text = (value == null) ? "" : value.toString();
        editButton.setAction(action);
        editButton.setActionCommand(String.valueOf(row));
        return editButton;
    }

    @Override
    public Object getCellEditorValue() {
        return text;
    }
}