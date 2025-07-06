package csc435.app;

import java.io.IOException;
import java.util.Random;

public class FileRetrievalClient
{
    public static void main(String[] args)
    {
        // Generate a random client ID between 1 and 10
        Random random = new Random();
        int clientID = random.nextInt(10) + 1;  // generates ID from 1 to 10
        ClientProcessingEngine engine = new ClientProcessingEngine(clientID);
        ClientAppInterface appInterface = new ClientAppInterface(engine);
        
        // read commands from the user
         try {
            System.out.println("Client started with ID: " + clientID);
            appInterface.readCommands();
        } finally {
            // Ensure we disconnect properly even if an error occurs
            engine.disconnect();
        }
    }
}
