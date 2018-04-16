import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;

public class Agent {

    private volatile boolean active = true;     /** it's always true, until someone send a "OFF" command to agent.  */

    private String ipAddress;                   /** on init is 0.0.0.0 later it change to outside ip sent by client */
    private int port;                           /** outside port of serverSocket to incoming connection             */
    private ServerSocket serverSocket;          /** all servers starts at 0.0.0.0. This about connection in LAN     */

    private final long startTimeCounter = System.currentTimeMillis();  /** moment of initialize currentTimeMillis() */
    private long counterOffSet;                                        /** OffSet in time from startTimeCounter     */

    private volatile HashSet<Contact> networkMap = new HashSet<>();    /** address book - agents without keeper     */

    public Agent(long counterOffSet) throws IOException {

        serverSocket = new ServerSocket(0);

        this.port = serverSocket.getLocalPort();
        this.ipAddress = serverSocket.getInetAddress().getHostAddress();

        System.out.println("####################################################################");
        System.out.println("\t\tStarting Agent server on: " + ipAddress + ":" + port);

        this.counterOffSet = counterOffSet;
        System.out.println("\t\tTime counter started with " + counterOffSet + " value.");
        System.out.println("####################################################################");
        startServer();
    }

    public Agent(long counter, String otherAgentIp, int otherAgentPort) throws IOException{
        this(counter);
        startFromAgent(otherAgentIp, otherAgentPort);
    }

    /**
     * Send information about this Agent to other Agent to update Network Map.
     * Then sends SYN to Synchronize timers.
     * @param parentAgentIp input ip from parameter
     * @param parentAgentPort input port from parameter
     * @throws IOException
     */
    private void startFromAgent(String parentAgentIp, int parentAgentPort) throws IOException {
        Socket client = new Socket(parentAgentIp, parentAgentPort);
        Contact cl = new Contact(client.getInetAddress().getHostAddress(), client.getPort());
        networkMap.add(cl);
        System.out.println("Added parent agent " + cl +".\n");

        PrintWriter out = new PrintWriter(client.getOutputStream(), true);

        out.println("RETURN_IP");
        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        ipAddress = in.readLine();
        out.println(client.getInetAddress().getHostAddress());

        out.println("NET");
        try {
            ObjectInputStream inObj = new ObjectInputStream(client.getInputStream());

            networkMap.addAll((HashSet<Contact>) inObj.readObject());

            System.out.println("[NET] Updated address book: " + networkMap+".");


        } catch (ClassNotFoundException e) {
            System.err.println("[NET] Couldn't parse received network map.");
        }

        sendContactToAll(true);

        synchronize();

        for (Contact c : networkMap){
            try{
                Socket synSocket = new Socket(c.getIp(), c.getPort());

                PrintWriter outSYN = new PrintWriter(synSocket.getOutputStream(), true);
                outSYN.println("SYN");
                BufferedReader inSYN = new BufferedReader(new InputStreamReader(synSocket.getInputStream()));
                String status = inSYN.readLine();

                synSocket.close();
                if(status.equals("DONE"))
                    System.out.println("[SYN] Client " + c + " synchronized its time counter.");
            }catch(IOException e){
                System.out.println("[SYN] Couldn't synchronize client " + c + ".");
            }

        }

        client.close();
    }

    /**
     * Starting instance of socket server and handling incoming connection.
     * @throws IOException
     */
    private void startServer() throws IOException {
        Thread server = new Thread(() -> {
            while(active){

                Socket newClient = null;
                try {
                    newClient = serverSocket.accept();
                } catch (IOException e) {
                    if (active)
                        System.err.println("Couldn't accept new connection.");
                }

                Socket finalNewClient = newClient;
                Thread conn = new Thread(() -> {
                    try {
                        newIncomingConnection(finalNewClient);
                        if (!active)
                            serverSocket.close();
                    } catch (IOException e) {
                        System.err.println("Java awful try chat Meh But i had to kill you socket server. I am sorry :P.");
                    }
                });
                conn.start();
            }
            System.out.println("Exiting...");
            System.exit(0);
        });
        server.start();
    }

    /**
     * Handle receive data and important operation command like CLK, NET, SYN.
     *
     * @param client new connection after accepted by server socket.
     * @throws IOException
     */
    private void newIncomingConnection(Socket client) throws IOException{
        PrintWriter out = new PrintWriter(client.getOutputStream(), true); // For commands
        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

        String clientID = client.getInetAddress().getHostAddress() + ":" + client.getPort();
        System.out.println("Client " + clientID + " connected.");

        while(active){
            String rcvMsg;
            if ((rcvMsg = in.readLine()) != null) {
                if (rcvMsg.startsWith("ADD")){
                    String contact = rcvMsg.substring(3); // separate command from data
                    try {
                        String[] data = contact.split(":");
                        if(networkMap.add(new Contact(data[0], Integer.parseInt(data[1]))))
                            System.out.println("[ADD] Added new Agent "+ contact +" to address book: "+networkMap+".");
                    } catch(Exception e){
                        System.err.println("Incorrect ADD parameter");
                        continue;
                    }
                }
                if (rcvMsg.equals("CLK")){
                    long sendCounter = getTimeCounter();  // It prevent to bug log information with different data
                    out.println(Long.toString(sendCounter));
                    System.out.println("[CLK] Counter value ("+sendCounter+") has been sent to " + clientID + ".");
                }
                if (rcvMsg.startsWith("DEL")){
                    String contact = rcvMsg.substring(3); // separate command from data

                    Contact tempContact = null;
                    for (Contact c : networkMap)
                        if (c.toString().equals(contact))
                            tempContact = c;

                    if(networkMap.remove(tempContact))
                        System.out.println("[DEL] Removed " + tempContact + " from address book: " + networkMap + ".");
                }
                if (rcvMsg.equals("RETURN_IP")){
                    out.println(client.getInetAddress().getHostAddress());
                    System.out.println("[RETURN_IP] Remote Ip of client "
                                        + client.getInetAddress().getHostAddress()
                                        + " has been sent to " + clientID +".");

                    if (ipAddress.equals("0.0.0.0")) {
                        ipAddress = in.readLine();
                        System.out.println("[RETURN_IP] IP Has been set by other agents to: " + ipAddress +".");
                    }
                    else
                        in.readLine();  // cleaning buffer

                }
                if (rcvMsg.equals("NET")){
                    ObjectOutputStream outBytes = new ObjectOutputStream(client.getOutputStream());
                    outBytes.writeObject(networkMap);
                    System.out.println("[NET] Address book has been sent to "+ clientID + " " + networkMap + ".");
                }
                if (rcvMsg.equals("OFF")){
                    if(networkMap.size() > 0)
                        System.out.println("[OFF] Removing contact from other agents.");
                    sendContactToAll(false);
                    active = false;
                }
                if (rcvMsg.equals("SYN")){
                    if(networkMap.size() > 0) {
                        System.out.println("[SYN] Synchronizing time counter with other agents.");
                        synchronize();
                    }
                    else
                        System.out.println("[SYN] Nothing to do. You are alone :(.");

                    out.println("DONE");
                }
            }
            else
                break;
        }
        client.close();
        System.out.println("Client " + clientID + " disconnected.\n");
        if (!active)
            System.out.println("Shutting down...");
    }

    /**
     * Synchronizing time counters with other agents.
     * Sending CLK command to all agents from network map to get values. Then get average of all counters(including).
     * Set counterOffset to new value (avgOfRestAgent - (NOW - startTime)).
     */
    private void synchronize() {
        int downloadedData = 1;
        long sum = getTimeCounter();

        // getting data
        for (Contact c : networkMap){
            try {
                Socket client = new Socket(c.getIp(), c.getPort());
                PrintWriter out = new PrintWriter(client.getOutputStream(), true);

                out.println("CLK");
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String number = in.readLine();

                sum += Long.parseLong(number);
                downloadedData++;

            } catch (IOException e) {
                System.err.println("[SYN] cannot connect to " + c);
            }
        }

        // math
        long avg = sum/downloadedData;
        long previousValue = getTimeCounter();
        counterOffSet = avg - (System.currentTimeMillis() - startTimeCounter);
        System.out.println("[SYN] Previous value: " + previousValue + ". New value of counter: " + getTimeCounter() + ".");
    }

    /**
     * Sending own contact object to remove it from others.
     * @param toAdd if true adding contact if false removing
     */
    private void sendContactToAll(boolean toAdd) {
        for (Contact c : networkMap){
            try {
                Socket client = new Socket(c.getIp(), c.getPort());
                PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                if (toAdd) {
                    out.println("ADD" + ipAddress + ":" + port);
                    System.out.println("[ADD] Contact add request " + "ADD" + ipAddress + ":" + port + " has been sent to " + c + ".");
                } else {
                    out.println("DEL" + ipAddress + ":" + port);
                    System.out.println("[DEL] Contact remove request " + "DEL" + ipAddress + ":" + port + " has been sent to " + c + ".");
                }
            } catch (IOException e){
                System.err.println("Couldn't connect to " + c + ".");
            }
        }
    }

    private long getTimeCounter(){
        return System.currentTimeMillis()-startTimeCounter+counterOffSet;
    }
}
