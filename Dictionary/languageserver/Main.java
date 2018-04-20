package zad1.languageserver;

public class Main {
    public static void main(String[] args) {

        if (args.length == 3){
            String ip = args[0];
            int port = 0;

            try{
                port = Integer.parseInt(args[1]);
            }
            catch(NumberFormatException e){
                System.err.println(e.getMessage());
                System.err.println("Incorrect number of parameters.\n" +
                        "Usage: ip port path\n" +
                        "Where ip and port belong to mid server and path is to JSON dictionary file.");
                System.exit(1);
            }
            String path = args[2];
            new LanguageServer(ip, port, path);
        }
        else{
            System.err.println("Incorrect number of parameters.\n" +
                    "Usage: ip port path\n" +
                    "Where ip and port belong to mid server and path is to JSON dictionary file.");
            System.exit(1);
        }
    }
}
