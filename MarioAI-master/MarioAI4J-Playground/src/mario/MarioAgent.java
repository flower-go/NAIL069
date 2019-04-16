package mario;

import java.awt.Graphics;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import ch.idsia.agents.AgentOptions;
import ch.idsia.agents.IAgent;
import ch.idsia.agents.controllers.MarioHijackAIBase;
import ch.idsia.agents.controllers.modules.Entities;
import ch.idsia.agents.controllers.modules.Tiles;
import ch.idsia.benchmark.mario.MarioSimulator;
import ch.idsia.benchmark.mario.engine.LevelScene;
import ch.idsia.benchmark.mario.engine.VisualizationComponent;
import ch.idsia.benchmark.mario.engine.generalization.Entity;
import ch.idsia.benchmark.mario.engine.generalization.EntityType;
import ch.idsia.benchmark.mario.engine.generalization.Tile;
import ch.idsia.benchmark.mario.engine.input.MarioControl;
import ch.idsia.benchmark.mario.engine.input.MarioInput;
import ch.idsia.benchmark.mario.environments.IEnvironment;
import ch.idsia.tools.EvaluationInfo;
import ch.idsia.utils.MarioLog;

/**
 * Code your custom agent here!
 * <p>
 * Modify {@link #actionSelectionAI()} to change Mario's behavior.
 * <p>
 * Modify {@link #debugDraw(VisualizationComponent, LevelScene, IEnvironment, Graphics)} to draw custom debug information.
 * <p>
 * You can change the type of level you want to play in {@link #main(String[])}.
 * <p>
 * Once your agent is ready, you can use the {@link Evaluate} class to benchmark the quality of your AI.
 */
public class MarioAgent extends MarioHijackAIBase implements IAgent {

    @Override
    public void reset(AgentOptions options) {
        super.reset(options);
    }

    private boolean hole = false;
    private boolean danger = false;
    private boolean dangerCompute = false;
    int[] stairs = new int[3];

    @Override
    public void debugDraw(VisualizationComponent vis, LevelScene level, IEnvironment env, Graphics g) {
        super.debugDraw(vis, level, env, g);
        if (mario == null) return;
        String debug = "";
        if (mario.onGround) debug += "|OnGRND|";
        if (danger) debug += "|DANGER|";
        if (hole) debug += "|HOLE|";
        if (dangerCompute) debug += "|DANGERcOUNT|";


        // provide custom visualization using 'g'

        // EXAMPLE DEBUG VISUALIZATION

        VisualizationComponent.drawStringDropShadow(g, debug, 0, 26, 1);
    }

    /**
     * Called on each tick to find out what action(s) Mario should take.
     * <p>
     * Use the {@link #e} field to query entities (Goombas, Spikies, Koopas, etc.) around Mario;
     * see {@link EntityType} for a complete list of entities.
     * Important methods you will definitely need: {@link Entities#danger(int, int)} and {@link Entities#entityType(int, int)}.
     * <p>
     * Use the {@link #t} field to query tiles (bricks, flower pots, etc.} around Mario;
     * see {@link Tile} for a complete list of tiles.
     * An important method you will definitely need: {@link Tiles#brick(int, int)}.
     * <p>
     * Use {@link #control} to output actions (technically this method must return {@link #action} in order for
     * {@link #control} to work).
     * Note that all actions specified through {@link #control} run in "parallel"
     * (except {@link MarioControl#runLeft()} and {@link MarioControl#runRight()}, which cancel each other out in consecutive calls).
     * Also note that you have to call {@link #control} methods on every {@link #actionSelectionAI()} tick
     * (otherwise {@link #control} will think you DO NOT want to perform that action}.
     */
    @Override
    public MarioInput actionSelectionAI() {
        if (mario.mayShoot) control.shoot();
        if(mario.onGround && mario.speed.x < 0) control.runRight();
        if(monsterAbove(0) || monsterAbove(1) )   {
            control.runLeft();
            //control.sprint();
        }
        else if(mario.onGround){
                    if(waitForFlower()){

                    }
                    else if ((brickAhead() || dangerAhead()) ) {
                        if(!(e.danger(1,2) || e.danger(1,3) || e.danger(1,4))){
                            control.runRight();
                            //control.sprint();
                            control.jump();
                        }

                    } else {

                        control.runRight();
                        control.sprint();
                        if (holeAhead(2) && 2< mario.speed.x)
                            //if(!(e.danger(4,1)|| e.danger(5,1)))
                            
                            control.jump();

                    }


        }
        else {
            int direction;
            direction = monsterUnder(0);
            if(direction == 0) direction = monsterUnder(1);
            if (0< mario.speed.y && (direction != 0)) {
                if(direction == -1){
                    control.runLeft();
                }
                else control.runRight();

                control.sprint();
                control.jump();
            } else {
                control.jump();
                control.runRight();
                //control.sprint();

            }


        }


        // RETURN THE RESULT
        return action;
    }

    public static void main(String[] args) {
        // YOU MAY RAISE THE LOGGING LEVEL, even though there is probably no inforamation you need to know...
        //MarioLog.setLogLevel(Level.ALL);

        // UNCOMMENT THE LINE OF THE LEVEL YOU WISH TO RUN

        //LevelConfig level = LevelConfig.LEVEL_0_FLAT;
        //LevelConfig level = LevelConfig.LEVEL_1_JUMPING;
        //LevelConfig level = LevelConfig.LEVEL_2_GOOMBAS;
        //LevelConfig level = LevelConfig.LEVEL_3_TUBES;
        LevelConfig level = LevelConfig.LEVEL_4_SPIKIES;

        // CREATE SIMULATOR
        //MarioSimulator simulator = new MarioSimulator(level.getOptions());

        // CREATE SIMULATOR AND RANDOMIZE LEVEL GENERATION
        // -- if you wish to use this, comment out the line above and uncomment the line below
        MarioSimulator simulator = new MarioSimulator(level.getOptionsRandomized());

        // INSTANTIATE YOUR AGENT
        IAgent agent = new MarioAgent();

        // RUN THE SIMULATION
        EvaluationInfo info = simulator.run(agent);

        // CHECK RESULT
        switch (info.getResult()) {
            case LEVEL_TIMEDOUT:
                MarioLog.warn("LEVEL TIMED OUT!");
                break;

            case MARIO_DIED:
                MarioLog.warn("MARIO KILLED");
                break;

            case SIMULATION_RUNNING:
                MarioLog.error("SIMULATION STILL RUNNING?");
                throw new RuntimeException("Invalid evaluation info state, simulation should not be running.");

            case VICTORY:
                MarioLog.warn("VICTORY!!!");
                break;
        }
    }

    private boolean brickAhead() {
        return
                t.brick(1, 0)
                        || t.brick(2, 0)
                        || t.brick(3, 0);
    }

    private boolean smallHole() {
        return (mario.onGround && (t.emptyTile(1, 1) || t.emptyTile(2, 1)));
    }

    private boolean dangerAhead() {
        return
                e.danger(1, 0)
                        || e.danger(2, 0)
                ;
    }

    private boolean waitDangerAhead(int max) {
        if (!danger)
            stairs = getStairs(max);
        if (stairs != null) {

            return checkDangerInArea();
        }
        return false;
    }

    private boolean checkDangerInArea() {
        for (int x = stairs[1]; x <= stairs[2]; x++) {
            int y = stairs[0];
            boolean isDanger = e.danger(x, y);
            if (isDanger) {
                danger = true;
                return true;
            }
        }
        return false;
    }

    private int[] getStairs(int max) {
        int initY = 0;
        int[] result = new int[3];
        int stairs = 0;
        //ArrayList<Integer> result = new ArrayList<>();
        boolean up = false;
        for (int x = 1; x < max; x++) {
            int y = initY + 1;
            if (t.brick(x, initY)) {
                up = true;
                return null;
            }

            while (!t.brick(x, y)) {
                y++;
            }


            if (initY < y - 1) {
                stairs++;
                initY = y - 1;
                if (stairs == 1) {
                    result[0] = y - 1;
                    result[1] = x;
                } else if (stairs == 2) {
                    result[2] = x - 1;
                    return result;
                } else if (2 < stairs) return result;
            }
        }
        return null;
    }

    private boolean waitForFlower(){
        for(int i = 9; 0 < i; i--){
            if(t.brick(1,-1 * i) || t.brick(2,-1 * i)) return false;
            if(e.entities(1,-1*i).size() != 0){
                if(e.entities(1,-1*i).get(0).type == EntityType.ENEMY_FLOWER )
                    return true;
            }
            if(e.entities(2,-1*i).size() != 0){
                if(e.entities(2,-1*i).get(0).type == EntityType.ENEMY_FLOWER )
                    return true;
            }
        }
        return false;
    }

    private boolean holeAhead(int length) {
        for (int i = 1; i < length; i++) {
            if (t.emptyTile(i, 1)) {
                hole = true;
                return true;
            }
        }
        hole = false;
        return false;
    }


    private int dangerCount(int width, int depth, int start) {
        int res = 0;
        for (int i = 1; i < width; i++) {
            for (int j = start; j < depth; j++) {
                res += convBool(e.danger(i, j)  && !t.brick(i, j));
            }
        }
        return res;
    }

    private boolean dangerFarAhead() {
        return
                e.danger(4, 2) || e.danger(4, -1) || e.danger(4, 1)
                        || e.danger(2, 2) || e.danger(2, -1) || e.danger(2, 1)
                        || e.danger(3, 2) || e.danger(2, -1) || e.danger(3, 1);

    }

    private int monsterUnder(int x) {
        int i = 0;
        if (!mario.onGround) {
            while (t.brick(x, i) == false) {
                if (e.danger(x, i) || e.danger(x-1,i)){
                    if(e.entities(x,i).size() == 0) return 0;
                    Entity monster = e.entities(x,i).get(0);
                    if(0 < monster.speed.x){

                        if(t.brick(x+1,i)) return 1;
                        else
                            if(e.danger(x-1,i)) return 0;
                            return -1;
                    }
                    else if(monster.speed.x < 0 )
                    return 1;
                }


                i++;
                if (Math.min(mario.receptiveFieldHeight,2) < i) break;
            }
            return 0;
        }
        return 0;
    }

    private boolean monsterAbove(int x) {
        int i = -6;

        while (t.brick(x, i) == false) {
            if (e.danger(x, i)){
                danger = true;
                return true;
            }

            i++;
            if (i == 0) break;
        }

        return false;

    }

    public static int convBool(boolean b) {
        int convBool = 0;
        if (b) convBool = 1;
        return convBool;
    }

}