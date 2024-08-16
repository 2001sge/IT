import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// A class that reads Lubm data.nq datasets and converts them into a text file to make them partition-able.
public class LUBM_data_Reader {
    public static void main(String[] args) {

        String path = "C:/Users/kazem/Downloads/uba1.7/data4.nq-0";

        // Create a map in which we will store the neighbors of each Lubm dataset node.
        Map<String, List<String>> lubm_data_neighbors = new HashMap<>();

        try{

            // The Lubm file is opened for reading.
            BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
            String line;

            // In this loop, the subjects and associated objects are all added to each other in the map as neighbors.
            while ((line = bufferedReader.readLine()) != null) {
                String[] triple = line.split(" ");
                String subject = triple[0];
                String object = triple[2];
                String second_object = triple[3];

                YAGO_ntx_gz_Reader.addNeighbor(lubm_data_neighbors, subject, object);
                YAGO_ntx_gz_Reader.addNeighbor(lubm_data_neighbors, object, subject);
                YAGO_ntx_gz_Reader.addNeighbor(lubm_data_neighbors, subject, second_object);
                YAGO_ntx_gz_Reader.addNeighbor(lubm_data_neighbors, second_object, subject);
                YAGO_ntx_gz_Reader.addNeighbor(lubm_data_neighbors, second_object, object);
                YAGO_ntx_gz_Reader.addNeighbor(lubm_data_neighbors, object, second_object);
            }
            bufferedReader.close();
        }catch (IOException e){
            e.printStackTrace();
        }

        String lubm_data_file = "lubm_data_file.txt";

        // Here we save the information needed for partitioning in a text file.
        try{

            // Allows us to write data into our text file.
            BufferedWriter bufferedWriter = LUBM_merged_Reader.getBufferedWriter(lubm_data_file, lubm_data_neighbors);
            bufferedWriter.close();

        }catch (IOException e){
            e.printStackTrace();
        }
    }
}