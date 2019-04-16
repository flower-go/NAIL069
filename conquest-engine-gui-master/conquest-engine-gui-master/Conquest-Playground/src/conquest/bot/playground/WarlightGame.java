package conquest.bot.playground;

import conquest.bot.fight.FightSimulation;
import conquest.bot.state.*;
import conquest.engine.Config;
import conquest.engine.RunGame;
import conquest.game.FightMode;
import conquest.game.world.Region;
import conquest.utils.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WarlightGame implements Game<GameState, Action> {
    @Override
    public GameState initialState() {
        return new GameState();
    }

    @Override
    public GameState clone(GameState state) {
        return state.clone();
    }

    @Override
    public int player(GameState state) {
        return state.me;
    }

    @Override
    public void apply(GameState state, Action action) {
        action.apply(state);
    }

    @Override
    public boolean isDone(GameState state) {
        return state.isDone();
    }

    @Override
    public double outcome(GameState state) {
        if (state.winningPlayer() == state.me){
            return 1.0;
        } else if(state.winningPlayer() == state.opp){
            return 0;
        } else {
            return 0.5;
        }
    }
}

class WarlightGenerator implements Generator<GameState, Action>{

    Random rand = new Random();

    @Override
    public List<Action> actions(GameState state) {
        switch (state.getPhase()){
            case STARTING_REGIONS:
                return startingRegions(state);
            case PLACE_ARMIES:
                return placeArmies(state);
        }
        return null;
    }

    @Override
    public List<Possibility<GameState>> possibleResults(GameState state, Action action) {
        return null;
    }

    private List<Action> startingRegions(GameState state){
        List<Region> choosable = state.getPickableRegions();
        Action a = new ChooseCommand(choosable.get(rand.nextInt(choosable.size())));
        List<Action> result = new ArrayList<>();
        result.add(a);
        return result;
    }

    private List<Action> placeArmies(GameState state)
    {
        List<Action> result = new ArrayList<>();

        PlayerState me = state.players[state.me];
        List<Region> mine = new ArrayList<Region>(me.regions.keySet());
        int numRegions = mine.size();

        int[] count = new int[numRegions];
        for (int i = 0 ; i < me.placeArmies ; ++i) {
            int r = rand.nextInt(numRegions);
            count[r]++;
        }

        List<PlaceCommand> ret = new ArrayList<PlaceCommand>();
        for (int i = 0 ; i < numRegions ; ++i)
            if (count[i] > 0)
                ret.add(new PlaceCommand(mine.get(i), count[i]));

        List<MoveCommand> retMove = new ArrayList<MoveCommand>();

        for (RegionState rs : state.players[state.me].regions.values()) {
            int counter = rand.nextInt(rs.armies);
            if (counter > 0) {
                List<Region> neighbors = rs.region.getNeighbours();
                Region to = neighbors.get(rand.nextInt(neighbors.size()));
                retMove.add(new MoveCommand(rs.region, to, counter));
            }
        }

        Action akce =  new PlaceMoveAction(ret, retMove);
        result.add(akce);
        return result;
        // return null;
    }
}

class WarlightEvaluator implements Evaluator<GameState>{

    @Override
    public double evaluate(GameState state) {
        return 0;
    }
}

