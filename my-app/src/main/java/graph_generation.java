import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

// This code is inspired by the work of Dibyadarshan, which is viewable at
// https://github.com/Dibyadarshan/FENNEL-Streaming-Graph-Partitioning

// This code creates a random, undirected graph. The user can enter the number of nodes and edges themselves.
public class graph_generation {
    public static void main(String[] args) {

        // Creating a scanner, so that the user is able to input the desired number of nodes and edges.
        Scanner scanner = new Scanner(System.in);

        // Create two boolean variables to make sure, that the user inputs a valid number of nodes and edges. 
        boolean valid_number_nodes = false;
        boolean valid_number_edges = false;

        int nodes = 0;
        int edges = 0;

        // A while loop to ensure, that a positive number of nodes is entered by the user.
        while(!valid_number_nodes){
            System.out.print("Please enter the number of nodes: ");
            if(scanner.hasNextInt()){
                nodes = scanner.nextInt();
                if((nodes > 0) && (nodes <= 1000000)){
                    valid_number_nodes = true;
                } else {
                    System.out.println("Please enter a positive integer between 1 and 1000000!");
                }
            } else {
                System.out.println("Please enter a valid number of nodes!");
                scanner.next();
            }
        }

        // A while loop to ensure, that a positive number of edges is entered by the user.
        while(!valid_number_edges){
            System.out.print("Please enter the number of edges: ");
            if(scanner.hasNextInt()){
                edges = scanner.nextInt();
                if((edges >= 0) && (edges <= 1000000)){
                    valid_number_edges = true;
                } else {
                    System.out.println("Please enter a integer between 0 and 1000000!");
                }
            } else {
                System.out.println("Please enter a valid number of edges!");
                scanner.next();
            }
        }

        scanner.close();

        // Create an array of sets, so that we can store the neighbors of each node. 
        int neighbors_array_size = nodes + 1;
        Set<Integer>[] node_neighbors = new HashSet[neighbors_array_size];
        for (int i = 0; i < neighbors_array_size; ++i){
            node_neighbors[i] = new HashSet<>();
        }

        // Here we gradually assign each of the edges to two different random nodes, resulting in a random graph.
        for (int i = 1; i <= edges; ++i){
            Random random = new Random();
            int first_node = random.nextInt(nodes - 1 + 1) + 1;
            int second_node = random.nextInt(nodes - 1 + 1) + 1;

            // On the one hand, we avoid double edges in the following, so that there is always a maximum of one edge
            // between two different nodes. On the other hand, we also avoid intrinsic edges, so that a node has no
            // edge to itself.
            if((first_node == second_node) || (node_neighbors[first_node].contains(second_node))){
                --i;
                continue;
            }

            // If a valid edge is found, it is added here by declaring the two associated nodes as neighbors in the
            // node_neighbors array of sets.
            node_neighbors[first_node].add(second_node);
            node_neighbors[second_node].add(first_node);
        }

        String synth_graph_file = "synth_graph_file.txt";

        // Below we will create a text file in which we will save the graph so that we can
        // continue working with it later.
        try {

            // Allows us to write data into our text file.
            FileOutputStream fileOutputStream = new FileOutputStream(synth_graph_file);

            // Makes sure our characters are encoded as "UTF-8" so we can read them later with
            // Fennel, LDG and Frac_greedy.
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8);

            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

            // In the top line of our text file we write down the number of nodes.
            bufferedWriter.write(nodes + "\n");

            // Here we save the number of neighbors of each node and also the indices of the neighbors of each node
            // in the text file.
            for(int i = 1; i <= nodes; ++i){
                bufferedWriter.write(node_neighbors[i].size() + " ");
                for(int it : node_neighbors[i]  ){
                    bufferedWriter.write(it + " ");
                }
                bufferedWriter.write("\n");
            }
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}