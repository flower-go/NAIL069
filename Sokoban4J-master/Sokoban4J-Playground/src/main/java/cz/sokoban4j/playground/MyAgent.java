package cz.sokoban4j.playground;

import static java.lang.System.out;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import cz.sokoban4j.Sokoban;
import cz.sokoban4j.agents.ArtificialAgent;
import cz.sokoban4j.simulation.actions.compact.*;
import cz.sokoban4j.simulation.SokobanResult;
import cz.sokoban4j.simulation.actions.EDirection;

import cz.sokoban4j.simulation.board.compact.BoardCompact;

/**
 * The simplest Tree-DFS agent. Feel free to fool around here! You're in the PLAYGROUND after all!
 * @author Jimmy
 */
public class MyAgent extends ArtificialAgent {
	protected BoardCompact board;
	protected int searchedNodes;
	
	@Override
	protected List<EDirection> think(BoardCompact board) {
		// INIT SEARCH
		this.board = board;
		
		// DEBUG
		out.println("=================");
		out.println("===== BOARD =====");
		board.debugPrint();
		out.println("=================");
		
		// FIRE THE SEARCH

		Stats stats = new Stats();
		SokobanProblem c = new SokobanProblem(board);
		Node<BoardCompact, EDirection> n = AStar.search(c, stats);

		searchedNodes = 0;
		
		long searchStartMillis = System.currentTimeMillis();
		
		// List<EDirection> result = new ArrayList<EDirection>();
		// dfs(5, result); // the number marks how deep we will search (the longest plan we will consider)

		List<EDirection> result = createPath(n);

		long searchTime = System.currentTimeMillis() - searchStartMillis;
		
		out.println("SEARCH TOOK:   " + searchTime + " ms");
		out.println("NODES VISITED: " + searchedNodes);
		out.println("PERFORMANCE:   " + ((double)searchedNodes / (double)searchTime * 1000) + " nodes/sec");
		out.println("SOLUTION:      " + (result.size() == 0 ? "NOT FOUND" : "FOUND in " + result.size() + " steps"));
		
		/*if (result.size() > 0) {
			out.print("STEPS:         ");
			for (EDirection winDirection : result) {
				out.print(winDirection + " -> ");
			}
			out.println("BOARD SOLVED!");
		}*/
		out.println("=================");
		
		/*if (result.size() == 0) {
			throw new RuntimeException("FAILED TO SOLVE THE BOARD...");
		}*/
				
		return result;
	}

	private List<EDirection> createPath(Node<BoardCompact,EDirection> node){
		List<EDirection> result = new ArrayList<EDirection>();
		while(node != null) {
			result.add(node.action);
			node = node.parent;
		}
		Collections.reverse(result);
		return result;
	}

	private boolean dfs(int level, List<EDirection> result) {
		if (level <= 0) return false; // DEPTH-LIMITED
		
		++searchedNodes;
		
		// COLLECT POSSIBLE ACTIONS
		
		List<CAction> actions = new ArrayList<CAction>(4);
		
		for (CMove move : CMove.getActions()) {
			if (move.isPossible(board)) {
				actions.add(move);
			}
		}
		for (CPush push : CPush.getActions()) {
			if (push.isPossible(board)) {
				actions.add(push);
			}
		}
		
		// TRY ACTIONS
		for (CAction action : actions) {
			// PERFORM THE ACTION
			result.add(action.getDirection());
			action.perform(board);
			
			// CHECK VICTORY
			if (board.isVictory()) {
				// SOLUTION FOUND!
				return true;
			}
			
			// CONTINUE THE SEARCH
			if (dfs(level - 1, result)) {
				// SOLUTION FOUND!
				return true;
			}
			
			// REVERSE ACTION
			result.remove(result.size()-1);
			action.reverse(board);
		}
		
		return false;
	}
	
	public static void main(String[] args) {
		SokobanResult result;
		
		// VISUALIZED GAME
		result = Sokoban.playAgentLevel("../Sokoban4J/levels/Easy/level0001.s4jl", new MyAgent());   //  5 steps required
		result = Sokoban.playAgentLevel("../Sokoban4J/levels/Easy/level0002.1.s4jl", new MyAgent()); // 13 steps required
		result = Sokoban.playAgentLevel("../Sokoban4J/levels/Easy/level0002.2.s4jl", new MyAgent()); // 25 steps required
		result = Sokoban.playAgentLevel("../Sokoban4J/levels/Easy/level0002.3.s4jl", new MyAgent()); // 37 steps required

		// HEADLESS == SIMULATED-ONLY GAME
		//result = Sokoban.simAgentLevel("../Sokoban4J/levels/Easy/level0001.s4jl", new MyAgent());
		
		out.println("MyAgent result: " + result.getResult());
		
		System.exit(0);
	}

}
