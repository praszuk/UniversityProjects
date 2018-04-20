package zad1.middleServer;

public class Main {
    public static void main(String[] args) {
        if (args.length == 1){
            int port = 0;
            try{
                port = Integer.parseInt(args[0]);
            }
            catch(NumberFormatException e){
                System.err.println(e.getMessage());
                System.err.println("Incorrect port number value");
                System.exit(1);
            }
            new MiddleServer(port);
        }
        else {
            System.err.println("Incorrect number of parameters.\n" +
                    "Usage: port\n" +
                    "port - is port which will be used as server listener.");
            System.exit(1);
        }
    }
}
