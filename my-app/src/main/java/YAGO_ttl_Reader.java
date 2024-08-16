import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// This class stores all nodes and their associated neighbors of a Yago .ttl dataset file in a text file
// in order to then partition it.
public class YAGO_ttl_Reader {
    public static void main(String[] args) {

        // Path to our Yago file.
        String path = "C:/Users/kazem/Downloads/yago-3.0.2-turtle-simple/yagoLiteralFacts.ttl";

        // Create a map in which we will store the neighbors of each Yago dataset node.
        Map<String, List<String>> yago_ttl_neighbors = new HashMap<>();

        try{

            // The Yago file is opened for reading.
            BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(path));
            String line;

            // Each individual line is now read out and the required data is saved.
            while ((line = bufferedReader.readLine()) != null) {

                // Every line containing a Rdf triple starts with a '<'.
                if(line.startsWith("<")){

                    // Since in the Yago files the subjects are placed first and
                    // the objects third within a line, we store these.
                    String[] triple = line.split("\t");
                    String subject = triple[0];
                    String object = triple[2];

                    String subjectName = subject.substring(1, subject.length()-1);

                    String objectName;

                    // There are three different types of objects when using the .ttl file,
                    // so we provide a different name storage for each type.
                    if (object.endsWith(">")){

                        int start_index = object.indexOf("\"") + 1;
                        int end_index = object.indexOf("\"", start_index);
                        objectName = object.substring(start_index, end_index);
                        start_index = object.indexOf("<") + 1;
                        end_index = object.indexOf(">", start_index);
                        objectName = objectName + object.substring(start_index, end_index);

                    } else if (object.endsWith("\"")) {

                        int start_index = object.indexOf("\"") + 1;
                        int end_index = object.indexOf("\"", start_index);
                        objectName = object.substring(start_index, end_index);
                        objectName = objectName.replaceAll(" ", "_");

                    }else{

                        int start_index = object.indexOf("\"") + 1;
                        int end_index = object.indexOf("\"", start_index);
                        objectName = object.substring(start_index, end_index);

                    }

                    // In our neighbors map, the subject now receives the object as a neighbor and vice versa.
                    YAGO_ntx_gz_Reader.addNeighbor(yago_ttl_neighbors, subjectName, objectName);
                    YAGO_ntx_gz_Reader.addNeighbor(yago_ttl_neighbors, objectName, subjectName);
                }
            }
            bufferedReader.close();
        }catch (IOException e){
            e.printStackTrace();
        }

        String yago_ttl_file = "yago_ttl_file.txt";

        // Here we save the information needed for partitioning in a text file.
        try{

            // Allows us to write data into our text file.
            BufferedWriter bufferedWriter = LUBM_merged_Reader.getBufferedWriter(yago_ttl_file, yago_ttl_neighbors);
            bufferedWriter.close();

        }catch (IOException e){
            e.printStackTrace();
        }
    }
}