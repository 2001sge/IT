import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

// This class stores all nodes and their associated neighbors of a Yago .ntx.gz dataset file in a text file
// in order to then partition it.
public class YAGO_ntx_gz_Reader {

    // Here we create a function that maps the nodes from the data sets to their neighbors.
    public static void addNeighbor(Map<String, List<String>> node_neighbors, String firstNode, String secondNode){

        // We create a list in the map for the current firstNode to store its neighbors
        // if no list exists for firstNode yet.
        node_neighbors.putIfAbsent(firstNode, new ArrayList<>());

        // Add secondNode to firstNode's neighbor list if secondNode is not already in firstNode's neighbor list.
        if(!node_neighbors.get(firstNode).contains(secondNode)){
            node_neighbors.get(firstNode).add(secondNode);
        }
    }

    // A method to obtain only the names of each subject and object.
    private static String getNameOnly(String longName) {

        // First, the brackets at the beginning and end of the name are removed.
        longName = longName.substring(1, longName.length() - 1);

        // The position of the last slash ("/") before the actual name of the subject/object is saved.
        int index_before_name = longName.lastIndexOf('/');

        // The name without the removed closing bracket is returned.
        return longName.substring(index_before_name + 1);
    }

    public static void main(String[] args) {

        // Path to our Yago file.
        String path = "C:/Users/kazem/OneDrive/Dokumente/Bachelorarbeit/bachelor/yago-wd-annotated-facts-small.ntx.gz";

        // Create a map in which we will store the neighbors of each Yago dataset node.
        Map<String, List<String>> yago_ntx_gz_neighbors = new HashMap<>();

        try{

            // The following line reads the contents of our .ntx.gz Yago file and makes it available
            // for further processing.
            GZIPInputStream gzip = new GZIPInputStream(Files.newInputStream(Paths.get(path)));

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(gzip));
            String line;

            // Each individual line is now read out and the required data is saved.
            while ((line = bufferedReader.readLine()) != null) {

                // Since in the Yago files the subjects are placed first and
                // the objects third within a line, we store these.
                String[] triple = line.split("\t");
                String subject = triple[1];
                String object = triple[3];

                // With our getNameOnly method we store the names of the subject and object of the current line.
                String subjectName = getNameOnly(subject);
                String objectName = getNameOnly(object);

                // In our neighbors map, the subject now receives the object as a neighbor and vice versa.
                addNeighbor(yago_ntx_gz_neighbors, subjectName, objectName);
                addNeighbor(yago_ntx_gz_neighbors, objectName, subjectName);
            }
            bufferedReader.close();
            gzip.close();
        }catch (IOException e){
            e.printStackTrace();
        }

        String yago_ntx_gz_file = "yago_ntx_gz_file.txt";

        // Here we save the information needed for partitioning in a text file.
        try{

            // Allows us to write data into our text file.
            BufferedWriter bufferedWriter = LUBM_merged_Reader.getBufferedWriter(
                    yago_ntx_gz_file, yago_ntx_gz_neighbors);
            bufferedWriter.close();

        }catch (IOException e){
            e.printStackTrace();
        }
    }
}