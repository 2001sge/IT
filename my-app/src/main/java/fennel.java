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

// The graphs are partitioned using the Fennel approach. The Fennel approach can be viewed at
// https://www.microsoft.com/en-us/research/wp-content/uploads/2016/02/MSR-TR-2012-213.pdf
public class fennel {

    // Here we create the function that calculates the partition cost. So the cost of an additional node in a partition.
    static double partition_cost (double gamma, double alpha ,double partition_size){
        double partition_cost;
        partition_cost = (alpha * (Math.pow(partition_size + 1.0, gamma))) -
                (alpha * (Math.pow(partition_size, gamma)));
        return partition_cost;
    }

    // Here we create the function that determines the cost of a partition, based on its size.
    static double size_cost(double gamma, double alpha, double partition_size){
        double size_cost;
        size_cost = alpha * (Math.pow(partition_size, gamma));
        return size_cost;
    }

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
            System.out.print("Please enter whether you want to partition a " +
                    "LUBM graph (1), a synthetic graph (2) or a YAGO graph (3): ");
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

        // Here we record the start time of the actual Fennel program.
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

        // Here we now read in the information from the graph, depending on whether we use a Lubm dataset, a
        // synthetic dataset or a Yago dataset.
        if (type_of_graph == 1 || type_of_graph == 3){
            try{
                List<String> graph_information = Files.readAllLines(get_path);

                // The number of nodes are stored.
                nodes = Integer.parseInt(graph_information.get(0));

                edges++;

                // Store the name of the neighbors of each node in our lubm_yago_neighbors map.
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

        // We set gamma to 1.5 because in the above-mentioned Fennel paper by Tsourakakis, Charalampos, et al.,
        // this value achieves the best performance on a pointwise basis for all generated synthetic graphs.
        // We will use gamma later for the cost and partition-cost-function.
        double gamma = 1.5;

        double nodes_power = Math.pow(nodes, 3.0 / 2.0);
        double square_root_partitions = Math.sqrt(partitions);

        // We also justify the choice of alpha with the arguments of Tsourakakis, Charalampos, et al.
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
        for (int current_node = partitions; current_node < nodes; current_node++) {

            // A variable to later insert the current node into the appropriate partition.
            int chosen_partition = 0;

            int node = 0;
            String node_lubm_yago = "";

            if(type_of_graph == 1 || type_of_graph == 3){
                node_lubm_yago = node_ordering_lubm_yago.get(current_node);
            } else {
                node = node_ordering[current_node];
            }

            // A variable to later update the number of intra edges for the chosen partition.
            int intra_edges = 0;

            // The currently largest difference between inter- and intra-partition costs is initially
            // set to the most negative possible double number.
            double current_biggest_difference = -Double.MAX_VALUE;

            // This loop goes through all partitions to find the appropriate partition for the current node.
            for (int current_partition = 0; current_partition < partitions; current_partition++) {

                // We calculate the intra-partition costs for the current partition.
                double partition_size;
                if (type_of_graph == 1 || type_of_graph == 3){
                    partition_size = partition_nodes_lubm_yago[current_partition].size();
                } else {
                    partition_size = partition_nodes[current_partition].size();
                }

                double intra_partition_cost = partition_cost(gamma, alpha, partition_size);
                int inter_partition_cost = 0;

                // The following loop iterates through all neighbors of the current node
                // and checks whether they are in the currently viewed partition.
                if (type_of_graph == 1 || type_of_graph == 3){
                    for (String neighbor : lubm_yago_neighbors.get(node_lubm_yago)) {
                        if (partition_nodes_lubm_yago[current_partition].contains(neighbor)) {
                            ++inter_partition_cost;
                        }
                    }
                } else {
                    for (int current_neighbor : node_neighbors[node]) {
                        if(partition_nodes[current_partition].contains(current_neighbor)){
                            ++inter_partition_cost;
                        }
                    }
                }

                // Here we consider whether the difference between the current inter-partition cost and the current
                // intra-partition cost is larger than the previous largest difference between these two parameters.
                // If this is the case, the previous largest difference is updated, since a large difference indicates
                // many intra edges for the current node and few existing nodes in the current partition,
                // which is good for load balancing and the fraction of edges cut.
                if(((double)inter_partition_cost) - intra_partition_cost > current_biggest_difference){
                    current_biggest_difference = inter_partition_cost - intra_partition_cost;
                    chosen_partition = current_partition;
                    intra_edges = inter_partition_cost;
                }
            }

            // We insert the current node into the chosen partition and increase the number of intra-edges at
            // the chosen partition.
            if (type_of_graph == 1 || type_of_graph == 3){
                partition_nodes_lubm_yago[chosen_partition].add(node_lubm_yago);
            }else {
                partition_nodes[chosen_partition].add(node);
            }
            intra_partition_edges[chosen_partition] += intra_edges;
        }
        String result_file = "fennel_result_file.txt";

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
                        size_cost(gamma, alpha, partition_nodes_lubm_yago[i].size()));
            } else {
                System.out.println("Costs for partition " + (i+1) + ": " +
                        size_cost(gamma, alpha, partition_nodes[i].size()));
            }
            System.out.println();

            // Here, for each partition, we determine the difference between its number of intra edges
            // and the cost of that partition and add this difference for each partition to determine
            // the result of the maximization function.
            if(type_of_graph == 1 || type_of_graph == 3){
                maximizing_function += intra_partition_edges[i] -
                        size_cost(gamma, alpha, partition_nodes_lubm_yago[i].size());
            } else {
                maximizing_function += intra_partition_edges[i] - size_cost(gamma, alpha, partition_nodes[i].size());
            }

            total_intra_edges += intra_partition_edges[i];

            // We determine the maximum load needed to determine the balance of the graph created
            // by the Fennel algorithm.
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

        // Here we record the stoppage time of the Fennel program.
        Instant algorithm_end = Instant.now();

        // Now we calculate the runtime of the algorithm and output it in milliseconds.
        Duration runtime = Duration.between(algorithm_start, algorithm_end);
        System.out.println("The program takes " + runtime.toMillis() + " milliseconds to run.");
    }
}