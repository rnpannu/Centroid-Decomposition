import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;


 //Implements and displays a graphical canvas.
public class Canvas extends JComponent {

    public Tree<NodeData,EdgeData> tree;


    public Canvas() {
        tree = new Tree<NodeData, EdgeData>(); // Working tree, seperate from centroid decomp
    }

    // Paints default blue circle with r = 15px and default blue edge lines
    public void paintComponent(Graphics g){
        if (tree != null) {
            // Paint edges
            for (Tree<NodeData, EdgeData>.Edge edge : tree.getEdges()) {
                g.setColor(edge.getData().getColor());
                Tree<NodeData, EdgeData>.Node sigma = edge.getNode();
                Point s = sigma.getData().getPosition();
                Point r = edge.getOtherNode(sigma).getData().getPosition();
                // Paint edge from s to r
                double dist = paintArrowLine(g, (int)s.getX(), (int)s.getY(), (int)r.getX(), (int)r.getY(), 20, 10, edge.getData().getDistance());
                if (edge.getData().getDistance()==-1) {
                    edge.getData().setDistance(dist);
                }
            }
            // Paint nodes
            for (Tree<NodeData,EdgeData>.Node node : tree.getNodes()) {
                Point q = node.getData().getPosition();
                g.setColor(node.getData().getColor());
                g.fillOval((int) q.getX()-40, (int) q.getY()-40, 80, 80);
                //paint text
                g.setColor(Color.white);
                g.drawString(node.getData().getText(),(int)q.getX()-5,(int)q.getY()+5);
            }
        }

    }
    // Reset to default colours
     private void resetColors() {
         if (tree != null) {
             for (Tree<NodeData,EdgeData>.Edge edge : tree.getEdges()) {
                 edge.getData().setColor(new Color(8, 83, 109)); // Default edge color
             }
             for (Tree<NodeData,EdgeData>.Node node : tree.getNodes()) {
                 node.getData().setColor(new Color(8, 83, 109)); // Default node color
             }
         }
     }

    /**
     * Draw an arrow line between two points, revised from the code of @phibao37
     * http://stackoverflow.com/questions/2027613/how-to-draw-a-directed-arrow-line-in-java
     * Takes in x and y positions of the 2 points, as well as the width and height components
     * of the arrow/vector. Returns the distance between two points.
     */
    private double paintArrowLine(Graphics g, int x1, int y1, int x2, int y2, int d, int h,Double dist){
        int dx = x2 - x1, dy = y2 - y1;
        double D = Math.sqrt(dx*dx + dy*dy);
        double sin = dy/D, cos = dx/D;

        x2 = (int)(x2 - 40 * cos);
        y2 = (int)(y2 - 40 * sin);
        D = D - 40; // change the length of the line
        double xm = D - d, xn = xm, ym = h, yn = -h, x;

        x = xm*cos - ym*sin + x1;
        ym = xm*sin + ym*cos + y1;
        xm = x;

        x = xn*cos - yn*sin + x1;
        yn = xn*sin + yn*cos + y1;
        xn = x;

        int[] xpoints = {x2, (int) xm, (int) xn};
        int[] ypoints = {y2, (int) ym, (int) yn};

        g.drawLine(x1, y1, x2, y2);
        g.fillPolygon(xpoints, ypoints, 3);
        // paint the distance
        if (dist == -1.0) {
            // the actual distance(D+40) between two points next to the tail (D=the length of the arrow)
            dist = Math.round((D + 40) * 100) / 100.00;
        }
        g.drawString(Double.toString(dist),(int) (xm - 30 * cos), (int) (ym - 30 * sin));
        return dist;
    }

    public void startCentroidDecomposition() {
        if (tree != null && !tree.getNodes().isEmpty()) {
            System.out.println("Starting centroid decomposition.");
            new DecompositionWorker(tree).execute();
        } else {
            System.out.println("Tree is empty.");
            //instr.setText("Tree is empty or invalid.");
        }
    }
    // Swing worker class that handles the GUI processes.
    public class DecompositionWorker extends SwingWorker<Void, Tree<NodeData, EdgeData>.Node> {
        private final Tree<NodeData, EdgeData> decompositionTree;
        public DecompositionWorker(Tree<NodeData, EdgeData> tree) {
            this.decompositionTree = getCentroidDecomposition();
        }

        public Tree<NodeData,EdgeData> getCentroidDecomposition() {
            // Create a new tree to represent the centroid decomposition
            Tree<NodeData,EdgeData> centroidTree = new Tree<NodeData,EdgeData>();

            // If the tree is empty, return an empty centroid tree
            if (tree.getNodes().isEmpty()) {
                return centroidTree;
            }

            // Mark which nodes have been used as centroids
            ArrayList<Tree<NodeData,EdgeData>.Node> centroidMarked = new ArrayList<>();

            // Map to keep track of original nodes to centroid nodes
            Map<Tree<NodeData,EdgeData>.Node, Tree<NodeData,EdgeData>.Node> centroidMapping = new HashMap<>();


            // Start centroid decomposition from the first node
            getCentroidRecursive(tree.getNodes().get(0), centroidTree, centroidMarked, centroidMapping);

            return centroidTree;
        }
        private Tree<NodeData,EdgeData>.Node getCentroidRecursive(Tree<NodeData,EdgeData>.Node currentNode, Tree<NodeData,EdgeData> centroidTree,
                                                                  ArrayList<Tree<NodeData,EdgeData>.Node> centroidMarked,
                                                                  Map<Tree<NodeData,EdgeData>.Node, Tree<NodeData,EdgeData>.Node> centroidMapping){
            // Find the centroid of the current subtree
            System.out.println("\nProspective node: " + currentNode.getData().getText());


            Tree<NodeData,EdgeData>.Node centroid = findCentroid(currentNode, centroidMarked);
            // Mark this node as a centroid
            centroidMarked.add(centroid);

            // Create a centroid node in the new tree

            Tree<NodeData,EdgeData>.Node centroidTreeNode = centroidTree.addNode(centroid.getData());
            centroidMapping.put(centroid, centroidTreeNode);

            // Find the connected components after removing the centroid
            List<Tree<NodeData,EdgeData>.Node> components = findComponents(centroid, centroidMarked);

            // Recursively decompose each component
            for (Tree.Node component : components) {
                if (!centroidMarked.contains(component)) {
                    Tree.Node componentCentroid = getCentroidRecursive(component, centroidTree,
                            centroidMarked, centroidMapping);

                    // Add an edge between the current centroid and the component's centroid
                    if (componentCentroid != null) {
                        centroidTree.addEdge((new EdgeData(1.0)),
                                centroidMapping.get(centroid),
                                centroidMapping.get(componentCentroid));
                    }
                }
            }

            return centroid;
        }

        private Tree<NodeData,EdgeData>.Node findCentroid(Tree<NodeData,EdgeData>.Node root, ArrayList<Tree<NodeData,EdgeData>.Node> centroidMarked) {
            int[] totalSize = {0};
            Map<Tree<NodeData,EdgeData>.Node, Integer> subtreeSize = new HashMap<>();

            // First DFS to calculate subtree sizes
            System.out.print("Subtree rooted at: " + root.getData().getText() + ".\n" + "Size of tree: " + totalSize[0] + ", ");
            calculateSubtreeSizes(root, null, subtreeSize, totalSize, centroidMarked);

            // Second DFS to find the centroid
            return findCentroidHelper(root, null, subtreeSize, totalSize[0], centroidMarked);
        }

        private void calculateSubtreeSizes(Tree<NodeData,EdgeData>.Node current, Tree<NodeData,EdgeData>.Node parent,
                                           Map<Tree<NodeData,EdgeData>.Node, Integer> subtreeSize,
                                           int[] totalSize,
                                           ArrayList<Tree<NodeData,EdgeData>.Node> centroidMarked) {
            if (centroidMarked.contains(current)) return;


            totalSize[0]++;
            subtreeSize.put(current, 1);
            System.out.print(totalSize[0] + ", ");
            for (Tree<NodeData,EdgeData>.Node neighbor : current.getNeighbors()) {
                if (neighbor != parent && !centroidMarked.contains(neighbor)) {

                    calculateSubtreeSizes(neighbor, current, subtreeSize, totalSize, centroidMarked);
                    subtreeSize.put(current, subtreeSize.get(current) + subtreeSize.get(neighbor));
                }
            }
        }

        private Tree<NodeData,EdgeData>.Node findCentroidHelper(Tree<NodeData,EdgeData>.Node current, Tree<NodeData,EdgeData>.Node parent,
                                                                Map<Tree<NodeData,EdgeData>.Node, Integer> subtreeSize,
                                                                int totalSize,
                                                                ArrayList<Tree<NodeData,EdgeData>.Node> centroidMarked) {
            boolean isCentroid = true;
            Tree<NodeData,EdgeData>.Node heaviestChild = null;
            int maxChildSize = 0;

            for (Tree.Node neighbor : current.getNeighbors()) {
                if (neighbor != parent && !centroidMarked.contains(neighbor)) {
                    // Check if any subtree is too large
                    if (subtreeSize.get(neighbor) > totalSize / 2) {
                        isCentroid = false;
                    }

                    // Find the heaviest child
                    if (heaviestChild == null || subtreeSize.get(neighbor) > maxChildSize) {
                        heaviestChild = neighbor;
                        maxChildSize = subtreeSize.get(neighbor);
                    }
                }
            }
            // Check if current node is a valid centroid
            int remainingSize = totalSize - subtreeSize.get(current);
            if (isCentroid && remainingSize <= totalSize / 2) {
                return current;
            }

            // If not, recursively find centroid in the heaviest child
            return findCentroidHelper(heaviestChild, current, subtreeSize, totalSize, centroidMarked);
        }

        private List<Tree<NodeData,EdgeData>.Node> findComponents(Tree<NodeData,EdgeData>.Node centroid, ArrayList<Tree<NodeData,EdgeData>.Node> centroidMarked) {
            List<Tree<NodeData,EdgeData>.Node> components = new ArrayList<>();
            System.out.print("\nComponent rooted at centroid: " + centroid.getData().getText() + "\nComponent Node: ");
            for (Tree<NodeData,EdgeData>.Node neighbor : centroid.getNeighbors()) {
                if (!centroidMarked.contains(neighbor)) {
                    System.out.print(neighbor.getData().getText() + ", ");
                    components.add(neighbor);
                }
            }
            System.out.print("\nMarked centroids: ");
            for (Tree<NodeData,EdgeData>.Node node : centroidMarked) {
                System.out.print(node.getData().getText() + ", ");
            }
            return components;
        }

        protected Void doInBackground() {
            // Reset all nodes and edges to default color before starting
            resetColors();
//            for (Tree<NodeData, EdgeData>.Node node : tree.getNodes()) {
//                publish(node);
//                try {
//                    Thread.sleep(500); // Longer pause for better visualization
//                } catch (InterruptedException ignored) {}
//            }
            // Iterate through centroids for visualization
            for (Tree<NodeData, EdgeData>.Node centroid : decompositionTree.getNodes()) {
                publish(centroid);
                try {
                    Thread.sleep(2000); // Longer pause for better visualization
                } catch (InterruptedException ignored) {}
            }
            return null;
        }

        protected void process(List<Tree<NodeData, EdgeData>.Node> centroids) {
            // Get the last centroid to process
            Tree<NodeData, EdgeData>.Node lastCentroid = centroids.get(centroids.size() - 1);

            // Visualization stages
            highlightPreprocessing(lastCentroid);
            repaint();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {}

            highlightCentroid(lastCentroid);
            repaint();
        }

        @Override
        protected void done() {
            System.out.println("Centroid Decomposition Complete");
            // Optional: Reset colors to default after completion
            resetColors();
            repaint();
        }

        // Enhanced node highlighting methods
        private void highlightPreprocessing(Tree<NodeData, EdgeData>.Node node) {
            // Reset all colors first
            resetColors();

            // Highlight the current subtree in a lighter color
            for (Tree<NodeData, EdgeData>.Node neighbor : node.getNeighbors()) {
                neighbor.getData().setColor(new Color(173, 216, 230)); // Light blue for preprocessing
            }

            // Mark current node with a processing color
            node.getData().setColor(new Color(255, 165, 0)); // Orange for processing
        }

        private void highlightCentroid(Tree<NodeData, EdgeData>.Node node) {
            // Highlight the centroid in green
            node.getData().setColor(Color.GREEN);

            // Highlight edges connected to the centroid in red
            for (Tree<NodeData, EdgeData>.Edge edge : node.getEdges()) {
                edge.getData().setColor(Color.RED);
            }
        }


    }
    private void highlightNode(Tree<NodeData, EdgeData>.Node node) {
        for (Tree<NodeData, EdgeData>.Edge edge : node.getEdges()) {
            edge.getData().setColor(Color.RED); // Highlight subtree connections
        }
        node.getData().setColor(Color.GREEN); // Highlight centroid
    }

    /**
     * Repaint every thing to the default color
     */
    public void refresh(){
        if (tree != null) {
            for (Tree<NodeData,EdgeData>.Edge edge:tree.getEdges()){
                edge.getData().setColor(new Color(8,83,109));
            }
            for (Tree<NodeData,EdgeData>.Node node:tree.getNodes()){
                node.getData().setColor(new Color(8,83,109));
            }
        }

        repaint();
    }

    /**
     *  The component will look bad if it is sized smaller than this
     *
     *  @returns The minimum dimension
     */
    public Dimension getMinimumSize() {
        return new Dimension(1500,9000);
    }

    /**
     *  The component will look best at this size
     *
     *  @returns The preferred dimension
     */
    public Dimension getPreferredSize() {
        return new Dimension(1500,900);
    }
}
