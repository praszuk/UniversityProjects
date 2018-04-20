package zad1.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

class Server {
    private static final int TIMEOUT = 5000;
    private ServerSocket serverSocket;

    String middleServerIP;
    int middleServerPort;

    int createNewSocket() throws IOException {
        serverSocket = new ServerSocket(0);
        return serverSocket.getLocalPort();
    }

    String waitForResponse() throws IOException {
        serverSocket.setSoTimeout(TIMEOUT);
        Socket client = serverSocket.accept();
        client.setSoTimeout(TIMEOUT);

        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        String response = in.readLine();

        in.close();
        client.close();
        serverSocket.close();

        return response;
    }

    String sendRequest(String ip, int port, String msg, boolean withReturn) throws IOException {
        Socket client = new Socket(ip, port);
        client.setSoTimeout(TIMEOUT);

        PrintWriter out = new PrintWriter(client.getOutputStream(), true);

        BufferedReader in = null;
        String response = null;
        if (withReturn){
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        }

        out.println(msg);
        System.out.println("Send to: " + ip + ":" + port + " msg: " + msg);

        if (withReturn){
            response = in.readLine();
            System.out.println("Received: " +response);
        }


        out.close();
        if (withReturn)
            in.close();
        client.close();

        return response;
    }



}
