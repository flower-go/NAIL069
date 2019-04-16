package conquest.bot.playground;

public class Runner {
    public static <S, A> void play(
            Game<S, A> game, Strategy<S, A> strat1, Strategy<S, A> strat2, int count) {

        int[] wins = new int[3];

        for (int i = 0 ; i < count ; ++i) {

            S s = game.initialState();
            while (!game.isDone(s)) {
                A a = game.player(s) == 1 ? strat1.action(s) : strat2.action(s);

                game.apply(s, a);
            }

            double o = game.outcome(s);
            if (o == 0.5)
                ++wins[0];
            else if (o > 0.5)
                ++wins[1];
            else ++wins[2];
        }

        System.out.format("%s won %d (%.1f%%), ",
                strat1.getClass().getSimpleName(), wins[1], 100.0 * wins[1] / count);

        if (wins[0] > 0)
            System.out.format("%d draws (%.1f%%), ", wins[0], 100.0 * wins[0] / count);

        System.out.format("%s won %d (%.1f%%)\n",
                strat2.getClass().getSimpleName(), wins[2], 100.0 * wins[2] / count);
    }
}
