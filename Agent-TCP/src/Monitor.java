import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;

public class Monitor extends JFrame {

    private int refreshCLK = 5;    /** Time in seconds to get data from agents */
    private int refreshTable = 1;    /** Time in seconds to refresh table */

    private HashSet<Contact> networkMap = new HashSet<>();

    JTable table;
    MonitorTableModel mtm;

    private String ip;
    private int port;

    public Monitor(String ip, int port) throws IOException, InterruptedException {
        this.ip = ip;
        this.port = port;

        // Initialize data in table model
        Object[][] data = getData(ip, port);
        if (data != null)
            mtm = new MonitorTableModel(data);
        else
            mtm = new MonitorTableModel(new Object[][]{{}, {}, {}, {}, {}});

        // Start GUI in independent thread.
        SwingUtilities.invokeLater(() -> createGUI());
    }

    /**
     * All Graphical elements, Panels, Buttons and JTable + starts refresher.
     */
    private void createGUI() {
        // North part Options
        JPanel options = new JPanel();
        options.setBorder(BorderFactory.createTitledBorder("Options"));
        options.setLayout(new GridLayout(2, 1));

        // First button
        JButton clk = new JButton(refreshCLK+" seconds.");
        clk.addActionListener(e -> {
            refreshCLK = Integer.parseInt(JOptionPane.showInputDialog("Type time in seconds, how often program will be getting data from agents."));
            clk.setText(refreshCLK + " seconds.");
        });

        // Second button
        JButton tableRefresh = new JButton(refreshTable+ " seconds.");
        tableRefresh.addActionListener((ActionEvent e) -> {
            refreshTable = Integer.parseInt(JOptionPane.showInputDialog("Type time in seconds, how often program will be refreshing table."));
            tableRefresh.setText(refreshTable + " seconds.");
        });

        // Adding buttons
        JPanel tempPanel = new JPanel(new GridLayout(1, 2));
        tempPanel.add(new JLabel("Refresh CLK (s): "));
        tempPanel.add(clk);

        options.add(tempPanel);

        tempPanel = new JPanel(new GridLayout(1, 2));
        tempPanel.add(new JLabel("Refresh Table (s): "));
        tempPanel.add(tableRefresh);

        options.add(tempPanel);


        // Center part Table with Agents
        JPanel list = new JPanel();
        list.setBorder(BorderFactory.createTitledBorder("Agents"));

        // Renders and editor for Buttons in cells SYN and OFF. Class on the bottom of this file.
        table = new JTable(mtm);
        table.getColumnModel().getColumn(3)
                .setCellRenderer((table1, value, isSelected, hasFocus, row, column) -> new JButton("SYN"));
        table.getColumnModel().getColumn(4)
                .setCellRenderer((table1, value, isSelected, hasFocus, row, column) -> new JButton("OFF"));

        table.getColumnModel().getColumn(3).setCellEditor(new FunctionButtonEditor(new JCheckBox()));
        table.getColumnModel().getColumn(4).setCellEditor(new FunctionButtonEditor(new JCheckBox()));

        list.add(new JScrollPane(table));

        // Standard settings
        add(options, BorderLayout.NORTH);
        add(list, BorderLayout.CENTER);

        setPreferredSize(new Dimension(550, 550));
        setTitle("Monitor TCP Project");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setVisible(true);

        // Auto refresh after GUI is Ready.
        new Thread(() -> {
            try {
                refreshCLK();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Function connecting to Agent, getting address book and from it connection to each Agent to send CLK command
     * and get Current Timer value.
     *
     * @param ip agent ip
     * @param port agen port
     * @return data, which can be imported to table. data[0] = ip, data[1] = port, data[2] = timer, data[3/4] = buttons.
     */
    private Object[][] getData(String ip, int port) {
        try {
            Socket socket = new Socket(ip, port);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            out.println("NET");
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            networkMap = (HashSet<Contact>) in.readObject();
            networkMap.add(new Contact(ip, port));

            ArrayList<Contact> list = new ArrayList<>(networkMap);
            Object[][] data = new Object[list.size()][5];
            for (int i = 0; i < list.size(); i++) {
                    data[i][0] = list.get(i).getIp();
                    data[i][1] = list.get(i).getPort();
                    data[i][2] = getTime((String) data[i][0], (int) data[i][1]);
            }

            socket.close();

            return data;
        }
        catch (IOException e) {
            System.err.println("Couldn't connect to Agent with ip" + ip + ":" + port);

            // remove this agent from local network map
            Contact tempContact = null;
            for (Contact c : networkMap) {
                if (c.toString().equals(ip + ":" + port)) {
                    tempContact = c;
                    break;
                }
            }
            networkMap.remove(tempContact);

            if(networkMap.isEmpty()){
                JOptionPane.showMessageDialog(null, "No Agents. Program is terminating...");
                System.exit(0);
            }


            // set ip and port to the next on the list
            Contact next = networkMap.iterator().next();
            this.ip = next.getIp();
            this.port = next.getPort();

        } catch (ClassNotFoundException e) {
            System.err.println("Error with receiving object with Address Book.");
        }
        return null;
    }

    /**
     * Responsible for refreshing table. It uses 2 value:
     * - RefreshCLK which means, how often we update data directly from Agents (via network).
     * - RefreshTable which means, how often we update timers in current data.
     * @throws InterruptedException
     */
    private void refreshCLK() throws InterruptedException {
        int counter = 0;
        while (true) {
            Thread.sleep(refreshTable * 1000);
            counter += refreshTable*1000;
            mtm.increaseTimer(refreshTable);

            if (counter >= (refreshCLK *1000)){
                counter = 0;
                Object[][] data = getData(ip, port);
                if (data != null) {
                    mtm.updateData(data);
                    System.out.println("[CLK] Getting data from agents (" + ip  + port + ").");
                }

            }
        }
    }

    /**
     * @param ip agent
     * @param port agent
     * @return returning time counter - output of CLK command.
     */
    private long getTime(String ip, int port){
        long counter = -1;
        try {
            Socket socket = new Socket(ip, port);

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println("CLK");
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            counter = Long.parseLong(in.readLine());

            socket.close();
        } catch (IOException e) {
            System.err.println("[CLK] Couldn't get timer from + " + ip + ":" + port);
        }

        return counter;
    }

    // Class to button
    class FunctionButtonEditor extends DefaultCellEditor{
        private JButton button;
        private boolean statusOfClick;
        private int row;
        private int column;
        private JTable table;

        public FunctionButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            statusOfClick = true;
            this.row = row;
            this.column = column;
            this.table = table;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if(statusOfClick) {
                try {
                    Socket s = new Socket((String) table.getValueAt(row, 0), (int) table.getValueAt(row, 1));
                    PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                    if (column == 3) {
                        out.println("SYN");
                        BufferedReader inSYN = new BufferedReader(new InputStreamReader(s.getInputStream()));
                        String status = inSYN.readLine();
                        if (status.equals("DONE"))
                            System.out.println("[SYN] Synchronizing complete at " + ip + ":" + port + ".");
                    }
                    if (column == 4){
                        out.println("OFF");
                        System.out.println("[OFF] Sent OFF command to: " + ip + ":" + port + ".");
                    }
                    s.close();
                } catch (IOException e1) {
                    System.err.println("Couldn't send command via button to " + ip + ":"+port + ".");
                }
            }
            statusOfClick = false;
            return super.getCellEditorValue();
        }
    }
}
