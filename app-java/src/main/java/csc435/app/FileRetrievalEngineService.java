package csc435.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.grpc.stub.StreamObserver;

public class FileRetrievalEngineService extends FileRetrievalEngineGrpc.FileRetrievalEngineImplBase {
    private IndexStore store;
    
    public FileRetrievalEngineService(IndexStore store) {
        this.store = store;
    }

    @Override
    public void computeIndex(IndexReq request, StreamObserver<IndexRep> responseObserver) {
        responseObserver.onNext(doIndex(request));
        responseObserver.onCompleted();
    }

    @Override
    public void computeSearch(SearchReq request, StreamObserver<SearchRep> responseObserver) {
        responseObserver.onNext(doSearch(request));
        responseObserver.onCompleted();
    }

    private IndexRep doIndex(IndexReq request) {
        // TO-DO update global index with temporary index received from the request
        // TO-DO send an OK message as the reply
        String documentPath = request.getDocumentPath();
        Map<String, Long> wordFrequencies = request.getWordFrequenciesMap();
        // Get the document number for the document path
        long documentNumber = store.putDocument(documentPath);
        HashMap<String, Long> frequencyMap = new HashMap<>(wordFrequencies);
        for (Map.Entry<String, Long> entry : wordFrequencies.entrySet()) {
            frequencyMap.put(entry.getKey(), entry.getValue().longValue());
        }
        store.updateIndex(documentNumber, frequencyMap);
        return IndexRep.newBuilder().setAck("Index updated successfully").build();
    }

    private SearchRep doSearch(SearchReq request) {
        // Get the search terms from the request
        ArrayList<String> searchTerms = new ArrayList<>(request.getTermsList());
        // Create a HashMap to store the search results
        HashMap<String, Long> searchResults = new HashMap<>();
        // TO-DO do lookup over the global index given the search term from the request
        // TO-DO send the results as the reply message
        for (String term : searchTerms) {
            ArrayList<DocFreqPair> results = store.lookupIndex(term);
            for (DocFreqPair pair : results) {
                String documentPath = store.getDocument(pair.documentNumber);
                if (documentPath != null) {
                    searchResults.merge(documentPath, pair.wordFrequency, Long::sum);
                }
            }
        }
        // Build the SearchRep message with the search results
        SearchRep.Builder builder = SearchRep.newBuilder();
        for (Map.Entry<String, Long> entry : searchResults.entrySet()) {
            builder.putSearchResults(entry.getKey(), entry.getValue());
        }
        // TO-DO send the results as the reply message
        return builder.build();
    }
}

