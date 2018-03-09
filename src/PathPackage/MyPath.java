package PathPackage;

import java.util.ArrayList;
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
public class MyPath {
    
   
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
    static void printPath (List<String> path) {
        if (path == null)
            return;
        if (path.isEmpty()) {
            System.out.println("No path found"); 
            return;
        }
        System.out.printf ("{");
        for (int i=0; i<path.size(); i++)
            System.out.printf (" %s ", path.get(i));
        System.out.println("}");   
    }
    

    // find the shortest path (BFS)
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
        for (int i=0; i<dictionary.size(); i++) {
            if (visited [i] == 0 && singleDiff(source, dictionary.get(i), source.length())) {
                visited[i] = 1;
                next[i]=1;
            }
        }
        
        // Add next string to the path and find shortest path to the target
        // Parallel: building the path for each string in the next array can be done in parallel
        // needs to add a comparison between the bestPath from each CPU
        for (int i=0; i<dictionary.size(); i++) {
            if (next[i]==1) {
                List<String> tPath = new ArrayList<String> ();
                tPath.addAll(path);
                tPath.add(dictionary.get(i));
                bestPath = findPath(dictionary.get(i), target, dictionary, tPath, bestPath, visited);
            }
        }
        
        return bestPath;
    }

    // find the shortest path between the source and the target given dictionary
    static List<String> shortestPath (String source, String target, List<String> dictionaryIn) { 
        List<String> path = new ArrayList<String>();
        List<String> bestPath = new ArrayList<String>();
    
        if (!validateParameters (dictionaryIn, source, target)) {
            return null;
        }
        List<String> dictionary = validateDictionary(dictionaryIn, source.length());
        if (!dictionary.contains(source) || !dictionary.contains(target)) {
            System.out.println("Dictionary should contain source and target");
            return null;
        }
        
        int[] visited = new int[dictionary.size()];
        path.add(source);
        if (source.equals(target)) {
            path.add(target);
            return path;
        }

        for(int i=0; i<dictionary.size(); i++) {
            visited[i] = 0;
        }
        
        return (findPath (source, target, dictionary, path, bestPath, visited));
    }
 
    
/**
 * 
 * main function : test cases
 * 
 */
    
    public static void main(String[] args) { 
        List<String> dictionary = List.of("aaa", "aab", "abb", "bbb", "bbc", "bcc", "ccc", "daa", "dda", "ddd", "dde", "dee",
                "eee", "eec", "ecc");
        
        printPath (shortestPath ("aaa", "bbc", dictionary));
        printPath (shortestPath ("bbc", "aaa", dictionary));
        printPath (shortestPath ("aaa", "eee", dictionary));
        printPath (shortestPath ("aaa", "hhh", dictionary));
        printPath (shortestPath ("aaa", "aaa", dictionary));
        printPath (shortestPath ("aaa", "", dictionary));
        printPath (shortestPath (null, "bbb", dictionary));
        
        List<String> dictionary2 = List.of ("smart", "spark", "start", "stark", "smark", "stack", "slack",
                "black", "clack", "prank", "frank", "blank", "bland", "block", "brand", "bread",
                "great", "glade", "braid", "brain", "drain");
        printPath (shortestPath ("smart", "brain", dictionary2));
        printPath (shortestPath ("glade", "bland", dictionary2));
    }
}
    
    