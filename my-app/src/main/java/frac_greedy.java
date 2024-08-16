import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

// This code is inspired by the work of Dibyadarshan, which is viewable at
// https://github.com/Dibyadarshan/FENNEL-Streaming-Graph-Partitioning

// This code partitions: 1. random graphs created using graph_generation.java.
//                       2. graphs from a Lubm-dataset.
//                       3. graphs from a Yago-dataset.

// The graphs are partitioned using the Fractional Greedy heuristic.
// The Fractional Greedy heuristic can be viewed at https://hal.science/hal-01282071/document
public class frac_greedy {
    public static void main(String[] args) {

        // Creating a scanner, so that the user is able to input the desired number of partitions and the type of graph.
        Scanner scanner = new Scanner(System.in);

        boolean valid_type_of_graph = false;

        // Create a boolean variable to make sure, that the user inputs a valid number of partitions.
        boolean valid_number_partitions = false;

        // Creating variables in order to store the amount of nodes, edges, partitions and the type of graph later.
        int type_of_graph = 0;
        int nodes = 0;
        int edges = 0;
        int partitions = 0;

        // A while loop to ensure, that a correct type of graph is entered by the user.
        while(!valid_type_of_graph){
            System.out.print("Please enter whether you want to partition " +
                    "a LUBM graph (1), a synthetic graph (2) or a YAGO graph (3): ");
            if (scanner.hasNextInt()) {
                type_of_graph = scanner.nextInt();
                if (type_of_graph == 1 || type_of_graph == 2 || type_of_graph == 3) {
                    valid_type_of_graph = true;
                } else {
                    System.out.println("Please enter an integer between 1 and 3!");
                }
            } else {
                System.out.println("Please enter a valid number!");
                scanner.next();
            }
        }

        // A while loop to ensure, that a positive number of partitions is entered by the user.
        while(!valid_number_partitions){
            System.out.print("Please enter the number of partitions: ");
            if(scanner.hasNextInt()){
                partitions = scanner.nextInt();
                if(partitions > 0){
                    valid_number_partitions = true;
                } else {
                    System.out.println("Please enter a positive integer!");
                }
            } else {
                System.out.println("Please enter a valid number of partitions!");
                scanner.next();
            }
        }

        scanner.close();

        // Here we record the start time of the actual Fractional Greedy heuristic program.
        Instant algorithm_start = Instant.now();

        int partitions_array_size = partitions + 1;

        // Create an array of sets, so that we can store the nodes of each partition, if we use a synthetic dataset.
        Set<Integer>[] partition_nodes = new HashSet[partitions_array_size];
        if(type_of_graph == 2){
            for (int i = 0; i < partitions_array_size; ++i){
                partition_nodes[i] = new HashSet<>();
            }
        }

        // Create an array of sets, so that we can store the nodes of each partition, if we use a Lubm/Yago dataset.
        Set<String>[] partition_nodes_lubm_yago = new HashSet[partitions_array_size];
        if(type_of_graph == 1 || type_of_graph == 3){
            for (int i = 0; i < partitions_array_size; ++i){
                partition_nodes_lubm_yago[i] = new HashSet<>();
            }
        }
        String path;

        // Read in the path to the graph.
        // Change lubm_merged_file to lubm_data_file, if using a .nq Lubm file.
        // Change yago_ntx_gz_file to yago_ttl_file, if using a .ttl Yago file.
        if(type_of_graph == 1){
            path = "C:/Users/kazem/OneDrive/Dokumente/Bachelorarbeit/bachelor/lubm_merged_file.txt";
        } else if (type_of_graph == 2) {
            path = "C:/Users/kazem/OneDrive/Dokumente/Bachelorarbeit/bachelor/synth_graph_file.txt";
        } else {
            path = "C:/Users/kazem/OneDrive/Dokumente/Bachelorarbeit/bachelor/yago_ntx_gz_file.txt";
        }

        // Create a map in which we will store the neighbors of each Lubm/Yago dataset node.
        Map<String, List<String>> lubm_yago_neighbors = new HashMap<>();

        // Create an array of sets, so that we can store the neighbors of each node later.
        Set<Integer>[] node_neighbors = null;

        Path get_path = Paths.get(path);

        // Here we now read in the information from the graph, depending on whether we use a Lubm dataset a
        // synthetic dataset or a Yago dataset.
        if (type_of_graph == 1 || type_of_graph == 3){
            try{
                List<String> graph_information = Files.readAllLines(get_path);

                // The number of nodes are stored.
                nodes = Integer.parseInt(graph_information.get(0));

                edges++;

                // Store the name of the neighbors of each node in our lubm_neighbors map.
                for (int i = 1; i < nodes; i++) {
                    String[] current_names = graph_information.get(i).split(" ");
                    String firstNode = current_names[0];
                    // Add all the neighbors into to the neighbors set of the current node and vice versa.
                    for (int j = 1; j < current_names.length; j++) {
                        String secondNode = current_names[j];
                        edges++;
                        YAGO_ntx_gz_Reader.addNeighbor(lubm_yago_neighbors, firstNode, secondNode);
                        YAGO_ntx_gz_Reader.addNeighbor(lubm_yago_neighbors, secondNode, firstNode);
                    }
                }
            } catch (IOException | NumberFormatException e) {
                e.printStackTrace();
            }
        } else {
            try{
                List<String> graph_information = Files.readAllLines(get_path);

                // The number of nodes are stored.
                nodes = Integer.parseInt(graph_information.get(0));

                // Declare the size of our array of sets, so that we can store the neighbors of each node later.
                int neighbors_array_size = nodes + 1;
                node_neighbors = new HashSet[neighbors_array_size];
                for (int i = 0; i < neighbors_array_size; ++i){
                    node_neighbors[i] = new HashSet<>();
                }

                // We create a variable to store the amount of neighbors for each node, in order to store the
                // total amount of edges.
                int amount_neighbors;

                // Store the indices of the neighbors of each node in our array of sets node_neighbors.
                for(int i = 1; i <= nodes; i++){

                    // Read in the information for the current node.
                    String line = graph_information.get(i);
                    String[] numbers = line.split(" ");

                    // The following command reads the number of neighbors for the current node.
                    amount_neighbors = Integer.parseInt(numbers[0]);

                    // Update the total amount of edges.
                    edges += amount_neighbors;

                    // Here we insert the neighbors of the current node into our array of sets node_neighbors.
                    for (int j = 1; j <= amount_neighbors; j++) {
                        node_neighbors[i].add(Integer.parseInt(numbers[j]));
                    }
                }
            } catch (IOException | NumberFormatException e) {
                e.printStackTrace();
            }
        }

        // We still have to halve the value of edges here, since each edge should only be counted once and not twice.
        edges = edges / 2;

        double gamma = 1.5;
        double nodes_power = Math.pow(nodes, 3.0 / 2.0);
        double square_root_partitions = Math.sqrt(partitions);
        double alpha = square_root_partitions * (edges / nodes_power);

        // The following array will later contain for each partition the number of edges that have both ends
        // in that partition.
        int[] intra_partition_edges = new int[partitions];
        for (int i = 0; i < partitions; i++) {
            intra_partition_edges[i] = 0;
        }

        // The following array will contain all nodes and arrange them in a random order later
        // because we have chosen the random ordering as the stream order.
        int[] node_ordering = new int[nodes];
        if(type_of_graph == 2){
            for (int i = 0; i < nodes; i++) {
                node_ordering[i] =i + 1;
            }
        }

        // The following List will contain all nodes from the Lubm/Yago dataset we currently use.
        List<String> node_ordering_lubm_yago = new ArrayList<>(nodes);
        if(type_of_graph == 1 || type_of_graph == 3){
            for (Map.Entry<String, List<String>> entry : lubm_yago_neighbors.entrySet()) {
                node_ordering_lubm_yago.add(entry.getKey());
            }
        }

        // Shuffle our Lubm/Yago-dataset nodes.
        if(type_of_graph == 1 || type_of_graph == 3){
            Collections.shuffle(node_ordering_lubm_yago);
        }

        // To shuffle our node_ordering array, we use the so-called Fisher-Yates shuffle.
        Random random = new Random();
        if (type_of_graph == 2){
            for (int i = nodes - 1; i > 0; i--) {
                int random_index = random.nextInt(i + 1);
                int random_node = node_ordering[random_index];
                node_ordering[random_index] = node_ordering[i];
                node_ordering[i] = random_node;
            }
        }

        // We insert one node into each of our k partitions, i.e. the first k nodes from our node_ordering array.
        if(type_of_graph == 1 || type_of_graph == 3){
            for (int i = 0; i < partitions; i++) {
                partition_nodes_lubm_yago[i].add(node_ordering_lubm_yago.get(i));
            }
        } else {
            for (int i = 0; i < partitions; i++) {
                partition_nodes[i].add(node_ordering[i]);
            }
        }

        // In the following for loop, the assignment of the remaining nodes into the partitions begins.
        for (int current_node = partitions ; current_node < nodes; current_node++) {

            int node = 0;
            String node_lubm_yago = "";

            if(type_of_graph == 1 || type_of_graph == 3){
                node_lubm_yago = node_ordering_lubm_yago.get(current_node);
            } else {
                node = node_ordering[current_node];
            }

            double current_largest_difference = -Double.MAX_VALUE;
            int smallest_size = Integer.MAX_VALUE;
            List<Double> difference_list = new ArrayList<>();
            List<Integer> partition_indices = new ArrayList<>();
            List<Integer> min_size = new ArrayList<>();

            // This loop goes through all partitions to find the appropriate partition for the current node.
            for (int current_partition = 0; current_partition < partitions; current_partition++) {

                // We determine the size of the current partition.
                double partition_size;
                if (type_of_graph == 1 || type_of_graph == 3){
                    partition_size = partition_nodes_lubm_yago[current_partition].size();
                } else {
                    partition_size = partition_nodes[current_partition].size();
                }

                int current_intra_edges = 0;

                // The following loop iterates through all neighbors of the current node
                // and checks whether they are in the currently viewed partition.
                if (type_of_graph == 1 || type_of_graph == 3){
                    for (String neighbor : lubm_yago_neighbors.get(node_lubm_yago)) {
                        if (partition_nodes_lubm_yago[current_partition].contains(neighbor)) {
                            ++current_intra_edges;
                        }
                    }
                } else {
                    for (int current_neighbor : node_neighbors[node]) {
                        if(partition_nodes[current_partition].contains(current_neighbor)){
                            ++current_intra_edges;
                        }
                    }
                }

                // Here the calculation of the difference for the current partition takes part.
                double difference = (current_intra_edges) - ( 1 / ( 1 - ( (partition_size) /
                        ( (double) (nodes) / (partitions) ))));

                // This if-condition ensures that partitions that have already reached the maximum size of
                // nodes divided by partitions cannot accommodate any more nodes.
                if((partition_size)/((double) (nodes)/(partitions)) >= 1){
                    difference = Double.NEGATIVE_INFINITY;
                }

                // We update the current largest difference here if the current difference is even larger.
                if(difference > current_largest_difference){
                    current_largest_difference = difference;
                }

                // We add the current difference into a list, in which we store
                // all the current differences for all partitions.
                difference_list.add(difference);
            }
            int new_current_intra_edges = 0;

            // We want the indices of those partitions, whose differences are the largest difference over
            // all partitons (Can be more than 1 partition).
            for (int current_partition = 0; current_partition < partitions; current_partition++){
                if(difference_list.get(current_partition) == current_largest_difference){
                    partition_indices.add(current_partition);
                }
            }

            // We determine the smallest size of a partition from the partitons that have the largest difference.
            for (int current_index : partition_indices) {
                int current_size;
                if (type_of_graph == 1 || type_of_graph == 3) {
                    current_size = partition_nodes_lubm_yago[current_index].size();
                } else {
                    current_size = partition_nodes[current_index].size();
                }
                if (current_size < smallest_size) {
                    smallest_size = current_size;
                }
            }

            // We store all the indices of those partitions which have the smallest size and at the same time
            // the largest difference.
            for (int minimum_index : partition_indices) {
                int minimum_size;
                if (type_of_graph == 1 || type_of_graph == 3) {
                    minimum_size = partition_nodes_lubm_yago[minimum_index].size();
                } else {
                    minimum_size = partition_nodes[minimum_index].size();
                }
                if (minimum_size == smallest_size) {
                    min_size.add(minimum_index);
                }
            }

            // Out of those partitions we got, we now choose a random one to put our current node into.
            Random random_index = new Random();
            int chosen_random_index = random_index.nextInt(min_size.size());
            int random_partition = min_size.get(chosen_random_index);

            // We determine the number of neighbors of the current node that have already been assigned
            // into the chosen partition.
            if (type_of_graph == 1 || type_of_graph == 3){
                for (String neighbor : lubm_yago_neighbors.get(node_lubm_yago)) {
                    if (partition_nodes_lubm_yago[random_partition].contains(neighbor)) {
                        ++new_current_intra_edges;
                    }
                }
            } else {
                for (int current_neighbor : node_neighbors[node]) {
                    if(partition_nodes[random_partition].contains(current_neighbor)){
                        ++new_current_intra_edges;
                    }
                }
            }

            // We insert the current node into the chosen partition and increase the number of intra-edges at
            // the chosen partition.
            if (type_of_graph == 1 || type_of_graph == 3){
                partition_nodes_lubm_yago[random_partition].add(node_lubm_yago);
            }else {
                partition_nodes[random_partition].add(node);
            }
            intra_partition_edges[random_partition] += new_current_intra_edges;
        }
        String result_file = "frac_greedy_result_file.txt";

        // Here we create a text file in which we save our partitions with their assigned nodes.
        // We also output the partitions in the terminal.
        try {

            // Allows us to write data into our text file.
            FileOutputStream fileOutputStream = new FileOutputStream(result_file);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8);
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

            for (int i = 0; i < partitions; i++) {
                bufferedWriter.write("Partition " + (i + 1) + ": ");
                System.out.print("Partition " + (i + 1) + ": ");
                if (type_of_graph == 1 || type_of_graph == 3){
                    for (String j : partition_nodes_lubm_yago[i]){
                        bufferedWriter.write(j + " ");
                        System.out.print(j + " ");
                    }
                } else {
                    for (int j : partition_nodes[i]) {
                        bufferedWriter.write(j + " ");
                        System.out.print(j + " ");
                    }
                }
                bufferedWriter.write("\n\n");
                System.out.println();
                System.out.println();
            }
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        double maximizing_function = 0;
        int total_edges_cut;
        int total_intra_edges = 0;
        int maximum_load = 0;
        double normalized_maximum_load;

        for (int i = 0; i < partitions; i++) {

            // We output the number of intra edges and the cost of every partition.
            System.out.println("Number of intra edges in partition " + (i+1) + ": " + intra_partition_edges[i]);
            if (type_of_graph == 1 || type_of_graph == 3){
                System.out.println("Costs for partition " + (i+1) + ": " +
                        fennel.size_cost(gamma, alpha, partition_nodes_lubm_yago[i].size()));
            } else {
                System.out.println("Costs for partition " + (i+1) + ": " +
                        fennel.size_cost(gamma, alpha, partition_nodes[i].size()));
            }
            System.out.println();

            // Here, for each partition, we determine the difference between its number of intra edges and
            // the cost of that partition and add this difference for each partition to determine
            // the result of the maximization function.
            if(type_of_graph == 1 || type_of_graph == 3){
                maximizing_function += intra_partition_edges[i] -
                        fennel.size_cost(gamma, alpha, partition_nodes_lubm_yago[i].size());
            } else {
                maximizing_function += intra_partition_edges[i] -
                        fennel.size_cost(gamma, alpha, partition_nodes[i].size());
            }

            total_intra_edges += intra_partition_edges[i];

            // We determine the maximum load needed to determine the balance of the graph created by the
            // Fractional Greedy heuristic.
            if(type_of_graph == 1 || type_of_graph == 3){
                if (partition_nodes_lubm_yago[i].size() > maximum_load) {
                    maximum_load = partition_nodes_lubm_yago[i].size();
                }
            } else {
                if (partition_nodes[i].size() > maximum_load) {
                    maximum_load = partition_nodes[i].size();
                }
            }
        }

        // To determine the fraction of edges cut, we first need to determine the total number of edges cut
        // by subtracting the total number of intra edges from the total number of edges.
        total_edges_cut = edges - total_intra_edges;

        // We now determine the fraction of edges cut and output them in the terminal.
        double fraction_edges_cut = (((double)total_edges_cut) / ((double)edges)) * 100;
        System.out.println("The fraction of edges cut: " + fraction_edges_cut + "%");

        // Here we determine the normalized maximum load and output it.
        normalized_maximum_load = ((double)maximum_load) / (((double)nodes) / ((double)partitions));
        System.out.println("The normalized maximum load p: " + normalized_maximum_load);

        // Here we output the result of the maximization function.
        System.out.println("The result of the maximization function is: " + maximizing_function);

        // Here we record the stoppage time of the Fractional Greedy heuristic program.
        Instant algorithm_end = Instant.now();

        // Now we calculate the runtime of the algorithm and output it in milliseconds.
        Duration runtime = Duration.between(algorithm_start, algorithm_end);
        System.out.println("The program takes " + runtime.toMillis() + " milliseconds to run.");
    }
}