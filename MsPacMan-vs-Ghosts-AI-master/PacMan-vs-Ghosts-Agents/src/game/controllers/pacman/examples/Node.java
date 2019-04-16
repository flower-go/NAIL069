package game.controllers.pacman.examples;
class Node<S> implements Comparable<Node<S>>{
    public S state;
    public Node<S> parent;  // parent node, or null if this is the start node
    public int action;  // the action we took to get here from the parent
    public int cost;

    public Node(S state, Node<S> parent, int action, int cost){
        this.state = state;
        this.parent = parent;
        this.action = action;
        this.cost = cost;
    }

    @Override
    public int compareTo(Node<S> o) {
        return Integer.compare(this.cost,o.cost);
    }
}