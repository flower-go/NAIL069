package conquest.bot.playground;

public class Possibility<S> {
    public double prob; // probability from 0..1
    public S state;

    public Possibility(double prob, S state){
        this.prob = prob;
        this.state = state;
    }

}
