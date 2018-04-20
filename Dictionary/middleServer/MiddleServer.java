package zad1.middleServer;

import javafx.util.Pair;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class MiddleServer {

    private static final int TIMEOUT = 1000;
    private final int serverPort;
    private ConcurrentHashMap<String, Pair<String, Integer>> langServers = new ConcurrentHashMap<>();

    public MiddleServer(int port) {
        this.serverPort = port;
        try {
            createServer();
            System.out.println("Started server on " + serverPort);
        } catch(IOException e){
            System.err.println("Cannot create server.");
            System.exit(2);
        }
    }

    @SuppressWarnings("Duplicates")
    private void createServer() throws IOException{
        ServerSocket server = new ServerSocket(serverPort);
        Thread serverThread = new Thread(()->{
            while(true){
                Socket newClient = null;
                try {
                    newClient = server.accept();
                } catch (IOException e) {
                    System.err.println("Cannot accept incoming connection. IOException.");
                }

                if (newClient != null){
                    Socket client = newClient;
                    Thread connectionThread = new Thread(()-> {
                        try {
                            newConnection(client);
                        } catch (Exception e) {
                            System.err.println(e.getMessage());
                        }
                        finally{
                            closeConnection(client);
                        }
                    });
                    connectionThread.start();
                }

            }
        });
        serverThread.start();
    }

    @SuppressWarnings("Duplicates")
    private void newConnection(Socket client) throws Exception {
        final String clientID = client.getInetAddress().getHostAddress() + "@@@" + client.getPort();
        String msg;

        PrintWriter out;
        BufferedReader in;

        try {
            out = new PrintWriter(client.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            msg = in.readLine();
            System.out.println(clientID + ": " + msg);
        }
        catch (IOException e){
            throw new IOException("Cannot receive message.");
        }

        // Diagnostic command
        if (msg.equals("PING")) {
            out.println("PONG");
        }

        else if (msg.startsWith("ADD")){
            String[] data = msg.split("@@@");
            String ip = client.getInetAddress().getHostAddress();
            int port;
            try {
                port = Integer.parseInt(data[1]);
            } catch (NumberFormatException e) {
                throw new Exception("Cannot parse port number in ADD command: " + msg + " from: "+ clientID);
            }
            String lang = data[2];
            langServers.put(lang, new Pair<>(ip, port));
            System.out.println("Added " + lang + " from: " + ip + ":" + port+" to languageServers");
        }

        else if (msg.equals("GET_LANGUAGES")){
            refreshLangServers();
            String response;
            if (!langServers.isEmpty()) {
                StringBuilder languages = new StringBuilder();
                for (String key : langServers.keySet())
                    languages.append("@@@"+key);

                response = "LANGUAGES"+languages;
            }
            else
                response = "LANGUAGES@@@";

            out.println(response);
            System.out.println("Sent languages list to: " + clientID + ": " + response);
        }
        else if (msg.startsWith("TRANSLATE")){
            String[] data = msg.split("@@@");
            String lang = data[1];
            String clientIP = client.getInetAddress().getHostAddress();
            String clientPort = data[2];
            String word = data[3];

            if (langServers.containsKey(lang)){

                String ip = langServers.get(lang).getKey();
                int port = langServers.get(lang).getValue();

                Socket s = new Socket(ip, port);
                PrintWriter tServer = new PrintWriter(s.getOutputStream(), true);
                tServer.println("TRANSLATE@@@"+clientIP+"@@@"+clientPort+"@@@" + word);
                tServer.close();
                System.out.println("Send to: " + lang + "@@@" + "TRANSLATE@@@"+clientIP+"@@@"+clientPort+"@@@" + word);
            }
            else {
                Socket s = new Socket(clientIP, Integer.parseInt(clientPort));
                PrintWriter tServer = new PrintWriter(s.getOutputStream(), true);
                tServer.println("TRANSLATE@@@ERROR_SERVER");
                tServer.close();
                System.out.println("No: " + lang + " in available servers.");
            }
        }
        in.close();
        out.close();
        client.close();

    }

    private void refreshLangServers() {

        for (Map.Entry<String, Pair<String, Integer>> serv : langServers.entrySet()){
            String language = serv.getKey();
            String ip = serv.getValue().getKey();
            int port = serv.getValue().getValue();

            try {
                Socket s = new Socket(ip, port);

                PrintWriter out = new PrintWriter(new OutputStreamWriter(s.getOutputStream()), true);
                out.println("PING");

                BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));

                String msg = in.readLine();
                if (msg.equals("PONG@@@"+language))
                    continue;
                else{
                    langServers.remove(language);
                    System.out.println("Removed " + language + " server, from the list.");
                }

                out.close();
                in.close();
                s.close();
            }
            catch (IOException e){
                langServers.remove(language);
                System.out.println("Removed " + language + " server, from the list.");
            }
        }

    }

    private void closeConnection(Socket client) {
        try{
            client.close();
        } catch(IOException e){
            System.err.println("Cannot close connection quietly.");
        }
    }
}
