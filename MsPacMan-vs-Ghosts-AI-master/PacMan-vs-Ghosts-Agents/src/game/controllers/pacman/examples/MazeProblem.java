package game.controllers.pacman.examples;

import game.core.G;
import game.core.Game;

import java.util.ArrayList;
import java.util.List;

public class MazeProblem implements Problem<Integer> {
    private Game game;
    int[] activePills;
    int[] activePowerPills;
    int[] ghostPos;
    boolean ghostsEdible;



    public MazeProblem(Game game){
        this.game = game;
        activePills = game.getPillIndicesActive();
        activePowerPills = game.getPowerPillIndicesActive();
        ghostsEdible = isAnyEdible();
        ghostPos = new int [G.NUM_GHOSTS];
        for(int i = 0; i < G.NUM_GHOSTS; i++){
            ghostPos[i] = game.getCurGhostLoc(i);
        }

    }

    @Override
    public Integer initialState() {
        return game.getCurPacManLoc();
    }

    @Override
    public List<Integer> actions(Integer state) {
        List<Integer> result = new ArrayList<>();
        for(int i = 0; i < 4; i++){
            if(game.getNeighbour(state,i) != -1) result.add(i);
        }

        return result;
    }

    @Override
    public Integer result(Integer state, int action) {
        return game.getNeighbour(state,action);
    }

    @Override
    public boolean isGoal(Integer state) {
        if(isDanger(state)) return false;
        if(state == game.getCurPacManLoc()) return false;
        int index;
       /* if(0 < activePowerPills.length ){
            index = game.getPowerPillIndex(state);
            if(index != -1){
                if(game.checkPowerPill(index)) return true;
            }

            return false;
        }
        */
        if(ghostsEdible) {
            index =  indexOf(state,ghostPos);
            if(index != -1){
                if(game.isEdible(index))
                    return true;
            }
            return false;
        }
        // TODO HERE is the problem
        index = game.getPillIndex(state);
        if(index != -1){
            if(game.checkPill(index) ) return true;
        }
        index = game.getPowerPillIndex(state);
        if(index != -1){
            if(game.checkPowerPill(index)) return true;
        }

        index = indexOf(state,ghostPos);
        if(index != -1){
            if(game.isEdible(index)) return true;
        }

        return false;
    }

    private boolean isDanger(int state){

        for(int  i =0; i < G.NUM_GHOSTS ;i++){
            if(game.getLairTime(i) != 0)
                break;
            int ghostDistance = game.getPathDistance(game.getCurGhostLoc(i),state);
            if(ghostDistance < 10 && !game.isEdible(i)) return true;
                    }
        return false;
    }

    public static <T> int indexOf(Integer needle, int[] haystack)
    {
        for (int i=0; i<haystack.length; i++)
        {
            if (haystack[i] == needle)
                     return i;
        }

        return -1;
    }

    private boolean isAnyEdible(){
        for(int i = 0; i < Game.NUM_GHOSTS; i++){
            if(game.isEdible(i)) return true;
        }
        return false;
    }

    @Override
    public int cost(Integer state, int action) {
                state = game.getNeighbour(state,action);
                int ghost = indexOf(state,ghostPos);
                if(ghost != -1){
                    boolean pathToDanger = !game.isEdible(ghost);
                    if(pathToDanger)
                        return 10000*game.getNumberOfNodes()+ 100;
                    else return 1;
                }
                int pill = game.getPillIndex(state);
                if(pill != -1 && game.checkPill(pill)) return 10;
                pill = game.getPowerPillIndex(state);
                if(pill != -1 && game.checkPowerPill(pill)) return 5;
            return 20;
    }
}
