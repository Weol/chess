package net.rahka.agent;

public class AStarAgent  {

    /**
    private SortedSet<Node> frontier;
    private Set<Node> explored;

    private Map<Node, Integer> costs;

    public AStarAgent() {
        this.frontier = new SortedHashSet<>(Comparator.comparingInt(this::compareNodes));
        this.explored = new HashSet<>();

        costs = new HashMap<>();
    }

    @Override
    public Node[] execute() {
        costs.clear(); //Clear the Map of any values from previous executions
        costs.put(getOrigin(), 0); //Add the origin into the distances Map, it costs 0 to get from origin to origin

        Node node = getOrigin();
        while (node != getGoal()) {
            Node neighbourGoal = null;

            for (Node neighbour : node.getNeighbours()) {
                if (neighbour == node || explored.contains(neighbour)) continue; //Skip explored nodes

                //Set neighbourGoal to neighbour and exit for loop if neighbour is the goal node
                if (neighbour == getGoal()) {
                    neighbourGoal = neighbour;
                }

                int cost = costs.get(node) + neighbour.getWeight();

                //If map does not contain the cost to the node, then it has never been seen before
                //Else if the cost is lower than the stored cost then we replace it, we have found a faster path to it
                if (!costs.containsKey(neighbour) || costs.get(neighbour) >= cost) {
                    costs.put(neighbour, cost);
                    neighbour.setExpandedBy(node);

                    frontier.add(neighbour);
                }
            }

            frontier.remove(node);
            explored.add(node);

            //This simply notifies that the algorithm state has changed, only used for animating the algorithm
            onAlgorithmStateChange(getAlgorithmState(node));

            //If neighbourGoal is not null then we found the goal node and set node to neighbourGoal so the loop ends
            if (neighbourGoal != null) {
                node = neighbourGoal;
            } else {
                node = frontier.first();
            }
        }
        frontier.remove(node);

        onAlgorithmStateChange(getAlgorithmState(node));

        return getPathTo(node).toArray(new Node[0]); //Return the final path
    }

    private int compareNodes(Node node) {
        return costs.get(node) + manhattanDistance(node, getGoal());
    }

    private int manhattanDistance(Node from, Node to) {
        return Math.abs(from.getX() - to.getX()) + Math.abs(from.getY() - to.getY());
    }

    @Override
    public Move getMove(Collection<Move> moves, Collection<Piece> pieces, Board board) {
        return null;
    }
    **/

}
