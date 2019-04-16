package game.controllers.pacman.examples;
import java.util.*;

public class UCS {
    boolean notFear = false;
    public static <S> Node<S> search(Problem<S> prob) {
        PriorityQueue<Node<S>> frontier = new PriorityQueue<>();
        HashMap<S, Node<S>> mapping = new HashMap<>();
        Set<S> explored  = new HashSet<S>();

        Node<S> node = new Node<>(prob.initialState(),null,-1,0);
        addToFrontier(node, frontier, mapping, prob);


        while (frontier.size() > 0){

            Node<S> actual = removeFromFontier(frontier,mapping);

            if(prob.isGoal(actual.state)) return actual;
            if(explored.contains(actual.state)) continue;

            explored.add(actual.state);
            expand(actual,frontier,mapping,prob);


        }

        return null;
    }

    private static <S> void addToFrontier(Node<S> node,PriorityQueue<Node<S>> f, HashMap<S, Node<S>> m, Problem<S> p ){
        f.add(node);
        m.put(p.initialState(),node);
    }

    private static <S> Node<S> removeFromFontier(PriorityQueue<Node<S>> f, HashMap<S, Node<S>> m){
        Node<S> result =  f.poll();
        m.remove(result.state);
        return result;
    }


    private static <S> void expand(Node<S> node,PriorityQueue<Node<S>> f, HashMap<S, Node<S>> m, Problem<S> p ){
        List<Integer> actions  = p.actions(node.state);
        for (Integer i : actions){
            S state = p.result(node.state,i);
            int cost = p.cost(node.state,i);

                Node<S> podobny = m.get(state);
                if(podobny != null) {
                    if(cost + node.cost < podobny.cost){
                        Node<S> novy = new Node<S>(state,node,i, cost + node.cost);
                        addToFrontier(novy,f,m,p);
                    }

                }
                else {
                    Node<S> novy = new Node<S>(state,node,i, cost + node.cost);
                    addToFrontier(novy,f,m,p);
                }
        }
    }

}
