package cz.sokoban4j.playground;

import java.util.*;

// A* search
class AStar<S, A> {

    static Stats statsG;

    public static <S, A> Node<S, A> search(Problem<S, A> prob, Stats stats) {
        statsG = stats;
        stats.expanded = 0;
        PriorityQueue<Node<S,A>> frontier = new PriorityQueue<Node<S,A>>();
        HashMap<S, Node<S,A>> mapping = new HashMap<S, Node<S,A>>();
        Set<S> explored = new HashSet<S>();

        Node<S,A> node = new Node<S,A>(prob.initialState(), null, null, 0, 0);
        addToFrontier(node, frontier, mapping, prob);


        while (frontier.size() > 0) {

            Node<S,A> actual = removeFromFontier(frontier, mapping);

            if (prob.isGoal(actual.state)) return actual;
            if (explored.contains(actual.state)) continue;

            explored.add(actual.state);

            expand(actual, frontier, mapping, prob);


        }

        return null;
    }

    private static <S,A> void addToFrontier(Node<S,A> node, PriorityQueue<Node<S,A>> f, HashMap<S, Node<S,A>> m, Problem<S,A> p) {
        f.add(node);
        m.put(p.initialState(), node);
    }

    private static <S,A> Node<S,A> removeFromFontier(PriorityQueue<Node<S,A>> f, HashMap<S, Node<S,A>> m) {
        Node<S,A> result = f.poll();
        m.remove(result.state);
        return result;
    }


    private static <S,A> void expand(Node<S,A> node, PriorityQueue<Node<S,A>> f, HashMap<S, Node<S,A>> m, Problem<S,A> p) {
        statsG.expanded++;
        List<A> actions = p.actions(node.state);
        for (A i : actions) {
            S state = p.result(node.state, i);
            int cost = p.cost(node.state, i);

            if(state == null)
                continue;
            Node<S,A> podobny = m.get(state);
            if (podobny != null) {
                if (cost + node.gn + podobny.getHn() < podobny.getFn()) {
                    Node<S,A> novy = new Node<S,A>(state, node, i, cost + node.gn, podobny.getHn());
                    addToFrontier(novy, f, m, p);
                }

            } else {
                Node<S,A> novy = new Node<S,A>(state, node, i, cost + node.gn,p.estimate(state));
                addToFrontier(novy, f, m, p);
            }
        }
    }
}

