package cz.sokoban4j.playground;

import cz.sokoban4j.simulation.actions.EDirection;
import cz.sokoban4j.simulation.actions.compact.CAction;
import cz.sokoban4j.simulation.actions.compact.CMove;
import cz.sokoban4j.simulation.actions.compact.CPush;
import cz.sokoban4j.simulation.board.compact.BoardCompact;
import cz.sokoban4j.simulation.board.compact.CTile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class SokobanProblem implements Problem<BoardCompact, EDirection> {

    BoardCompact compact;
    boolean[][] alive;
    List<Position> taargets;
    Random ran = new Random();


    public SokobanProblem(BoardCompact compact) {
        this.compact = compact;
        alive = new boolean[compact.width()][compact.height()];
        taargets = findTargets(compact, alive);
        bfs(alive, taargets, compact);
    }

    @Override
    public BoardCompact initialState() {
        return compact;
    }

    @Override
    public List<EDirection> actions(BoardCompact state) {
        List<EDirection> actions = new ArrayList<EDirection>(4);

        for (CPush push : CPush.getActions()) {
            if (push.isPossible(state)) {
                actions.add(push.getDirection());
            }
        }
        for (CMove move : CMove.getActions()) {
            if (move.isPossible(state)) {
                actions.add(move.getDirection());
            }
        }

        return actions;
    }

    @Override
    public BoardCompact result(BoardCompact state, EDirection action) {
        CMove m = new CMove(action);
        CPush p = new CPush(action);
        BoardCompact cloned = state.clone();
        if (p.isPossible(cloned)) {
            p.perform(cloned);
            if (!isOnDead(cloned)) {
                return cloned;
            }
        } else if (m.isPossible(cloned)) {
            m.perform(cloned);
            if (!isOnDead(cloned)) {

                return cloned;
            }
        }
        return null;
    }

    @Override
    public boolean isGoal(BoardCompact state) {
        return state.isVictory();
    }

    @Override
    public int cost(BoardCompact state, EDirection action) {

        int x = ran.nextInt(2) + 1;
        return x;
    }

    @Override
    public int estimate(BoardCompact state) {
        int estimate = 0;
        for (int x = 0; x < state.width(); x++) {
            for (int y = 0; y < state.height(); y++) {
                if (CTile.isSomeBox(state.tile(x, y))) {
                    estimate += findNearestTarget(x, y, state);
                }
            }

        }
        return state.boxInPlaceCount;
    }

    private int findNearestTarget(int x, int y, BoardCompact state) {
        int length = Integer.MAX_VALUE;
        for (Position p : taargets
                ) {
            if(!CTile.forBox(CTile.getBoxNum(state.tile(x,y)),state.tile(p.x, p.y))) continue;
            int nove = Math.abs(p.x - x) + Math.abs(p.y - y);
            if (nove < length) {
                length = nove;
            }
        }
        return length;
    }

    private boolean[][] bfs(boolean[][] finded, List<Position> nextGeneration, BoardCompact board) {

        if (nextGeneration.size() == 0) return finded;
        List<Position> forFinding = new ArrayList<>();
        for (Position p : nextGeneration
                ) {

            forFinding.addAll(findAscentors(p, board.width(), board.height(), board, finded));
        }

        return bfs(finded, forFinding, board);
    }

    private List<Position> findAscentors(Position p, int wMax, int hMax, BoardCompact board, boolean[][] finded) {
        List<Position> result = new ArrayList<>();

        // x+1,y
        int xN = p.x + 1;
        int yN = p.y;
        int xP = p.x + 2;
        int yP = p.y;
        if (isOk(xN, yN, xP, yP, wMax, hMax, board, finded)) {
            result.add(new Position(xN, yN));
            finded[xN][yN] = true;
        }
        // x-1, y
        xN = p.x - 1;
        yN = p.y;
        xP = p.x - 2;
        yP = p.y;
        if (isOk(xN, yN, xP, yP, wMax, hMax, board, finded)) {
            result.add(new Position(xN, yN));
            finded[xN][yN] = true;
        }

        //x, y+1
        xN = p.x;
        yN = p.y + 1;
        xP = p.x;
        yP = p.y + 2;
        if (isOk(xN, yN, xP, yP, wMax, hMax, board, finded)) {
            result.add(new Position(xN, yN));
            finded[xN][yN] = true;
        }

        //x, y - 1
        xN = p.x;
        yN = p.y - 1;
        xP = p.x;
        yP = p.y - 2;
        if (isOk(xN, yN, xP, yP, wMax, hMax, board, finded)) {
            result.add(new Position(xN, yN));
            finded[xN][yN] = true;
        }
        return result;
    }

    // is possible to get from pushplace to neighbour
    private boolean isOk(int xNeighbour, int yNeighbour, int xPushPlace, int yPushPlace, int wMax, int hMax, BoardCompact board, boolean[][] finded) {
        if (!finded[xNeighbour][yNeighbour] &&
                xNeighbour < wMax
                && xPushPlace < wMax
                && yNeighbour < hMax
                && yPushPlace < hMax
                && xNeighbour >= 0
                && xPushPlace >= 0
                && yNeighbour >= 0
                && yPushPlace >= 0
                && (CTile.isWalkable(board.tile(xNeighbour, yNeighbour)) || CTile.isSomeBox(board.tile(xNeighbour, yNeighbour)))
                && (CTile.isWalkable(board.tile(xPushPlace, yPushPlace)) || CTile.isSomeBox(board.tile(xPushPlace, yPushPlace)))) {
            return true;
        }
        return false;
    }

    private boolean isOkToUse(int x, int y, int wMax, int hMax, BoardCompact board) {
        if (
                x < wMax &&
                        x >= 0 &&
                        y < hMax &&
                        y >= 0 &&
                        alive[x][y] &&
                        CTile.isWall(board.tile(x, y))
                ) {
            return true;
        }
        return false;
    }

    private List<Position> findTargets(BoardCompact board, boolean[][] alive) {
        List<Position> result = new ArrayList<>();
        int[][] tiles = board.tiles;
        for (int x = 0; x < board.width(); x++) {
            for (int y = 0; y < board.height(); y++) {
                if (CTile.forSomeBox(board.tile(x, y))) {
                    result.add(new Position(x, y));
                    alive[x][y] = true;
                }
            }
        }
        return result;
    }

    private boolean isOnDead(BoardCompact board) {
        for (int x = 0; x < board.width(); x++) {
            for (int y = 0; y < board.height(); y++) {
                if (CTile.isSomeBox(board.tile(x, y)) && !alive[x][y]) {
                    return true;
                }
            }
        }
        return false;
    }

    class Position {
        public Position(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int x;
        public int y;
    }
}