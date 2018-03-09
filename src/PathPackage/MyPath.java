package PathPackage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 *  Given two five letter words, A and B, and a dictionary of five letter words,
 *  find a shortest transformation from A to B, such that only one letter can be changed at a time and
 *  all intermediate words in the transformation must exist in the dictionary.  
 *  For example, if A and B are "smart" and "brain", the result may be: 
 *  { smart start stark stack slack black blank bland brand braid brain }
 *   
 *  Your implementation should take advantage of multiple CPU cores.
 *  Please also include test cases against your algorithm.
 *  Your solution should produce the words that make up the shortest path itself, not just the count. 
 *  
 * @author Gal Eliraz-Levonai
 *
 */
public class MyPath {
    
   
    // returns true if the strings have a single character difference
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

    // returns the number of different characters
    static int diffCount (String S1, String S2, int l) {
        if (S1.length()!=l || S2.length()!=l) {
            return (S1.length() < S2.length() ? S2.length() : S1.length());
        }

        int count = 0;
        for (int i=0; i<l; i++) {
            if (S1.charAt(i) != S2.charAt(i)) {
                count++;
            }
        }
        return count;
    }
    
    // Creates new dictionary that includes only the words that are the same length as the source
    // also validate all the characters are letters and set them to lower case
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
    
    // validate the input parameters
    static boolean  validateParameters (List<String> dictionary, String source, String target) {
        if (source == null || source.isEmpty()) {
            System.out.println("empty source string");
            return false;
        }
        if (target == null || target.isEmpty()) {
            System.out.println("empty target string");
            return false;
        }
        if (dictionary == null || dictionary.isEmpty()) {
            System.out.println("empty dictionary");
            return false;
        }
        return true;
    }
    
    // print out the best path
    static void printPath (List<String> path) {
        if (path == null || path.isEmpty()) {
            System.out.println("no path found"); 
            return;
        }
        System.out.printf ("{");
        for (int i=0; i<path.size(); i++)
            System.out.printf (" %s ", path.get(i));
        System.out.println("}");   
    }
    
/**
 *
 * solution 1: recursive function 
 *
 */

    static List<String> findPath (String tempStart, String target, List<String> dictionary,
            List<String> tempPath, List<String> bestPath, int[] visited) {
        
        // check for the target String
        if (singleDiff(tempStart, target, tempStart.length())) {
            tempPath.add(target);
            if (bestPath.size()>tempPath.size() || bestPath.size()==0) {
                bestPath.clear();
                bestPath.addAll(tempPath);
            } 
            return bestPath;
        }

        // find all the possible next Strings
        // Parallel: building the list of the nestSteps can be done on parallel
        int[] next = new int[dictionary.size()];
        for (int i=0; i<dictionary.size(); i++) {
            if (visited [i] == 0 && singleDiff(tempStart, dictionary.get(i), tempStart.length())) {
                visited[i] = 1;
                next[i]=1;
            }
        }
        
        
        // add the nestStep to the path and recursive build on the unvisited strings
        // Parallel: building the path for each string in the list tempNext can be done in parallel
        // needs to add a comparison between the bestPath from each CPU
        for (int i=0; i<dictionary.size(); i++) {
            if (next[i]==1) {
                List<String> tPath = new ArrayList<String> ();
                tPath.addAll(tempPath);
                tPath.add(dictionary.get(i));
                bestPath = findPath(dictionary.get(i), target, dictionary, tPath, bestPath, visited);
            }
        }
        
        return bestPath;
    }
    
    static List<String> shortestPath (String source, String target, List<String> dictionaryIn) { 
        List<String> path = new ArrayList<String>();   
        List<String> bestPath = new ArrayList<String>();
    
        if (!validateParameters (dictionaryIn, source, target)) {
            return bestPath;
        }
        List<String> dictionary = validateDictionary(dictionaryIn, source.length());
        if (!dictionary.contains(source) || !dictionary.contains(target))
            return bestPath;
    
        int[] visited = new int[dictionary.size()];
        path.add(source);
    
        for(int i=0; i<dictionary.size(); i++) {
            visited[i] = 0;
        }

        return (findPath (source, target, dictionary, path, bestPath, visited));
    }
 
    
/**
 *
 * solution 2: based on Dijkstra algorithm for the shortest path 
 *
 */

    // initializing the variables
    static int dijkstraInit (String source, String target, List<String> dictionary,
            int[] distance, int[] visited, int[] previous) {

        int maxDistance = dictionary.size()+1;
        int sourceIndex = 0;
        int targetIndex = 0;

        for (int i = 0; i < dictionary.size(); i++) {
            distance[i] = maxDistance;
            visited[i] = 0;

            int count = diffCount(source,  dictionary.get(i), source.length());
            if (count == 1) { 
                distance[i] = count;
            }
            if (count == 0) { 
                visited[i] = 1;
                distance[i] = 0;
                sourceIndex = i;
            }
            if (diffCount (target,  dictionary.get(i), target.length()) == 0) {
                targetIndex = i;
            }
        }
        for (int i = 0; i < dictionary.size(); i++) {
            previous[i] = sourceIndex;
        }
        
        return targetIndex;
    }

    // construct the shortest path
    static List<String> dijkstraPath (int sourceIndex, int targetIndex, List<String> dictionary,
            List<String> bestPath, int[] distance, int[] previous, int maxDistance) {
        int i = targetIndex;
        if (distance[i] == maxDistance) {
            return bestPath;
        }
        while (i != sourceIndex) {
            bestPath.add (0, dictionary.get(i));
            i = previous[i];
        }
        bestPath.add(0, dictionary.get(sourceIndex));
        return bestPath;
    }
    
    
    // dijkstra algorithm
    static List<String> dijkstra (String source, String target, List<String> dictionaryIn)
    {
        List<String> bestPath = new ArrayList<String> ();
        if (!validateParameters (dictionaryIn, source, target)) {
            return bestPath;
        }
        List<String> dictionary = validateDictionary(dictionaryIn, source.length());
        if (!dictionary.contains(source) || !dictionary.contains(target))
            return bestPath;
        
        int sourceIndex = 0;
        int targetIndex = 0;
        int maxDistance = dictionary.size()+1;
        int minDistance = maxDistance;
        int nextStep = 0;
        
        int[] distance = new int[dictionary.size()];
        int[] visited = new int[dictionary.size()];
        int[] previous = new int[dictionary.size()];
        
        // initializing the variables
        targetIndex = dijkstraInit (source, target, dictionary, distance, visited, previous);
        sourceIndex = previous[0];        
        
        for (int j = 0; j < dictionary.size(); j++) {
            minDistance = maxDistance;
            // choose the next string 
           for (int i = 0; i < dictionary.size(); i++) {
                if (minDistance > distance[i] && visited[i] != 1) {
                    minDistance = distance[i];
                    nextStep = i;
                }
            }
            
            // mark nextStep as visited
            visited[nextStep] = 1;
            
            // update the path to all the strings with the nextStep
            for (int i = 0; i < dictionary.size(); i++) {
                if (visited[i] != 1 && singleDiff(dictionary.get(nextStep), dictionary.get(i), source.length())) {
                    // check if the path using nextStep is shorter 
                    if (minDistance+1 < distance[i]) {
                        distance[i] = minDistance+1;
                        previous[i] = nextStep;
                    }
                }
            }
        }
        // construct the shortest path
        return dijkstraPath (sourceIndex, targetIndex, dictionary, bestPath, distance, previous, maxDistance);
    }
    
    
    static List<String> dijkstraWithThreads (String source, String target, List<String> dictionaryIn)
    {
        List<String> bestPath = new ArrayList<String> ();
        if (!validateParameters (dictionaryIn, source, target)) {
            return bestPath;
        }
        List<String> dictionary = validateDictionary(dictionaryIn, source.length());
        if (!dictionary.contains(source) || !dictionary.contains(target))
            return bestPath;
        
        int sourceIndex = 0;
        int targetIndex = 0;
        int maxDistance = dictionary.size()+1;
        int minDistance = maxDistance;
        int nextStep = 0;
        
        int[] distance = new int[dictionary.size()];
        int[] visited = new int[dictionary.size()];
        int[] previous = new int[dictionary.size()];
        
        // initializing the variables
        targetIndex = dijkstraInit (source, target, dictionary, distance, visited, previous);
        sourceIndex = previous[0];   
        
        for (int j = 0; j < dictionary.size(); j++) {
            minDistance = maxDistance;
            // choose the next string 
            // Parallel: for large dictionary, it can be divided into cluster, and check each cluster on parallel
            // then merge the results
            for (int i = 0; i < dictionary.size(); i++) {
                if (minDistance > distance[i] && visited[i] != 1) {
                    minDistance = distance[i];
                    nextStep = i;
                }
            }
            
            // mark nextStep as visited
            visited[nextStep] = 1;
            
            // update the path to all the strings with the nextStep
            // Parallel: updating the path can be done in parallel since it's independent for each string
            for (int i = 0; i < dictionary.size(); i++) {
                if (visited[i] != 1 && singleDiff(dictionary.get(nextStep), dictionary.get(i), source.length())) {
                    // check if the path using nextStep is shorter 
                    if (minDistance+1 < distance[i]) {
                        distance[i] = minDistance+1;
                        previous[i] = nextStep;
                    }   
                }
            }
        }
        // construct the shortest path
        return dijkstraPath (sourceIndex, targetIndex, dictionary, bestPath, distance, previous, maxDistance);
    }
    
    
/**
 * 
 * main function : test cases
 * 
 */
    
    public static void main(String[] args) { 
        List<String> dictionary = List.of("aaa", "aab", "abb", "bbb", "bbc", "bcc", "ccc", "daa", "dda", "ddd", "dde", "dee",
                "eee", "eec", "ecc");

        String source = "aaa";
        String target = "bbc";
        
        printPath (shortestPath (source, target, dictionary));
        printPath (dijkstra (source, target, dictionary));
        printPath (dijkstraWithThreads (source, target, dictionary));
        
        source = "eee";
        printPath (shortestPath (source, target, dictionary));
        printPath (dijkstra (source, target, dictionary));
        printPath (dijkstraWithThreads (source, target, dictionary));

        source = "hhh";
        printPath (shortestPath (source, target, dictionary));
        printPath (dijkstra (source, target, dictionary));
        
        source = "eeee";
        printPath (shortestPath (source, target, dictionary));
        printPath (dijkstra (source, target, dictionary));
        
        source = "aaa";
        target = "";
        printPath (shortestPath (source, target, dictionary));
        printPath (dijkstra (source, target, dictionary));
        
        source = "";
        target = "";
        printPath (shortestPath (source, target, dictionary));
        printPath (dijkstra (source, target, dictionary));
        
        source = "";
        target = null;
        printPath (shortestPath (source, target, dictionary));
        printPath (dijkstra (source, target, dictionary));
        
        
        List<String> dictionary2 = List.of ("smart", "spark", "start", "stark", "smark", "stack", "slack",
                "black", "clack", "prank", "frank", "blank", "bland", "block", "brand", "bread",
                "great", "glade", "braid", "brain", "drain");
        source = "smart";
        target = "brain";
        printPath (shortestPath (source, target, dictionary2));
        printPath (dijkstra (source, target, dictionary2));
        printPath (dijkstraWithThreads (source, target, dictionary2));

        System.out.println("done");
    }
}
    
    