package csc435.app;

import java.lang.System;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class ClientAppInterface {
    private ClientProcessingEngine engine;

    public ClientAppInterface(ClientProcessingEngine engine) {
        this.engine = engine;

        // TO-DO implement constructor
        // keep track of the connection with the client
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
            if (command.equals("quit")) {
                System.out.println("Disconnecting and ending program...");
                engine.disconnect();
                break;
            }

            // if the command begins with connect, connect to the given server
            if (command.startsWith("connect")) {
                String[] parts = command.split("\\s+");
                if (parts.length == 3) {
                    String serverIP = parts[1]; // TO-DO implement index operation
                    String serverPort = parts[2];
                    engine.connect(serverIP, serverPort);  // call the connect method from the server side engine
                } else {
		    System.out.println("Invalid connect command! Usage: connect <server IP> <port>");
                }
                continue;
            }
        
            // if the command begins with index, index the files from the specified directory
            if (command.startsWith("index")) {
                if (command.length() <= 6) {
                    System.out.println("Please provide a directory path to index");
                    continue;
                }
                String directory = command.substring(6).trim();
                System.out.println("Indexing directory: " + directory); // TO-DO implement index operation
                try {
                    IndexResult result = engine.indexFiles(directory); // call the index method on the server side engine and pass the folder to be indexed
                    System.out.println("Time taken to index: " + result.executionTime + " seconds"); 
                    System.out.println("Total bytes read: " + result.totalBytesRead);
                } catch (Exception e) {
                    System.out.println("Error during indexing: " + e.getMessage());
                }
                continue;
            }

            // if the command begins with search, search for files that matches the query
            if (command.startsWith("search")) {
               if (command.length() <= 7) {
                   System.out.println("Please provide search terms");
                   continue;
               }
               String searchTerms = command.substring(7).trim();
               ArrayList<String> terms = new ArrayList<>(Arrays.asList(searchTerms.split("\\s+AND\\s+|\\s+")));
               if (terms.isEmpty()) {
                   System.out.println("Please provide valid search terms");
                   continue;
               }
               try { // TO-DO implement index operation
                   SearchResult result = engine.searchFiles(terms);  // extract the terms and call the server side engine method to search the terms for files
                   System.out.println("Search completed in " + result.excutionTime + " seconds");
                   
                   if (result.documentFrequencies.isEmpty()) {
                       System.out.println("No documents found matching the search terms");
                   } else {
                       System.out.println("Top matches (showing up to 10 results):");
                       for (DocPathFreqPair pair : result.documentFrequencies) {
                           System.out.println("  " + pair.documentPath + " (frequency: " + pair.wordFrequency + ")");
                       }
                   }
               } catch (Exception e) {
                   System.out.println("Error during search: " + e.getMessage());
               }
               continue;
           }
          
            System.out.println("unrecognized command!");
        }

        sc.close();
    }
}

