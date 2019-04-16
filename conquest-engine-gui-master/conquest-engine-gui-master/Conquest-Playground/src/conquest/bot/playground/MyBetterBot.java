package conquest.bot.playground;

import conquest.bot.fight.FightSimulation;
import conquest.bot.state.*;
import conquest.engine.Config;
import conquest.engine.RunGame;
import conquest.game.FightMode;
import conquest.game.world.Region;
import conquest.utils.Util;

import java.io.File;
import java.util.List;

public class MyBetterBot extends GameBot {

    private PlaceMoveAction placeMove = null;
    // if using expectiminimax
    Expectiminimax<GameState, Action> strategy =
            new Expectiminimax<>(new WarlightGame(), new WarlightGenerator(), new WarlightEvaluator(), 1);

    FightSimulation.FightAttackersResults attackResults;

    public MyBetterBot() {
        attackResults = FightSimulation.FightAttackersResults.loadFromFile(Util.file(
                "../Conquest-Bots/FightSimulation-Attackers-A200-D200.obj"));
    }

    @Override
    public ChooseCommand chooseRegion(List<Region> choosable, long timeout) {
        return (ChooseCommand) strategy.action(state);
    }

    @Override
    public List<PlaceCommand> placeArmies(long timeout) {
        placeMove =  (PlaceMoveAction) strategy.action(state);
        return placeMove.placeCommands;
    }

    @Override
    public List<MoveCommand> moveArmies(long timeout) {
        return placeMove.moveCommands;
    }

    public static void runInternal() {
        Config config = new Config();

        config.bot1Init = "internal:conquest.bot.playground.MyBetterBot";

        config.bot2Init = "internal:conquest.bot.custom.AggressiveBot";
        //config.bot2Init = "human";

        config.botCommandTimeoutMillis = 20 * 1000;

        config.game.maxGameRounds = 200;

        config.game.fight = FightMode.CONTINUAL_1_1_A60_D70;

        config.visualize = true;

        config.replayLog = new File("./replay.log");

        RunGame run = new RunGame(config);
        run.go();

        System.exit(0);
    }

    public static void main(String[] args)
    {
        runInternal();

        //JavaBot.exec(new String[]{"conquest.bot.custom.AggressiveBot", "./AggressiveBot.log"});
    }

}
