package csc435.app;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class BenchmarkWorker implements Runnable {
    // TO-DO declare a ClientProcessingEngine
    private ClientProcessingEngine engine;
    private String datasetPath;
    private String serverIP;
    private String serverPort;

    public BenchmarkWorker(String serverIP, String serverPort, String datasetPath) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.datasetPath = datasetPath;
    }

    @Override
    public void run() {
        // TO-DO create a ClientProcessinEngine
        int clientID = new java.util.Random().nextInt(10) + 1;
        engine = new ClientProcessingEngine(clientID);
        // TO-DO connect the ClientProcessingEngine to the server
        System.out.println("Client " + clientID + " connecting to server: " + serverIP + ":" + serverPort);
        engine.connect(serverIP, serverPort);
        // TO-DO index the dataset
        System.out.println("Client " + clientID + " indexing dataset: " + datasetPath);
        try {
           IndexResult result = engine.indexFiles(datasetPath);
           System.out.println("Client " + clientID + " completed indexing:");
           System.out.println("  Bytes read: " + result.totalBytesRead);
           System.out.println("  Time taken: " + result.executionTime + " seconds");
        } catch (Exception e) {
           System.err.println("Client " + clientID + " failed to index: " + e.getMessage());
           e.printStackTrace();
        }
    }

    public void search(String query) {
        // TO-DO perform search operations on the ClientProcessingEngine
        System.out.println("\nPerforming search query: " + query);
        try {
            // Split query into terms
            String[] queryTerms = query.split("\\s+AND\\s+|\\s+");
            ArrayList<String> terms = new ArrayList<>();
            for (String term : queryTerms) {
                terms.add(term.trim().toLowerCase());
            }
             // Perform search
            SearchResult result = engine.searchFiles(terms);
            System.out.println("Search completed in " + result.excutionTime + " seconds");
           if (result.documentFrequencies.isEmpty()) {
               System.out.println("No matching documents found");
           } else {
               System.out.println("Top " + Math.min(10, result.documentFrequencies.size()) + " results:");
               for (DocPathFreqPair pair : result.documentFrequencies) {
                   System.out.println("  " + pair.documentPath + " (frequency: " + pair.wordFrequency + ")"); // TO-DO print the results and performance
               }
           }
        } catch (Exception e) {
           System.err.println("Search failed: " + e.getMessage());
           e.printStackTrace();
        }
    }

    public void disconnect() {
        // TO-DO disconnect the ClientProcessingEngine from the server
        if (engine != null) {
            engine.disconnect();
            System.out.println("Client disconnected from server");
        }
    }
}

public class FileRetrievalBenchmark {
    public static void main(String[] args)
    {
        if (args.length < 4) {
            System.out.println("Usage: java FileRetrievalBenchmark <server_ip> <server_port> <num_clients> <dataset_paths...>");
            System.out.println("Example: java FileRetrievalBenchmark localhost 12345 2 /dataset1 /dataset2");
            return;
        }
        try{
            // TO-DO extract the arguments from args
            String serverIP = args[0];
            String serverPort = args[1];
            int numberOfClients = Integer.parseInt(args[2]); 
            // Collect dataset paths
            ArrayList<String> clientsDatasetPath = new ArrayList<>();
            for (int i = 3; i < args.length; i++) {
                clientsDatasetPath.add(args[i]);
            }
            // TO-DO measure the execution start time
            System.out.println("Starting benchmark with " + numberOfClients + " clients");
            long startTime = System.currentTimeMillis(); 
            // TO-DO create and start benchmark worker threads equal to the number of clients
            ExecutorService executorService = Executors.newFixedThreadPool(numberOfClients);
            ArrayList<BenchmarkWorker> workers = new ArrayList<>();
            for (int i = 0; i < numberOfClients; i++) {
                BenchmarkWorker worker = new BenchmarkWorker(serverIP, serverPort, clientsDatasetPath.get(i));
                workers.add(worker);
                executorService.execute(worker); // TO-DO join the benchmark worker threads
            }
            // Wait for all indexing to complete
            executorService.shutdown();
            if (!executorService.awaitTermination(30, TimeUnit.MINUTES)) {
               System.err.println("Timeout waiting for indexing to complete");
            }
            // TO-DO measure the execution stop time and print the performance
            long stopTime = System.currentTimeMillis();
            double totalTime = (stopTime - startTime) / 1000.0;
            System.out.println("Total indexing time for all clients: " + totalTime + " seconds");
            // TO-DO run search queries on the first client (benchmark worker thread number 1)
            if (!workers.isEmpty()) {
                System.out.println("\nRunning search queries on the first client:");
                workers.get(0).search("Worms");
                workers.get(0).search("distortion AND adaptation");
            }
            // TO-DO disconnect all clients (all benchmakr worker threads)
            System.out.println("\nDisconnecting all clients...");
            for (BenchmarkWorker worker : workers) {
               worker.disconnect();
            }
            System.out.println("Benchmark completed successfully");
        } catch (NumberFormatException e) {
            System.err.println("Error: Invalid number format in arguments");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error during benchmark execution: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
