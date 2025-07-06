package csc435.app;

public class FileRetrievalServer
{
    public static void main( String[] args )
    {
        // TO-DO change server port to a non-privileged port from args[0]
        int serverPort;
        try {
            if (args.length < 1) {
                System.out.println("Usage: java FileRetrievalServer <port>");
                System.out.println("Using default port: 12345");
                serverPort = 12345;  // Default port if none specified
            } else {
                serverPort = Integer.parseInt(args[0]);
                // Validate port number
                if (serverPort < 1024 || serverPort > 65535) {
                    System.out.println("Port must be between 1024 and 65535");
                    System.out.println("Using default port: 12345");
                    serverPort = 12345;
                }
            }
        System.out.println("Starting server on port " + serverPort);
        IndexStore store = new IndexStore();
        ServerProcessingEngine engine = new ServerProcessingEngine(store);
        ServerAppInterface appInterface = new ServerAppInterface(engine);
        
        // create a thread that runs the gRPC server
        try{
        engine.initialize(serverPort);
        System.out.println("Server initialized successfully");

        // read commands from the user
        appInterface.readCommands();
        }catch (Exception e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    } catch (NumberFormatException e) {
        System.err.println("Invalid port number format");
        System.exit(1);
    } catch (Exception e) {
        System.err.println("Unexpected error: " + e.getMessage());
        e.printStackTrace();
        System.exit(1);
    }
}
}

