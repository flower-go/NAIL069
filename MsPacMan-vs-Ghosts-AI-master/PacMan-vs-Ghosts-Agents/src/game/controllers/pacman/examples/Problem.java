package game.controllers.pacman.examples;
import java.util.List;

// S = state type, A = action type
public interface Problem<S, A> {
    S initialState();
    List<A> actions(S state);
    S result(S state, A action);
    boolean isGoal(S state);
    int cost(S state, A action);

    int estimate(S state);  // optimistic estimate of cost from state to goal
}