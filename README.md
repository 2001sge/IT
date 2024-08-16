Depending on whether you want to partition a Yago.ttl, a Yago.ntx.gz, a Lubm_data.nq-0 or a Lubm_merged.nq file, run the
appropriate reader class. To do this, you must first update the path and specify the path to the appropriate file in
your directory. 

If you want to create a synthetic graph to partition it, use the graph_generation class. Run it and then enter the
desired number of nodes and edges.

You will, for all five classes, receive a text file containing the graph structure that can now be partitioned.

Now go to the class of the algorithm with which you want to partition the graph. The Fennel algorithm, the LDG algorithm
and the Fractional Greedy algorithm are available for this purpose.

To do this, first go to line 114 for the Fennel class and to line 99 for the LDG and Fractional Greedy classes to
specify the path to the created text file.

Use the if-branch to enter the path of the created Lubm text files, the if-else-branch to enter the path of the
text file of a synthetic graph and the else-branch to enter the path of the created Yago text files.

Now let the algorithm run and enter the desired number of partitions.

As output, you will receive a text file containing all partitions with their associated nodes. In addition, you will
receive information in the terminal about the number of intra-edges per partition, costs per partition,
the fraction of edges cut, the normalized maximum load, the result of the maximization function g(P)
and the running time.