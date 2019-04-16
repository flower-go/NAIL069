package conquest.bot.playground;

import conquest.bot.state.Action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Expectiminimax<S, A> implements Strategy<S, A> {


    private final Game<S, A> game;
    private final Generator<S, A> generator;
    private final Evaluator<S> evaluator;
    private final int maxDepth;

    public Expectiminimax(Game<S, A> game, Generator<S, A> generator, Evaluator<S> evaluator,
                          int maxDepth) {


        this.game = game;
        this.generator = generator;
        this.evaluator = evaluator;
        this.maxDepth = maxDepth;
    }

    /*
    Returns which action to take in given state
     */
    @Override
    public A action(S state) {
        List<A> actions = generator.actions(state);

        double max;
        if (game.player(state) == 1) {
            max = -Double.MAX_VALUE;
        } else {
            max = Double.MAX_VALUE;
        }

        A actionMax = null;

        for (A action : actions
                ) {
            double value = 0;
            for (Possibility<S> p : generator.possibleResults(state, action)
                    ) {
                try {
                    value += p.prob * expectiminimax(p.state, maxDepth, -Double.MAX_VALUE, Double.MAX_VALUE, game.player(p.state) == 1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (game.player(state) == 1) {
                if (value >= max) {
                    actionMax = action;
                    max = value;
                }
            } else {
                if (value <= max) {
                    actionMax = action;
                    max = value;
                }
            }

        }
        return actionMax;
    }

    private double expectiminimax(S state, int depth, double alpha, double beta, boolean maximizing) throws Exception {

        if (game.isDone(state)) {
            return game.outcome(state);
        }
        if (depth <= 0) {
            return evaluator.evaluate(state);
        }

        List<A> actions = generator.actions(state);

        ActionScore best = null;

        for (A action : actions
                ) {

            ActionScore a = computeActionScore(action, state, depth, alpha, beta);

            // scores.add(a);
            if (best == null) {
                best = a;
            } else if (maximizing) {
                // return scores.get(0).score;
                if (best.score < a.score) {
                    best = a;
                }
                alpha = alpha > a.score ? alpha : a.score;
                if (alpha >= beta) return best.score;

            } else {
                //return scores.get(scores.size() - 1).score;
                if (best.score > a.score) {
                    best = a;
                }
                beta = beta > a.score ? a.score : beta;
                if (beta <= alpha) {
                    return best.score;
                }
            }

        }
        return best.score;
        // Collections.sort(scores);
    }

    private ActionScore computeActionScore(A action, S state, int depth, double alpha, double beta) {
        double score = 0;
        List<Possibility<S>> actions = generator.possibleResults(state, action);
        for (Possibility<S> p : actions
                ) {
            try {
                score += p.prob * expectiminimax(p.state, depth - 1, alpha, beta, game.player(p.state) == 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return new ActionScore(score, action);
    }

    class ActionScore implements Comparable<ActionScore> {
        double score;
        A action;

        public ActionScore(double score, A action) {
            this.action = action;
            this.score = score;
        }

        @Override
        public int compareTo(ActionScore o) {
            return Double.compare(this.score, o.score);
        }
    }
}
