package zad1.client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

class GUI extends JFrame {
    private final int SIZE_HEIGHT = 150;
    private final int SIZE_WIDTH = 400;

    private JComboBox<String> languageList;
    private JButton refreshButton;

    private JTextField toTranslation;
    private JTextField resultOfTranslation;

    private JButton translateButton;
    private Controller controller;

    GUI(){
        this.controller = new Controller(this);
        SwingUtilities.invokeLater(()->{
            createGUI();
            changeStateOfElements(false);
        });
    }

    private void createGUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());


        JPanel settingsView = new JPanel(new FlowLayout());
        {
            languageList = new JComboBox<>();

            refreshButton = new JButton("Refresh");
            refreshButton.setActionCommand("REFRESH_BUTTON_CLICK");
            refreshButton.addActionListener(controller);

            settingsView.add(languageList);
            settingsView.add(refreshButton);
        }

        JPanel translateView = new JPanel(new GridLayout(2,1));
        {
            toTranslation = new JTextField();

            resultOfTranslation = new JTextField();
            resultOfTranslation.setEditable(false);

            translateView.setBorder(new EmptyBorder(10,5,10,5));
            translateView.add(toTranslation);
            translateView.add(resultOfTranslation);
        }

        translateButton = new JButton("Translate!");
        translateButton.setActionCommand("TRANSLATE_BUTTON_CLICK");
        translateButton.addActionListener(controller);

        mainPanel.add(settingsView, BorderLayout.NORTH);
        mainPanel.add(translateView, BorderLayout.CENTER);
        mainPanel.add(translateButton, BorderLayout.SOUTH);
        add(mainPanel);

        pack();
        setTitle("Translator - Not Connected");
        setSize(SIZE_WIDTH, SIZE_HEIGHT);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
    }

    /**
     * Function which activates or deactivates elements in GUI.
     *
     * @param b if false blocking b else activating
     */
    void changeStateOfElements(boolean b) {
        toTranslation.setEditable(b);
        translateButton.setEnabled(b);
    }

    /**
     * Show JOptionPane confirm dialog, asking about ip and port to MiddleServer address.
     * @return new String[]}{IP, PORT} or NULL if user clicked CANCEL in OptionPane.
     */
    static String[] getServerAddress() {
        JTextField ip = new JTextField();
        JTextField port = new JTextField();

        Object[] dialogs = {"IP:", ip, "Port:", port};
        String title = "Connect to new Translation server";

        int result = JOptionPane.showConfirmDialog(null, dialogs, title, JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION)
            return new String[]{ip.getText(), port.getText()};
        else
            return null;
    }

    /**
     * Updates JComboBox language servers list.
     * @param array fill ComboBox with strings from array if NULL clear all items.
     */
    void updateLanguageList(String[] array){
        if (array == null)
            languageList.removeAllItems();
        else
            languageList.setModel(new DefaultComboBoxModel<>(array));
    }

    public String getToTranslation() { return toTranslation.getText(); }

    public void setResultOfTranslation(String text, boolean isError) {
        if (isError)
            resultOfTranslation.setForeground(Color.RED);
        else
            resultOfTranslation.setForeground(Color.BLACK);
        resultOfTranslation.setText(text);
    }
    public String getLanguageListSelection(){
        return (String) languageList.getSelectedItem();
    }
}
