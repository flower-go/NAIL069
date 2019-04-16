package cz.sokoban4j.playground;

/* The classic sliding block puzzle, i.e. the 8-puzzle or 15-puzzle.
 *
 * Construct the 8-puzzle like this:
 *
 *    new NPuzzle(3);
 *
 * The starting position has the tiles in reversed order:
 *
 *   8 7 6
 *   5 4 3
 *   2 1 _
 *
 * The goal position is
 *
 *   _ 1 2
 *   2 3 4
 *   5 6 7
 *
 * The minimal solution has 28 steps.
 *
 * The heuristic function below is the sum of the Manhattan distances of tiles
 * from their goal positions.  With this heuristic, A* should find the solution
 * while expanding only a few hundred nodes.
 *
 * The corresponding 15-puzzle is
 *
 *   new NPuzzle(4);
 *
 * This is much harder, and requires pattern databases to solve effectively.
 */

import java.util.*;

// direction in which the a tile moves
enum Dir { Left, Right, Up, Down }

class PuzzleState {
    final int size;
    final int[] squares;
    final int empty;  // index of empty square

    public PuzzleState(int size, int[] squares, int empty) {
        this.size = size; this.squares = squares; this.empty = empty;
    }

    static int findEmpty(int[] a) {
        for (int i = 0 ; i < a.length ; ++i)
            if (a[i] == 0)
                return i;
        throw new Error("no empty square");
    }

    public PuzzleState(int size, int[] squares) {
        this(size, squares, findEmpty(squares));
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof PuzzleState) {
            PuzzleState s = (PuzzleState) o;
            return Arrays.equals(squares, s.squares);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(squares);
    }

    public List<Dir> possibleDirections() {
        List<Dir> dirs = new ArrayList<Dir>();
        int r = empty / size;
        int c = empty % size;

        if (r > 0)
            dirs.add(Dir.Down);
        if (r < size - 1)
            dirs.add(Dir.Up);
        if (c > 0)
            dirs.add(Dir.Right);
        if (c < size - 1)
            dirs.add(Dir.Left);

        return dirs;
    }

    public PuzzleState slide(Dir dir) {
        int d;

        switch (dir) {
            case Left: d = -1; break;
            case Right: d = 1; break;
            case Up: d = - size; break;
            case Down: d = size; break;
            default: throw new Error();
        }

        int[] s = squares.clone();
        s[empty] = s[empty - d];
        s[empty - d] = 0;
        return new PuzzleState(size, s, empty - d);
    }

    public boolean isGoal() {
        for (int i = 0 ; i < size * size ; ++i)
            if (squares[i] != i)
                return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();

        for (int i = 0 ; i < size ; ++i) {
            for (int j = 0 ; j < size ; ++j)
                sb.append(String.format("%d ", squares[i * size + j]));
            sb.append("\n");
        }

        return sb.toString();
    }
}

class NPuzzle implements Problem<PuzzleState, Dir> {
    PuzzleState initial;

    // Construct a puzzle where all the tiles are in reverse order.
    public NPuzzle(int size) {
        int[] a = new int[size * size];

        for (int i = 0 ; i < size * size ; ++i)
            a[i] = size * size - 1 - i;

        initial = new PuzzleState(size, a);
    }

    // Construct a puzzle by making _moves_ random moves from the goal state.
    public NPuzzle(int size, int moves) {
        Random rand = new Random();

        int[] a = new int[size * size];
        for (int i = 0 ; i < size * size ; ++i)
            a[i] = i;
        PuzzleState state = new PuzzleState(size, a);

        for (int i = 1 ; i <= moves ; ++i) {
            List<Dir> l = state.possibleDirections();
            int which = rand.nextInt(l.size());
            state = state.slide(l.get(which));
        }

        initial = new PuzzleState(size, a);
    }

    public PuzzleState initialState() {
        return initial;
    }

    public List<Dir> actions(PuzzleState state) {
        return state.possibleDirections();
    }

    public PuzzleState result(PuzzleState state, Dir action) {
        return state.slide(action);
    }

    public boolean isGoal(PuzzleState state) { return state.isGoal(); }

    @Override
    public int cost(PuzzleState state, Dir action) {
        return 1;
    }

    // taxicab distance between squares i and j
    static int dist(int size, int i, int j) {
        return Math.abs(i / size - j / size) + Math.abs(i % size - j % size);
    }

    @Override
    public int estimate(PuzzleState state) {
        // Compute the sum of the Manhattan distances of tiles from their goal positions.
        int sum = 0;
        for (int i = 0 ; i < state.size * state.size ; ++i)
            if (state.squares[i] > 0)
                sum += dist(state.size, state.squares[i], i);
        return sum;
    }

    public static void main(String[] args) {
        NPuzzle prob = new NPuzzle(3);

        Stats stats = new Stats();
        Node<PuzzleState, Dir> g = AStar.search(prob, stats);
        if (g != null) {
            System.out.format("optimal solution has %d steps\n", g.gn);
            System.out.format("expanded %d nodes\n", stats.expanded);
        }
        else
            System.out.println("no solution found");
    }

}
