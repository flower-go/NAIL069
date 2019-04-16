package game.controllers.pacman.examples;

import game.PacManSimulator;
import game.controllers.ghosts.game.GameGhosts;
import game.controllers.pacman.PacManHijackController;
import game.core.G;
import game.core.Game;
import game.core.GameView;
import jdk.jshell.spi.ExecutionControl;

import java.awt.*;

public final class MyPacMan extends PacManHijackController
{
	@Override
	public void tick(Game game, long timeDue) {
		
		// Code your agent here.
		Problem<Integer> p = new MazeProblem(game);
		Node<Integer> result = UCS.search(p);

		if(result != null){
			int[] res = {returnDirection(result)};
			pacman.set(game.getNextPacManDir(game.getTarget(game.getCurPacManLoc(),res,true, Game.DM.PATH),true, Game.DM.PATH));
			int current = game.getCurPacManLoc();
			//GameView.addPoints(game,Color.CYAN,game.getInitialGhostsPosition());
			//int[] ghostDistances = new int[]{ game.getPathDistance(game.getCurPacManLoc(), game.getCurGhostLoc(0)), game.getGhostPathDistance(0,game.getCurPacManLoc())};
			//GameView.addText(0, 10, Color.YELLOW, "Ghost distances: " + ghostDistances[0] + ", " + ghostDistances[1]);
			//GameView.addText(0,20,Color.YELLOW, String.valueOf(result.cost));
		}
		else {
			//GameView.addText(0,20, Color.blue, "NULL");

			pacman.set(0);
		}



	}
		// Dummy implementation: move in a random direction.  You won't live long this way,
		//int[] directions=game.getPossiblePacManDirs(false);
		//pacman.set(directions[G.rnd.nextInt(directions.length)]);


	private int returnDirection(Node<Integer> end){
		Node<Integer> second = end;
		while(end.parent != null)
		{
			second = end;
			end = end.parent;
		}
		return second.state;
	}
		
	public static void main(String[] args) {
		PacManSimulator.play(new MyPacMan(), new GameGhosts());
	}
}