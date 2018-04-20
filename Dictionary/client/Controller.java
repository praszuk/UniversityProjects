package zad1.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class Controller implements ActionListener {

    private final GUI gui;
    private final Server server;

    private boolean isConnected;

    Controller(GUI gui){
        this.gui = gui;
        this.server = new Server();
        this.isConnected = false;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd =e.getActionCommand();
        if (cmd.equals("REFRESH_BUTTON_CLICK"))
            refreshButtonAction();

        else if(cmd.equals("TRANSLATE_BUTTON_CLICK"))
            translateButtonAction();
    }

    private void translateButtonAction() {
        String lang = gui.getLanguageListSelection();
        String word = gui.getToTranslation();

        if (lang != null && !word.isEmpty()){
            try {
                int port = server.createNewSocket();

                String requestMessage = "TRANSLATE@@@"+lang+"@@@"+port +"@@@" + word;
                server.sendRequest(server.middleServerIP, server.middleServerPort, requestMessage,false);
                String response = server.waitForResponse();

                String[] arr = response.split("@@@");

                // TRANSLATE@@@WORD_TO_TRANSLATE@@@TRANSLATED_WORD
                if (arr.length == 3){
                    if (arr[2].equals("null")) {
                        gui.setResultOfTranslation("Word is not in dictionary.", true);
                    }
                    else
                        gui.setResultOfTranslation(arr[2], false);
                }
                else
                    gui.setResultOfTranslation("Error with getting translation", true);

            } catch (IOException e) {
                server.middleServerIP = null;
                server.middleServerPort = 0;

                gui.setResultOfTranslation("Error with getting translation", true);
                System.err.println("Couldn't get translation" + e.getMessage());
            }
        }
    }

    private void refreshButtonAction() {
        String ip = server.middleServerIP;
        int port = server.middleServerPort;

        if (ip == null) {
            String[] address = GUI.getServerAddress();
            if (address != null) {
                ip = address[0];

                try {
                    port = Integer.parseInt(address[1]);
                } catch (NumberFormatException e) {
                    return;
                }
            }
        }

        try{
            String servers = server.sendRequest(ip,port, "GET_LANGUAGES", true);
            String[] arr = servers.split("@@@");
            String[] newServerArr = null;
            if (arr.length > 1){
                newServerArr = new String[arr.length-1];
                System.arraycopy(arr, 1, newServerArr, 0, arr.length - 1);
            }

            server.middleServerIP = ip;
            server.middleServerPort = port;

            gui.updateLanguageList(newServerArr);
            gui.changeStateOfElements(true);
            gui.setTitle("Translator - Connected to: "+ip+":"+port);
            isConnected = true;

        } catch (IOException e) {
            System.err.println("Cannot get available server list from MiddleServer." +
                    "Probably MiddleServer has been shut down");
            isConnected = false;
            server.middleServerIP = null;
            server.middleServerPort = 0;
            gui.updateLanguageList(null);
            gui.changeStateOfElements(false);
        }
    }
}
