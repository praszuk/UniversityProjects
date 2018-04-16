public class Main {

    public static void main(String args[]) {
        if (args.length == 1) {
            if (args[1].contains("help"))
                System.out.println("How to use it:"
                        + "#############################################################################################"
                        + "    Compile: Go to Task1_TCP/src directory and type \"javac Main Agent Monitor Contact\""
                        + "#############################################################################################"
                        + "Launch:"
                        + "-- First Agent: java Main agent <value of counter> == \"java Main agent 10001\""
                        + "-- Next Agent: java Main agent <value of counter> [ip port] == \"java Main agent 20 10.1.1.2 10001\""

                        + "-- Monitor: java Main monitor <ip of agent> <port of agent> == \"java Main monitor 10.1.1.3 10001\""
                        + "#############################################################################################");
        }
        else if (args.length >= 2 && args.length <= 4) {

            if (args[0].toLowerCase().equals("agent")) {

                if (args.length == 2) {
                    try {
                        long time = Long.parseLong(args[1]);
                        new Agent(time);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else if (args.length == 4) {
                    try {
                        long time = Long.parseLong(args[1]);
                        int port = Integer.parseInt(args[3]);
                        new Agent(time, args[2], port);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else
                    System.err.println("Incorrect parameters. \nExample use: java Main agent 20 [10.1.1.14] [36742]");
            }

            else if (args[0].toLowerCase().equals("monitor")) {

                if (args.length == 3) {
                    try {
                        int port = Integer.parseInt(args[2]);
                        new Monitor(args[1], port);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else
                    System.err.println("Incorrect parameters. \nExample use: java Main monitor 10.1.1.14 36742");
            }

        else
            System.err.println("Incorrect parameters.");

        }
    }
}
