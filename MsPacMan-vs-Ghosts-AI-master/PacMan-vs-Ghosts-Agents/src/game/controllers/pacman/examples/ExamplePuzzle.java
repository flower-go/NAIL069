package game.controllers.pacman.examples;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExamplePuzzle implements Problem {
    Object[] states = {'S','A','B', 'C', 'D', 'G'};
    List<HashMap<Integer,Character>> actionList = new ArrayList<HashMap<Integer, Character>>();

    public ExamplePuzzle() {
        HashMap<Integer,Character> S = new HashMap<>();
        S.put(1,'A');
        S.put(12,'G');
        HashMap<Integer,Character> A = new HashMap<>();
        A.put(3,'B');
        A.put(1,'C');
        HashMap<Integer,Character> B = new HashMap<>();
        B.put(3,'D');
        HashMap<Integer,Character> C = new HashMap<>();
        C.put(1,'D');
        C.put(2, 'G');
        HashMap<Integer,Character> D = new HashMap<>();
        D.put(3,'G');
        D.put(1,'C');
        HashMap<Integer,Character> G = new HashMap<>();

        actionList.add(0, S);
        actionList.add(1, A);
        actionList.add(2, B);
        actionList.add(3, C);
        actionList.add(4, D);
        actionList.add(5, G);
    }

    @Override
    public Object initialState() {
        return 'S';
    }

    @Override
    public List<Integer> actions(Object state) {
        int index = 0;
        for (int i = 0; i <states.length; i++) {
            if (states[i].equals(state)) {
                index = i;
            }
        }
        return new ArrayList<>(actionList.get(index).keySet());
    }

    @Override
    public Object result(Object state, int action) {
        int index = 0;
        for (int i = 0; i <states.length; i++) {
            if (states[i] == state) {
                index = i;
            }
        }
        return actionList.get(index).get(action);
    }

    @Override
    public boolean isGoal(Object state) {
        if (state.equals('G'))
            return true;
        return false;
    }

    @Override
    public int cost(Object state, int action) {
        return action;
    }
}
