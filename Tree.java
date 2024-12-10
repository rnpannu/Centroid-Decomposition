import java.util.*;

/*
    Class that implements an undirected tree with unweighted vertices
    and edge lengths
 */
public class Tree<V,E> extends Object {

    private ArrayList<Edge> edges;

    private ArrayList<Node> nodes;


    public Tree() {
        edges = new ArrayList<Edge>();
        nodes = new ArrayList<Node>();
    }



    // Return all the nodes in the tree
    public ArrayList<Node> getNodes() {
        return nodes;
    }

    public ArrayList<Edge> getEdges() {
        return edges;
    }

    public int numNodes() {
        return nodes.size();
    }

    public int numEdges() {
        return edges.size();
    }

    public Node addNode(V data) {
        Node newNode = new Node(data);
        nodes.add(newNode);
        return newNode;
    }
    // If the nodes don't already share an edge
    public Edge addEdge(E data, Node n1, Node n2) {
        if (!n1.isNeighbor(n2)) {
            Edge newEdge = new Edge(data, n1, n2);
            edges.add(newEdge);
            return newEdge;
        } else {
            return null;
        }
    }

    public void removeEdge(Edge edge) {
        edges.remove(edge);
    }
    public void removeNode(Node node) {
        // Create a copy of the edges to avoid concurrent modification
        ArrayList<Edge> edgesToRemove = new ArrayList<>(node.getEdges());

        // Remove all edges connected to this node
        for (Edge edge : edgesToRemove) {
            removeEdge(edge);
        }

        nodes.remove(node);

    }


    public boolean isAcyclic() {
        if (numNodes() <= 1) {
            return true;
        }

        // Tracker for visited nodes
        ArrayList<Node> visited = new ArrayList<Node>();

        // Iterate through all nodes to handle disconnected graphs
        for (Node node : getNodes()) {
            // If the node hasn't been visited, run DFS
            if (!visited.contains(node)) {
                if (hasCycle(node, null, visited)) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean hasCycle(Node current, Node parent, ArrayList<Node> visited) {
        visited.add(current);

        for (Edge edge : current.getEdges()) {
            Node neighbor = edge.getOtherNode(current);

            // Skip the parent node
            if (neighbor == parent) {
                continue;
            }

            // If the neighbor has been visited and is not the parent -> cycle
            if (visited.contains(neighbor)) {
                return true;
            }

            // Recursively explore
            if (hasCycle(neighbor, current, visited)) {
                return true;
            }
        }

        return false;
    }
    // Nested edge class that uses EdgeData
    public class Edge {

        private E data;
        public Node node1;
        private Node node2;

        public Edge(E data, Node node1, Node node2) {
            this.data = data;
            this.node1 = node1;
            this.node2 = node2;
            node1.addEdge(this);
            node2.addEdge(this);
        }
        public Node getNode(){
            return node1;
        }
        public Node getOtherNode(Node node) {
            return (node == node1) ? node2 : node1;
        }

        public boolean equals(Edge edge){
            boolean result = false;
            if (node2 == edge.getOtherNode(node1)){
                result = true;
            }
            return result;
        }

        public E getData(){
            return data;
        }
        public void setData(E data){
            this.data = data;
        }

    }

    // Nested Node class that uses NodeData
    public class Node {

        private V data;
        private ArrayList<Edge> edges;

        public Node(V data){
            this.data = data;
            edges = new ArrayList<Edge>();
        }

        public void addEdge(Edge edge) {
            edges.add(edge);
        }
        public V getData(){
            return data;
        }

        public Edge edgeTo(Node neighbor) {
            for (Edge edge : edges) {
                if (edge.getOtherNode(this) == neighbor) {
                    return edge;
                }
            }
            return null;
        }

        public void setData(V data){ this.data = data; }

        public ArrayList<Edge> getEdges() {
            return edges;
        }

        public void removeEdge(Edge edge){
            if (edges.contains(edge)){
                edges.remove(edge);
            }
        }

        public boolean isNeighbor(Node neighbor){
            for (Edge edge:edges){
                for (Edge neighborEdge : neighbor.getEdges()){
                    if (edge.equals(neighborEdge)){
                        return true;
                    }
                }
            }
            return false;
        }

        public ArrayList<Node> getNeighbors() {
            ArrayList<Node> neighbors = new ArrayList<Node>();
            for (Edge edge : edges) {
                neighbors.add(edge.getOtherNode(this));
            }
            return neighbors;
        }

    }
}