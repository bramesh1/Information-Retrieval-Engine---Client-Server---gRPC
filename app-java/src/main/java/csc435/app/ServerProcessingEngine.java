package csc435.app;

import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.ServerBuilder;

public class ServerProcessingEngine {
    private IndexStore store;
    private RPCServerWorker serverWorker; // TO-DO keep track of the RPCServerWorker object
    private Thread serverThread; // TO-DO keep track of the gRPC server thread

    public ServerProcessingEngine(IndexStore store) {
        this.store = store;
    }

    // TO-DO create and start the gRPC Server
    public void initialize(int serverPort) {
        try {
        // TO-DO create the RPCServerWorker object
        serverWorker = new RPCServerWorker(store);
        // TO-DO create and start the gRPC server thread that runs in the context of the RPCServerWorker object
        serverThread = new Thread(serverWorker);
           serverThread.start();
           System.out.println("Server initialized and started on port " + serverPort);
        } catch (Exception e) {
            System.err.println("Failed to initialize server: " + e.getMessage());
            e.printStackTrace();
        } 
    }
    // TO-DO shutdown the gRPC Server
    public void shutdown() {
        try {
            // Shutdown the RPCServerWorker
            if (serverWorker != null) {
                serverWorker.shutdown();
            }
              // Join the server thread if it exists and is running
            if (serverThread != null && serverThread.isAlive()) {  // TO-DO join the gRPC server thread
            try {
                serverThread.join(5000); // Wait up to 5 seconds for thread to finish
                if (serverThread.isAlive()) {
                    System.err.println("Server thread did not terminate in time");
                }
            } catch (InterruptedException e) {
                System.err.println("Interrupted while waiting for server thread to finish: " + e.getMessage());
            }
            }
            System.out.println("Server shutdown completed");
        } catch (Exception e) {
        System.err.println("Error during server shutdown: " + e.getMessage());
        e.printStackTrace();
        }  
    }
}

