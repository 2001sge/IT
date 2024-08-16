import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// This class stores all nodes and their associated neighbors from a previously generated Lubm merged.nq dataset file
// in a text file in order to then partition it.
public class LUBM_merged_Reader {
    public static void main(String[] args) {

        // Path to our previously generated Lubm dataset.
        String path = "C:/Users/kazem/Downloads/uba1.7/merged10.nq";

        boolean found_class = false;
        String class_element = "";
        boolean Department = false;
        String addDepartment = "";
        boolean found_class_two = false;
        String class_two_element = "";

        // Create a map in which we will store the neighbors of each Lubm dataset node.
        Map<String, List<String>> lubm_merged_neighbors = new HashMap<>();

        try {

            // The Lubm file is opened for reading.
            BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
            String line;

            // Each individual line is now read out and the required data is saved.
            while ((line = bufferedReader.readLine()) != null) {

                // Existing empty lines are skipped.
                if (line.trim().isEmpty()) {
                    continue;
                }

                // We split a line into its different parts and store them as strings in an array.
                String[] parts = line.trim().split("\\s+");

                // If we have an opening class, we set a bool to true to be able to access
                // its neighbors within the owl file.
                if (parts[0].startsWith("<ub:FullProfessor") ||
                        parts[0].startsWith("<ub:AssociateProfessor") ||
                        parts[0].startsWith("<ub:AssistantProfessor") ||
                        parts[0].startsWith("<ub:Lecturer") ||
                        parts[0].startsWith("<ub:UndergraduateStudent") ||
                        parts[0].startsWith("<ub:GraduateStudent") ||
                        parts[0].startsWith("<ub:ResearchGroup") ||
                        parts[0].startsWith("<ub:Publication") ||
                        parts[0].startsWith("<ub:Course") ||
                        parts[0].startsWith("<ub:GraduateCourse") ||
                        parts[0].startsWith("<ub:TeachingAssistant")) {
                    found_class = true;
                    class_element = parts[1];
                    int index = parts[0].indexOf(':');
                    class_two_element = parts[0].substring(index + 1);
                    found_class_two = true;
                }

                // When the class is closed again, we set the bool back to false.
                if (parts[0].startsWith("</ub:FullProfessor") ||
                        parts[0].startsWith("</ub:AssociateProfessor") ||
                        parts[0].startsWith("</ub:AssistantProfessor") ||
                        parts[0].startsWith("</ub:Lecturer") ||
                        parts[0].startsWith("</ub:UndergraduateStudent") ||
                        parts[0].startsWith("</ub:GraduateStudent") ||
                        parts[0].startsWith("</ub:ResearchGroup") ||
                        parts[0].startsWith("</ub:Publication") ||
                        parts[0].startsWith("</ub:Course") ||
                        parts[0].startsWith("</ub:GraduateCourse") ||
                        parts[0].startsWith("</ub:TeachingAssistant")) {
                    found_class = false;
                    found_class_two = false;
                }

                if (parts[0].startsWith("<ub:Department")) {
                    Department = true;
                }

                if (parts[0].startsWith("</ub:Department")) {
                    Department = false;
                }

                if (Department){
                    if (parts[0].startsWith("<ub:name")){
                        addDepartment = parts[0].substring(("<ub:name>".length()),
                                (line.indexOf("</ub:name>") - 3));
                    }
                    if (parts[0].startsWith("<ub:University")){
                        String shortName = parts[1].substring((parts[1].indexOf("\"") + 1),
                                parts[1].lastIndexOf("\""));
                        YAGO_ntx_gz_Reader.addNeighbor(lubm_merged_neighbors, addDepartment, shortName);
                        YAGO_ntx_gz_Reader.addNeighbor(lubm_merged_neighbors, shortName, addDepartment);
                    }
                }

                if (parts[0].startsWith("<ub:ResearchAssistant")){
                    int index = parts[0].indexOf(':');
                    class_two_element = parts[0].substring(index + 1);
                    found_class_two = true;
                }

                if (parts[0].startsWith("</ub:ResearchAssistant")){
                    found_class_two = false;
                }

                // If we are inside an owl class, the following if statement == true.
                if (found_class){

                    // The following command only saves the link of the class
                    // and not additional brackets and rdf keywords.
                    String short_name_subject = class_element.substring((class_element.indexOf("\"") + 1),
                            class_element.lastIndexOf("\""));

                    // Once a predicate is found, we go into the body of the predicate to access the object.
                    if (parts[0].startsWith("<ub:teacherOf") ||
                            parts[0].startsWith("<ub:takesCourse") ||
                            parts[0].startsWith("<ub:advisor") ||
                            parts[0].startsWith("<ub:University") ||
                            parts[0].startsWith("<ub:worksFor") ||
                            parts[0].startsWith("<ub:memberOf") ||
                            parts[0].startsWith("<ub:subOrganizationOf") ||
                            parts[0].startsWith("<ub:publicationAuthor") ||
                            parts[0].startsWith("<ub:teachingAssistantOf")) {

                        // We save the link of the object.
                        String short_name_object = parts[1].substring((parts[1].indexOf("\"") + 1),
                                parts[1].lastIndexOf("\""));

                        // We add the object as a neighbor of the subject in the neighbor map and vice versa.
                        YAGO_ntx_gz_Reader.addNeighbor(lubm_merged_neighbors, short_name_subject, short_name_object);
                        YAGO_ntx_gz_Reader.addNeighbor(lubm_merged_neighbors, short_name_object, short_name_subject);
                    }

                    // Below we have some special data properties that we have not as a link,
                    // but as normal strings as objects in the owl file.
                    if(parts[0].startsWith("<ub:emailAddress>")){
                        // We store the names of the objects.
                        String short_name_object = parts[0].substring(("<ub:emailAddress>".length()),
                                (line.indexOf("</ub:emailAddress>") - 3));
                        YAGO_ntx_gz_Reader.addNeighbor(lubm_merged_neighbors, short_name_subject, short_name_object);
                        YAGO_ntx_gz_Reader.addNeighbor(lubm_merged_neighbors, short_name_object, short_name_subject);
                    }
                    if(parts[0].startsWith("<ub:telephone>")){
                        String short_name_object = parts[0].substring(("<ub:telephone>".length()),
                                (line.indexOf("</ub:telephone>") - 3));
                        YAGO_ntx_gz_Reader.addNeighbor(lubm_merged_neighbors, short_name_subject, short_name_object);
                        YAGO_ntx_gz_Reader.addNeighbor(lubm_merged_neighbors, short_name_object, short_name_subject);
                    }
                    if(parts[0].startsWith("<ub:researchInterest>")){
                        String short_name_object = parts[0].substring(("<ub:researchInterest>".length()),
                                (line.indexOf("</ub:researchInterest>") - 3));
                        YAGO_ntx_gz_Reader.addNeighbor(lubm_merged_neighbors, short_name_subject, short_name_object);
                        YAGO_ntx_gz_Reader.addNeighbor(lubm_merged_neighbors, short_name_object, short_name_subject);
                    }
                    if(parts[0].startsWith("<ub:name>")){
                        String short_name_object = parts[0].substring(("<ub:name>".length()),
                                (line.indexOf("</ub:name>") - 3));
                        YAGO_ntx_gz_Reader.addNeighbor(lubm_merged_neighbors, short_name_subject, short_name_object);
                        YAGO_ntx_gz_Reader.addNeighbor(lubm_merged_neighbors, short_name_object, short_name_subject);
                    }
                }

                // Here we store which classes our subjects belong to.
                if(found_class_two){
                    if (parts[0].startsWith("<ub:ResearchAssistant") ||
                            parts[0].startsWith("<ub:FullProfessor") ||
                            parts[0].startsWith("<ub:AssociateProfessor") ||
                            parts[0].startsWith("<ub:AssistantProfessor") ||
                            parts[0].startsWith("<ub:Lecturer") ||
                            parts[0].startsWith("<ub:UndergraduateStudent") ||
                            parts[0].startsWith("<ub:GraduateStudent") ||
                            parts[0].startsWith("<ub:ResearchGroup") ||
                            parts[0].startsWith("<ub:Publication") ||
                            parts[0].startsWith("<ub:Course") ||
                            parts[0].startsWith("<ub:GraduateCourse") ||
                            parts[0].startsWith("<ub:TeachingAssistant")){

                        // We save the link of the object.
                        String short_name_object = parts[1].substring((parts[1].indexOf("\"") + 1),
                                parts[1].lastIndexOf("\""));

                        // We add the object as a neighbor of the subject in the neighbor map and vice versa.
                        YAGO_ntx_gz_Reader.addNeighbor(lubm_merged_neighbors, class_two_element, short_name_object);
                        YAGO_ntx_gz_Reader.addNeighbor(lubm_merged_neighbors, short_name_object, class_two_element);
                    }
                }
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String lubm_merged_file = "lubm_merged_file.txt";

        // Here we save the information needed for partitioning in a text file.
        try{

            BufferedWriter bufferedWriter = getBufferedWriter(lubm_merged_file, lubm_merged_neighbors);
            bufferedWriter.close();

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    // Allows us to write data into our text file.
    public static BufferedWriter getBufferedWriter(String file, Map<String, List<String>> node_neighbors)
            throws IOException {

        // We open a file to write the required graph information into it.
        FileOutputStream fileOutputStream = new FileOutputStream(file);

        // Makes sure our characters are encoded as "UTF-8" so we can read them later with
        // Fennel, LDG and Frac_greedy.
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8);

        BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

        // Here we store the number of nodes in the first line of the text file.
        bufferedWriter.write(node_neighbors.size() + "\n");

        // Here, each node now gets its own line in the text file.
        // Each line contains the name of the current node + the names of all of its neighbors.
        for (Map.Entry<String, List<String>> entry : node_neighbors.entrySet()) {
            bufferedWriter.write(entry.getKey() + " ");
            for (String neighbor : entry.getValue()) {
                bufferedWriter.write(neighbor + " ");
            }
            bufferedWriter.write("\n");
        }
        bufferedWriter.flush();
        return bufferedWriter;
    }
}