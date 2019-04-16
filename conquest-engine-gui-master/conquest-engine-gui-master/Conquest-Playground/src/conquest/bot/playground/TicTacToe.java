package conquest.bot.playground;

import java.util.*;

class TState {
    int[] board = new int[9];
    public int player = 1;

    Random rand = new Random();

    public TState() { }
    public TState(int[] board, int player) {
        this.board = board; this.player = player;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TState)) return false;

        TState t = (TState) o;
        return player == t.player && Arrays.equals(board, t.board);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(board) + player;
    }

    @Override
    public TState clone() {
        return new TState(board.clone(), player);
    }

    List<Integer> actions() {
        List<Integer> r = new ArrayList<Integer>();
        for (int i = 0 ; i < 9 ; ++i)
            if (board[i] == 0)
                r.add(i);
        return r;
    }

    int countEmpty() {
        int count = 0;
        for (int i = 0 ; i < 9 ; ++i)
            if (board[i] == 0)
                ++count;

        return count;
    }

    int randomAction() {
        int m = rand.nextInt(countEmpty());
        int i = -1;
        for (int j = 0 ; j <= m ; ++j)
            do {
                i += 1;
            } while (board[i] != 0);

        return i;
    }

    void apply(int action) {
        if (board[action] != 0)
            throw new Error("illegal move");

        board[action] = player;
        player = 3 - player;
    }

    TState result(int action) {
        TState s = clone();
        s.apply(action);
        return s;
    }

    int winner(int s, int d) {
        int w = board[s];

        return board[s + d] == w && board[s + 2 * d] == w ? w : 0;
    }

    static int[] check =   { 0, 1, 2, 0, 3, 6, 0, 2 };
    static int[] check_d = { 3, 3, 3, 1, 1, 1, 4, 2 };

    public int winner() {
        for (int i = 0 ; i < check.length ; ++i) {
            int w = winner(check[i], check_d[i]);
            if (w > 0)
                return w;
        }
        return 0;
    }

    public boolean isDone() {
        return (winner() != 0 || countEmpty() == 0);
    }

    public double outcome() {
        switch (winner()) {
            case 0: return 0.5;   // draw
            case 1: return 1.0;
            case 2: return 0.0;
            default: throw new Error();
        }
    }

    char asChar(int i) {
        switch (i) {
            case 0: return '.';
            case 1: return 'X';
            case 2: return 'O';
            default: throw new Error();
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0 ; i < 3 ; ++i) {
            for (int j = 0 ; j < 3 ; ++j)
                sb.append(String.format("%c ", asChar(board[3 * i + j])));
            sb.append("\n");
        }

        return sb.toString();
    }
}

class TRandomStrategy implements Strategy<TState, Integer> {
    Random rand = new Random();

    public Integer action(TState s) {
        return s.randomAction();
    }
}

class BasicStrategy implements Strategy<TState, Integer> {
    public Integer action(TState s) {
        // win if possible
        for (int i = 0 ; i < 9 ; ++i)
            if (s.board[i] == 0 && s.result(i).isDone())
                return i;

        // block a win if possible
        TState t = new TState(s.board, 3 - s.player);  // assume other player's turn
        for (int i = 0 ; i < 9 ; ++i)
            if (t.board[i] == 0 && t.result(i).isDone())
                return i;

        // move randomly
        return s.randomAction();
    }
}

class TGenerator implements Generator<TState, Integer> {
    public List<Integer> actions(TState s) { return s.actions(); }

    public List<Possibility<TState>> possibleResults(TState state, Integer action) {
        return List.of(new Possibility<TState>(1.0, state.result(action)));
    }
}

class TEvaluator implements Evaluator<TState> {
    public double evaluate(TState state) {
        return 0.5;   // just a guess
    }
}

public class TicTacToe implements Game<TState, Integer> {
    public TState initialState() {	return new TState(); }

    public TState clone(TState state) { return state.clone(); }

    public int player(TState state) { return state.player; }

    public void apply(TState state, Integer action) { state.apply(action); }

    public boolean isDone(TState state) { return state.isDone(); }

    public double outcome(TState state) { return state.outcome(); }

    public static void main(String[] args) {
        TicTacToe game = new TicTacToe();

        //Strategy<TState, Integer> emm = new Expectiminimax<>(game, new TGenerator(), new TEvaluator(), 5);

        //Runner.play(game, emm, new TRandomStrategy(), 500);

        Strategy<TState, Integer> emm = new Expectiminimax<>(game, new TGenerator(), new TEvaluator(), 8);

        Runner.play(game,  new BasicStrategy(), emm, 10);
    }

}