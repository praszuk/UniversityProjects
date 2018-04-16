import javax.swing.table.AbstractTableModel;

public class MonitorTableModel extends AbstractTableModel{
    private Object[][] data;
    private static final String[] COLUMN_NAMES = {"IP", "PORT", "Timer", "SYN", "OFF"};

    public MonitorTableModel(Object[][] data){
        if (data == null)
            this.data = new Object[][]{{}};
        else
            this.data = data;
    }

    @Override
    public int getRowCount() {
        return data.length;
    }

    @Override
    public int getColumnCount() {
        return data[0].length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return data[rowIndex][columnIndex];
    }

    @Override
    public String getColumnName(int column) {
        return COLUMN_NAMES[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return getValueAt(0, columnIndex).getClass();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 3 || columnIndex == 4;
    }

    public void increaseTimer(int refreshTimeInSeconds){
        if (data[0].length == COLUMN_NAMES.length){
            for (Object[] o : data)
                o[2] = Long.sum((long) o[2], refreshTimeInSeconds *1000);
            fireTableDataChanged();
        }
    }

    public void updateData(Object[][] data){
        this.data = data;
        for (Object[] o : data) {
            System.out.println("[" + o[0] + ", " + o[1] + ", " + o[2] + "]");
        }
        fireTableDataChanged();
    }
}
