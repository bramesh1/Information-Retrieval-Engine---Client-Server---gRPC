package csc435.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

// Data structure that stores a document number and the number of time a word/term appears in the document
class DocFreqPair {
    public long documentNumber;
    public long wordFrequency;

    public DocFreqPair(long documentNumber, long wordFrequency) {
        this.documentNumber = documentNumber;
        this.wordFrequency = wordFrequency;
    }
}

public class IndexStore {
    // TO-DO declare data structure that keeps track of the DocumentMap
    private HashMap<String, Long> documentMap;
    // TO-DO declare data structures that keeps track of the TermInvertedIndex
    private HashMap<String, ArrayList<DocFreqPair>> termInvertedIndex;
    // TO-DO declare two locks, one for the DocumentMap and one for the TermInvertedIndex
    private ReentrantLock documentMapLock;
    private ReentrantLock termInvertedIndexLock;
    private long nextDocumentNumber;

    public IndexStore() {
        // TO-DO initialize the DocumentMap and TermInvertedIndex members
        documentMap = new HashMap<>();
        termInvertedIndex = new HashMap<>();
        documentMapLock = new ReentrantLock();
        termInvertedIndexLock = new ReentrantLock();
        nextDocumentNumber = 1;
    }

    public long putDocument(String documentPath) {
        // TO-DO assign a unique number to the document path and return the number
        // IMPORTANT! you need to make sure that only one thread at a time can access this method
        documentMapLock.lock();
        try {
            // Assign a unique number to the document path and return the number
            long documentNumber = nextDocumentNumber++;
            documentMap.put(documentPath, documentNumber);
            return documentNumber;
        } finally {
            documentMapLock.unlock();
        }
    }

    public String getDocument(long documentNumber) {
        // TO-DO retrieve the document path that has the given document number
         for (Map.Entry<String, Long> entry : documentMap.entrySet()) {
            if (entry.getValue() == documentNumber) { //if number provided as arg is equal to entry set value then we return entry set key that is docPath
                return entry.getKey();
            }
        }
        return null; // Returning null if provided documentNumber does not match the entry set value.
    }

    public void updateIndex(long documentNumber, HashMap<String, Long> wordFrequencies) {
        termInvertedIndexLock.lock();
        // TO-DO update the TermInvertedIndex with the word frequencies of the specified document
        // IMPORTANT! you need to make sure that only one thread at a time can access this method
        try{
            for (String term : wordFrequencies.keySet()){ //Traversing all the keys in wordFrequencies key set i.e. keys are words
                long frequency = wordFrequencies.get(term); //Retrieving the frequency for the specified word given as arg
                if(!termInvertedIndex.containsKey(term)){ // If termInvertedIndex does not contain a word
                    termInvertedIndex.put(term, new ArrayList<>()); //Then We add that word in termInvertedIndex as key and also add a new object of
                }                                                   // ArrayList that holds pairs the documentNumber and word frequency as our value in tmm.
                termInvertedIndex.get(term).add(new DocFreqPair(documentNumber, frequency)); //Here we actually update our termInvertedIndex.
            }
        } finally {
            termInvertedIndexLock.unlock();
        }
    }

    public ArrayList<DocFreqPair> lookupIndex(String term) {
        ArrayList<DocFreqPair> results = new ArrayList<>();
        // TO-DO return the document and frequency pairs for the specified term
        if (termInvertedIndex.containsKey(term)){
            results = new ArrayList<>(termInvertedIndex.get(term));
        }
        return results;
    }
}

