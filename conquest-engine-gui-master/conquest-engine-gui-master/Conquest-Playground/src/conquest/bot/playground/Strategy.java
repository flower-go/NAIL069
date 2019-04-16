package conquest.bot.playground;

interface Strategy<S, A> {
    A action(S state);
}
