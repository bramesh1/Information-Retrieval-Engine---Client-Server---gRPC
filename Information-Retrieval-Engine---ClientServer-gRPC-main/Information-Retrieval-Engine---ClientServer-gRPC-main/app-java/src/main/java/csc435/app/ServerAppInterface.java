package csc435.app;

import java.lang.System;
import java.util.Scanner;

public class ServerAppInterface {
    private ServerProcessingEngine engine;

    public ServerAppInterface(ServerProcessingEngine engine) {
        this.engine = engine;
    }

    public void readCommands() {
        // TO-DO implement the read commands method
        Scanner sc = new Scanner(System.in);
        String command;
        
        while (true) {
            System.out.print("> ");
            
            // read from command line
            command = sc.nextLine();

            // if the command is quit, terminate the program       
            if (command.compareTo("quit") == 0) {
                engine.shutdown();
                break;
            }

            System.out.println("unrecognized command!");
        }

        sc.close();
    }
}

