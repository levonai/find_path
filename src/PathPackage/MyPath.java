package PathPackage;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Find the shorted path between 2 words.
 * 
 * @author Gal Eliraz Levonai
 *
 */
public class MyPath {// extends Thread {//implements Runnable {
    /*private String source;
    private String target;
    private List<String> dictionary;
    private List<String> path;
    private List<String> bestPath;
    private int[] visited;

   
    public MyPath(String source, String target, List<String> dictionary, List<String> path, List<String> bestPath,
            int[] visited) {
        this.source = source;
        this.target = target;
        this.dictionary = dictionary;
        this.path = path;
        this.bestPath = bestPath;
        this.visited = visited;
    }
*/
    // Returns true if the strings have exactly one character difference
    static boolean singleDiff (String S1, String S2, int l) {
        if (S1.length()!=l || S2.length()!=l) {
            return false;
        }

        int c = 0;
        for (int i=0; i<l; i++) {
            if (S1.charAt(i) != S2.charAt(i)) {
                c++;
            }
        }
        if (c == 1)
            return true;
        return false;
    }
    
    // Creates new dictionary that includes only the words that are the same length as the source.
    // In addition validates that all the characters are letters and set them to lower case.
    static List<String> validateDictionary (List<String> orgDictionary, int length) {
        
        List<String> updateDictionary = new ArrayList<String>();
        for (int i=0; i<orgDictionary.size(); i++) {
            String s = orgDictionary.get(i).toLowerCase();
            if (s.length()==length || s.chars().allMatch(Character::isLetter)) {
                updateDictionary.add(s);
            }
        }
        return updateDictionary;
    }
    
    // Validates the input parameters
    static boolean  validateParameters (List<String> dictionary, String source, String target) {
        if (source == null || source.isEmpty()) {
            System.out.println("Empty source string");
            return false;
        }
        if (target == null || target.isEmpty()) {
            System.out.println("Empty target string");
            return false;
        }
        if (dictionary == null || dictionary.isEmpty()) {
            System.out.println("Empty dictionary");
            return false;
        }
        return true;
    }
    
    // Print out the path
    static void printPath (List<String> path, String source, String target) {
        if (path == null)
            return;
        if (path.isEmpty()) {
            System.out.printf("%s to %s no path found", source, target); 
            System.out.println();
            return;
        }
        System.out.printf ("{");
        for (int i=0; i<path.size(); i++)
            System.out.printf (" %s ", path.get(i));
        System.out.println("}");   
    }
    

    // find the shortest path 
    static List<String> findPath (String source, String target, List<String> dictionary,
            List<String> path, List<String> bestPath, int[] visited) {
        
        // Check for the target String
        if (singleDiff(source, target, source.length())) {
            path.add(target);
            if (bestPath.size()>path.size() || bestPath.size()==0) {
                bestPath.clear();
                bestPath.addAll(path);
            } 
            return bestPath;
        }

        // Find all the possible next Strings
        // Parallel: the dictionary can be divided into clusters.
        //      each CPU can build independently the next array
        int[] next = new int[dictionary.size()];
        int count = 0;
        for (int i=0; i<dictionary.size(); i++) {
            if (visited [i] == 0 && singleDiff(source, dictionary.get(i), source.length())) {
                visited[i] = 1;
                next[count]=i;
                count++;
            }
        }
        
        if (count==0)
            return bestPath;
        
        // Add next string to the path and find shortest path to the target
        // Parallel: building the path for each string in the next array can be done in parallel
        // needs to add a comparison between the bestPath from each CPU
        for (int j=0; j<count; j++) { 
            List<String> tPath = new ArrayList<String> ();
            tPath.addAll(path);
            tPath.add(dictionary.get(next[j]));
            bestPath = findPath(dictionary.get(next[j]), target, dictionary, tPath, bestPath, visited);
        }
        
        return bestPath;
    }
    
    
    // find the shortest path with threads
    // threads added only on the first level to limit the number of threads
    static List<String> findPathThread (String source, String target, List<String> dictionary,
            List<String> path, List<String> bestPath, int[] visited) {
        
        // Check for the target String
        if (singleDiff(source, target, source.length())) {
            path.add(target);
            if (bestPath.size()>path.size() || bestPath.size()==0) {
                bestPath.clear();
                bestPath.addAll(path);
            } 
            return bestPath;
        }

        // Find all the possible next Strings
        int[] next = new int[dictionary.size()];
        int count = 0;
        for (int i=0; i<dictionary.size(); i++) {
            if (visited [i] == 0 && singleDiff(source, dictionary.get(i), source.length())) {
                visited[i] = 1;
                next[count]=i;
                count++;
            }
        }
        
        if (count==0)
            return bestPath;
        
        Thread[] threads = new Thread[count];
        List<List<String>> bestPathArray = new ArrayList<List<String>>();
        
        // Add next string to the path and find shortest path to the target
        for (int j=0; j<count; j++) {
            List<String> tPath = new ArrayList<String> ();
            tPath.addAll(path);
            tPath.add(dictionary.get(next[j]));
            List<String> tbestPath = new ArrayList<String> ();
            bestPathArray.add(tbestPath);
            bestPathArray.get(j).addAll(bestPath);
            String s = dictionary.get(next[j]);
            threads[j] = new Thread(new Runnable() {
                @Override
                public void run() {
                    findPath(s, target, dictionary, tPath, tbestPath, visited);
                }
            });
            threads[j].start();
        }
           
        // each thread returns a path, finds the shortest
        try {
            for (int j=0; j<count; j++) {
                threads[j].join();
                 if (bestPathArray.get(j).contains(target) &&
                        (bestPathArray.get(j).size() < bestPath.size() || bestPath.isEmpty())) {
                    bestPath.clear();
                    bestPath.addAll(bestPathArray.get(j));
                 }
            }
        } catch (InterruptedException e) {
                System.out.println("thread error");
        }
        
        return bestPath;
    }
    
    
    // find the shortest path between the source and the target given dictionary
    static void shortestPath (String source, String target, List<String> dictionaryIn) { 
        List<String> path = new ArrayList<String>();
        List<String> bestPath = new ArrayList<String>();
    
        if (!validateParameters (dictionaryIn, source, target)) {
            return;
        }
        List<String> dictionary = validateDictionary(dictionaryIn, source.length());
        if (!dictionary.contains(source) || !dictionary.contains(target)) {
            System.out.println("Dictionary should contain source and target");
            return;
        }
        
        int[] visited = new int[dictionary.size()];
        path.add(source);
        if (source.equals(target)) {
            System.out.printf("{ %s = %s }", source, target);
            System.out.println();
            return;
        }

        for(int i=0; i<dictionary.size(); i++) {
            visited[i] = 0;
        }
        
        printPath (findPathThread (source, target, dictionary, path, bestPath, visited), source, target);
        //printPath (findPath (source, target, dictionary, path, bestPath, visited), source, target);
    }
 
    
/**
 * 
 * main function : test cases
 * 
 */
    
    public static void main(String[] args) { 
        List<String> dictionary = List.of("aaa", "aab", "abb", "bbb", "bbc", "bcc", "ccc", "daa", "dda", "ddd", "dde", "dee",
                "eee", "eec", "ecc", "eaa", "eaf", "ead", "aaf");
       
        shortestPath ("aaa", "bbc", dictionary);
        shortestPath ("bbc", "aaa", dictionary);
        shortestPath ("aaa", "eee", dictionary);
        shortestPath ("aaa", "hhh", dictionary);
        shortestPath ("aaa", "aaa", dictionary);
        shortestPath ("aaa", "", dictionary);
        shortestPath (null, "bbb", dictionary);
        
        List<String> dictionary3 = List.of("aaa", "aab", "abb", "bbb", "bbc", "bcc", "ccc", "daa", "dda", "ddd", "dde", "dee",
                "eee", "eec", "ecc", "eaa", "eaf", "ead", "aaf", "aba", "abc","bcb", "add");
        shortestPath ("aaa", "bbc", dictionary3);
        shortestPath ("bbc", "aaa", dictionary3);
        shortestPath ("aaa", "eee", dictionary3);
        
        List<String> dictionary2 = List.of ("smart", "spark", "start", "stark", "smark", "stack", "slack",
                "black", "clack", "prank", "frank", "blank", "bland", "block", "brand", "bread",
                "great", "glade", "braid", "brain", "drain");
        shortestPath ("smart", "brain", dictionary2);
        shortestPath ("glade", "bland", dictionary2);
    }
}
    
    