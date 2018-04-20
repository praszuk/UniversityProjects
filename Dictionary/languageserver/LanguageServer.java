package zad1.languageserver;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class LanguageServer {
    private String serverIP;
    private int serverPort;

    private String json;
    private Translator t;

    private ServerSocket server;

    public LanguageServer(String middleServerIP, int middleServerPort, String path) {

        // Handle local dictionary file
        try {
            this.json = loadStringFromFile(path);
            this.t = parserTranslatorJson(json);
        }
        catch (IOException e) {
            System.err.println("Cannot load json dictionary file from: " + path);
            System.exit(2);
        }
        catch (JsonSyntaxException e){
            System.err.println("Incorrect json structure.");
            System.exit(3);
        }

        // Try to create listen server
        try {
            this.server = new ServerSocket(0);
            this.serverIP = this.server.getInetAddress().getHostAddress();
            this.serverPort = server.getLocalPort();

            System.out.println("Starting language: \"" + t.getLang()+ "-" + t.getFullName() + "\" server on:" +
                    "\n- ip: " + serverIP+
                    "\n- port: " + serverPort);
            createServer();
        } catch (IOException e) {
            System.err.println("Cannot create server.");
            System.err.println(e.getMessage());
            System.exit(4);
        }

        // Try to connect to Middle Server
        try {
            send(middleServerIP, middleServerPort, "ADD@@@" + serverPort + "@@@" + t.getLang() + "-" + t.getFullName());
        } catch(IOException e){
            System.err.println("Cannot connect to MiddleServer: " + middleServerIP + ":" + middleServerPort);
            System.err.println(e.getMessage());
            System.exit(5);
        }
    }
    @SuppressWarnings("Duplicates")
    private void createServer() {
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

    private void closeConnection(Socket client) {
        try{
            client.close();
        } catch(IOException e){
            System.err.println("Cannot close connection quietly.");
        }
    }
    @SuppressWarnings("Duplicates")
    private void newConnection(Socket client) throws Exception {
        final String clientID = client.getInetAddress().getHostAddress() + ":" + client.getPort();
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
            out.println("PONG@@@"+ t.getLang()+"-"+t.getFullName());
            System.out.println("PONG@@@"+ t.getLang()+"-"+t.getFullName());
        }

        // TRANSLATE:IP_DEST:PORT_DEST:WORD
        else if (msg.startsWith("TRANSLATE")) {
            String[] data = msg.split("@@@");
            String ip = data[1];
            int port;
            try {
                port = Integer.parseInt(data[2]);
            } catch (NumberFormatException e) {
                throw new Exception("Cannot parse port number in Translate command: " + msg + " from: "+ clientID);
            }
            String word = data[3];
            System.out.println("Translating word: " + word + ", Translation: " + t.getTranslation(word));
            final String response = "TRANSLATE@@@"+word+"@@@"+t.getTranslation(word);
            try {
                send(ip, port, response);
            } catch(IOException e){
                throw new IOException("Cannot send response to: " + ip + ":" + port  +" msg: " + msg);
            }
        }
    }

    private void send(String ip, int port, String msg) throws IOException {
        Socket client = new Socket(ip, port);
        PrintWriter out = new PrintWriter(client.getOutputStream(), true);
        out.println(msg);
        System.out.println("Send to: " + ip + ":" + port + " msg: " + msg);
        client.close();
    }

    private String loadStringFromFile(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }

    private Translator parserTranslatorJson(String json) throws JsonSyntaxException{
        Gson gson = new Gson();
        return gson.fromJson(json, Translator.class);
    }


}
