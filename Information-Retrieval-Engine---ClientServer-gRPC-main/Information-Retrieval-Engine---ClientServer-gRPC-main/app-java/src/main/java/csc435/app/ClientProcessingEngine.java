package csc435.app;

import csc435.app.FileRetrievalEngineGrpc.FileRetrievalEngineBlockingStub;

import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;

import java.io.File;
import java.nio.charset.MalformedInputException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

class IndexResult {
    public double executionTime;
    public long totalBytesRead;

    public IndexResult(double executionTime, long totalBytesRead) {
        this.executionTime = executionTime;
        this.totalBytesRead = totalBytesRead;
    }
}

class DocPathFreqPair {
    public String documentPath;
    public long wordFrequency;

    public DocPathFreqPair(String documentPath, long wordFrequency) {
        this.documentPath = documentPath;
        this.wordFrequency = wordFrequency;
    }
}

class SearchResult {
    public double excutionTime;
    public ArrayList<DocPathFreqPair> documentFrequencies;

    public SearchResult(double executionTime, ArrayList<DocPathFreqPair> documentFrequencies) {
        this.excutionTime = executionTime;
        this.documentFrequencies = documentFrequencies;
    }
}

public class ClientProcessingEngine {
    // TO-DO keep track of the connection
    ManagedChannel channel;
    FileRetrievalEngineBlockingStub stub;
    private int clientID;

    public ClientProcessingEngine(int clientID) {
        this.clientID = clientID;
     }

    public IndexResult indexFiles(String folderPath) {
        IndexResult result = new IndexResult(0.0, 0);
        long startTime = System.currentTimeMillis(); // TO-DO get the start time
        long totalBytesRead = 0;
        try {
            LinkedList<File> dirsToProcess = new LinkedList<>();
            dirsToProcess.add(new File(folderPath));
                while (!dirsToProcess.isEmpty()) {
                File currentDir = dirsToProcess.poll();
                File[] filesInList = currentDir.listFiles();
                if (filesInList == null) {
                    System.out.println("No files found in directory: " + folderPath);
                    return new IndexResult(0, 0);
                }
                System.out.println("Starting indexing of " + filesInList.length + " files in directory: " + folderPath);
                for (File file : filesInList) { // TO-DO crawl the folder path and extrac all file paths
                    if (file.isFile()) {
                        if (file.getName().equals(".DS_Store")) {
                            System.out.println("Skipping hidden file: " + file.getPath());
                            continue;
                        }
                        System.out.println("File being processed: " + file.getPath());
                        long fileSize = file.length();
                        totalBytesRead += fileSize; // TO-DO increment the total number of bytes read
                        // Get word frequencies for the current file
                        HashMap<String, Long> wordFrequencies = getWordFrequencies(file.getPath()); // TO-DO for each file extract all alphanumeric terms that are larger than 2 characters
                        // Create gRPC request                                                      //       and count their frequencies
                        IndexReq request = IndexReq.newBuilder()
                            .setClientId(clientID)
                            .setDocumentPath(file.getPath())
                            .putAllWordFrequencies(wordFrequencies)
                            .build();
                        // Make RPC call
                        IndexRep response = stub.computeIndex(request); // TO-DO for each file perform a remote procedure call to the server by calling the gRPC client stub
                        System.out.println("Index response: " + response.getAck());
                    } else if (file.isDirectory()) {
                        dirsToProcess.add(file);
                    }
                }
            }
            long stopTime = System.currentTimeMillis(); // TO-DO get the stop time and calculate the execution time
            result = new IndexResult((stopTime - startTime) / 1000.0, totalBytesRead);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;  // TO-DO return the execution time and the total number of bytes read
    }

    private HashMap<String, Long> getWordFrequencies(String filePath) {
        HashMap<String, Long> wordCounts = new HashMap<>();
        List<String> stopwords = Arrays.asList("and", "the", "is", "in", "of", "a", "to", "it");
        try {
            if (filePath.endsWith(".DS_Store")) {
                System.out.println("Skipping system file: " + filePath);
                return wordCounts;
            }
            try (Stream<String> lines = Files.lines(Paths.get(filePath))) {
                lines.forEach(line ->
                    Arrays.stream(line.split("[^a-zA-Z0-9]+"))
                            .map(String::toLowerCase)
                            .filter(word -> word.length() > 2)
                            .filter(word -> !stopwords.contains(word))
                            .forEach(word -> wordCounts.merge(word, 1L, Long::sum))
                );
            }
        } catch (MalformedInputException e) {
            System.err.println("Skipping file due to encoding issues: " + filePath);
        } catch (Exception e) {
            System.err.println("Error reading file: " + filePath);
            e.printStackTrace();
        }
        return wordCounts;
    }
    
    public SearchResult searchFiles(ArrayList<String> terms) {
        SearchResult result = new SearchResult(0.0, new ArrayList<DocPathFreqPair>());
        long startTime = System.currentTimeMillis(); // TO-DO get the start time
        try {
            // Validate search terms
            if (terms == null || terms.isEmpty()) {
                System.out.println("Search terms cannot be empty");
                return new SearchResult(0.0, new ArrayList<>());
            }
            // Create search request
            SearchReq request = SearchReq.newBuilder()
                .addAllTerms(terms)
                .build();
            // Make RPC call
            SearchRep response = stub.computeSearch(request);  // TO-DO perform a remote procedure call to the server by calling the gRPC client stub
            // Process results
            ArrayList<DocPathFreqPair> results = new ArrayList<>();
            for (Map.Entry<String, Long> entry : response.getSearchResultsMap().entrySet()) {
                results.add(new DocPathFreqPair(entry.getKey(), entry.getValue()));
            }
             // Sort results by frequency in descending order and get top 10
            results.sort((a, b) -> Long.compare(b.wordFrequency, a.wordFrequency));
            if (results.size() > 10) {
            results = new ArrayList<>(results.subList(0, 10));
            }
            double executionTime = (System.currentTimeMillis() - startTime) / 1000.0;  // TO-DO get the stop time and calculate the execution time
            return new SearchResult(executionTime, results); // TO-DO return the execution time and the top 10 documents and frequencies
        } catch (Exception e) {
            e.printStackTrace();
            return new SearchResult(0.0, new ArrayList<>());
        }
    }

    public void connect(String serverIP, String serverPort) {
        // TO-DO create communication channel with the gRPC Server
        try {
            channel = Grpc.newChannelBuilder(
                serverIP + ":" + serverPort,
                InsecureChannelCredentials.create()
            ).build();
            stub = FileRetrievalEngineGrpc.newBlockingStub(channel); // TO-DO create gRPC client stub
            System.out.println("Connected to server at " + serverIP + ":" + serverPort);
        } catch (Exception e) {
            System.err.println("Failed to connect to server: " + e.getMessage());
        }
    }

      public void disconnect() {
        if (channel != null && !channel.isShutdown()) {
            try {
                channel.shutdown();
                if (!channel.awaitTermination(5, TimeUnit.SECONDS)) {
                    System.err.println("Timed out waiting for channel shutdown");
                }
                System.out.println("Disconnected from server");
            } catch (InterruptedException e) {
                System.err.println("Error during disconnect: " + e.getMessage());
            }
        }
    }

    public int getClientID() {
        return clientID;
    }

    public void setClientID(int clientID) {
        this.clientID = clientID;
    }
}

