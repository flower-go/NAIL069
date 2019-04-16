package conquest.bot.playground;// The game of Tug.
//
// Tug is a simple game played on a line, with increasing values to the right.
// The ball begins at position 0.
//
// Player 1 is trying to move the ball to the right.  If it reaches position 15 (or
// higher), player 1 wins.  Player 2 wants to move to the left.  If the ball reaches
// position -15 (or lower), player 2 wins.
//
// On each player's turn, they have a choice.  Their possible moves are
//
// - Walk:
//     90% chance: the ball moves 1 unit toward the player's goal
//     10% chance: the ball does not move
// - Tug:
//     90% chance: the ball does not move
//     10% chance: the ball moves 10 units toward the player's goal

import java.util.*;

class TugState {
    int pos;
    int player;

    public TugState(int pos, int player) {
        this.pos = pos; this.player = player;
    }

    public TugState clone() {
        return new TugState(pos, player);
    }

    void move(int delta) {
        int dir = player == 1 ?  1 : -1;

        pos += dir * delta;
        player = 3 - player;
    }

    public TugState next(int delta) {
        TugState s = clone();
        s.move(delta);
        return s;
    }

    public String toString() {
        return "pos = " + pos;
    }
}

class TugGame implements Game<TugState, Boolean> {
    static final int Size = 15;
    static final int Tug = 10;
    static final double Prob = 0.9;

    Random rand = new Random();

    public TugState initialState() {
        return new TugState(0, 1);
    }

    public TugState clone(TugState s) {
        return s.clone();
    }

    public int player(TugState s) { return s.player; }

    public void apply(TugState s, Boolean action) {
        int d;
        if (action)  // tug
            d = rand.nextDouble() < Prob ? 0 : Tug;
        else  // walk
            d = rand.nextDouble() < Prob ? 1 : 0;

        s.move(d);
    }

    public boolean isDone(TugState s) {
        return Math.abs(s.pos) >= Size;
    }

    public double outcome(TugState s) {
        return s.pos > 0 ? 1.0 : 0.0;
    }

}

// stategy: always tug
class TugStrategy implements Strategy<TugState, Boolean> {
    public Boolean action(TugState state) {
        return true;
    }
}

// strategy: always walk
class WalkStrategy implements Strategy<TugState, Boolean> {
    public Boolean action(TugState state) {
        return false;
    }
}

// strategy: randomly walk or tug
class RandomStrategy implements Strategy<TugState, Boolean> {
    Random rand = new Random();

    public Boolean action(TugState state) {
        return rand.nextInt(2) == 1;
    }
}

class TugGenerator implements Generator<TugState, Boolean> {
    static final List<Boolean> allActions = List.of(false, true);

    public List<Boolean> actions(TugState s) { return allActions; }

    public List<Possibility<TugState>> possibleResults(TugState s, Boolean action) {
        List<Possibility<TugState>> l = new ArrayList<Possibility<TugState>>();
        if (action) {  // tug
            l.add(new Possibility<TugState>(TugGame.Prob, s.next(0)));
            l.add(new Possibility<TugState>(1 - TugGame.Prob, s.next(TugGame.Tug)));
        } else {
            l.add(new Possibility<TugState>(TugGame.Prob, s.next(1)));
            l.add(new Possibility<TugState>(1 - TugGame.Prob, s.next(0)));
        }
        return l;
    }
}

class TugEvaluator implements Evaluator<TugState> {
    public double evaluate(TugState state) {
        return 0.5 + 0.5 * state.pos / TugGame.Size;
    }
}

class SmartStrategy implements Strategy<TugState, Boolean> {
    public Boolean action(TugState state) {
        // compute distance to goal
        int dist = state.player == 1 ? TugGame.Size - state.pos : state.pos - (- TugGame.Size);

        return dist >= 9;
    }
}

class Tug {
    public static void main(String[] args) {
        TugGame game = new TugGame();

        Strategy<TugState, Boolean> emm = new Expectiminimax<>(game, new TugGenerator(), new TugEvaluator(), 2);

        Runner.play(game, emm, new WalkStrategy(), 2000);
    }
}