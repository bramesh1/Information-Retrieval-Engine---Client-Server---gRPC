package csc435.app;

import java.util.concurrent.TimeUnit;

import io.grpc.Server;
import io.grpc.ServerBuilder;

public class RPCServerWorker implements Runnable {
    private IndexStore store;
    // TO-DO keep track of the gRPC Server object
    private Server server;
    public RPCServerWorker(IndexStore store) {
        this.store = store;
    }

    @Override
    public void run() {
        try {
            // TO-DO build the gRPC Server
            server = ServerBuilder.forPort(12345)
                    .addService(new FileRetrievalEngineService(store))
                    .build();
            // TO-DO register the FileRetrievalEngineService service with the gRPC Server
            // TO-DO start the gRPC Server
            server.start();
            System.out.println("gRPC Server started on port " + server.getPort());
            server.awaitTermination();
        }  catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
         if (server != null) {
            try {
                // TO-DO shutdown the gRPC server
                server.shutdown();
                // TO-DO wait for the gRPC server to shutdown
                if (!server.awaitTermination(5, TimeUnit.SECONDS)) {
                    System.err.println("Timed out waiting for gRPC server shutdown");
                }
                System.out.println("gRPC Server shutdown complete");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

