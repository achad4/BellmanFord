Avi Chad-Friedman
ajc2212
PA2

Instructions:
1) Run "make" from within the src directory to compile the code
2) Run "java BFClient <config>" to start a host

Commands:
1) "SHOWRT"
--Display the current nodes distance vector

2) "LINKDOWN <ip> <port number>"
--Set the cost of neighboring node, specified by <ip> and <port number>
to infinity.  LINKDOWN fails if there is no neighbor at that destination.

3) "LINKUP <ip> <port number>"
--Restore a link to it's original value (one that has been broken
with the LINKDOWN command).  LINKUP fails if there is no neighbor at that destination.
NOTE: This command CANNOT be called on a timed out node.

4) "CHANGECOST <ip> <port number>"
--Change the cost of a link to a neighboring node.  CHANGECOST fails if there is no neighbor
at that destination.

5) "TRANSFER <file name> <ip> <port number>"
--Transfers the file <file name> to the destination specified by <ip> and <port number>, using
the current routing table.  The destination node indicated when it has successfully recieved the
file.

6) "CLOSE"
--Terminated BFClient process.

Design:
1) Timer
--I maintain a separate thread to update the distance vector, send updates, and check for
timed out nodes.  This thread is active every timeout seconds. A separate thread receives all incoming
information and handles it according to protocol.  When a node receives a distance vector from its neighbor.
it checks whether it has been contacted by that neighbor before, and, if it has, updates its most
recently active time.  If it hasn't seen the neighbor, it adds it to its array of neighbor.  Before running the
Bellman Ford algorithm, it checks whether a neighbor (in the config file) has not been heard from
in 3*timeout seconds from the start of the program.  This ensures that a node will not send updates to a
neighbor that never actually started running.  When iterating through all of the distance vectors its received,
the node checks whether the difference between the current date and the date that vector was received is greater
than 3*timeout.

2) Distance Vectors
--All the distance vector information is encapsulated in my DistanceVector class.  This contains an "owner"
node, and a hash map of Nodes to pairs of Nodes and Costs.  This is to represent the destination, next hop,
and cost respectively.  Cost, however, is a class that wraps around the double "weight".  I did this
so that I could set a flag to determine if the cost is infinite or not.  This way, I can destroy
and restore links without maintaining additional information about the old cost of the link.

Test cases:
I have included 5 config files that I used to test my code.
To replicate my tests on CLIC, run the following commands on the specified machines

tokyo: "java BFClient config1.txt"
paris: "java BFClient config2.txt"
vienna: "java BFClient config3.txt"
delhu: "java BFClient config4.txt"
tokyo: "java BFClient config5.txt"




