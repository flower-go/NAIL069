package cz.sokoban4j.playground;

class Node<S, A> implements Comparable<Node<S,A>> {
    public S state;
    public Node<S, A> parent;  // parent node, or null if this is the start node
    public A action;  // the action we took to get here from the parent
    public int gn;
    private int hn; // heuristics


    public Node(S state, Node<S,A> parent, A action, int cost, int hn){
        this.state = state;
        this.parent = parent;
        this.action = action;
        this.gn = cost;
        this.hn = hn;
    }

    // TODO treba pripocitavat jeste heuristiku
    @Override
    public int compareTo(Node<S,A> o) {
        return Integer.compare(this.getFn(),o.getFn());
    }


    public int getFn() {
        return gn + hn;
    }

    public int getHn() {
        return hn;
    }
}
