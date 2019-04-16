package conquest.bot.playground;

import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import conquest.bot.BotParser;
import conquest.bot.fight.FightSimulation.FightAttackersResults;
import conquest.bot.state.*;
import conquest.engine.Config;
import conquest.engine.RunGame;
import conquest.game.FightMode;
import conquest.game.world.Continent;
import conquest.game.world.Region;
import conquest.utils.Util;
import conquest.view.GUI;
import javafx.util.Pair;
import jdk.jshell.spi.ExecutionControl;


import static java.util.stream.Collectors.groupingBy;

public class MyBot extends GameBot
{
	Random rand = new Random();

	FightAttackersResults attackResults;
    Pair<List<PlaceCommand>,List<MoveCommand>> thisTurn = new Pair<>(null,null);
    double winProbability = 0.8;

	public MyBot() {
		attackResults = FightAttackersResults.loadFromFile(Util.file(
				"../Conquest-Bots/FightSimulation-Attackers-A200-D200.obj"));
	}

	@Override
	public void setGUI(GUI gui) {
	}

	// Code your bot here.

	//
	// This is a dummy implemementation that moves randomly.
	//

	// Choose a starting region.

	@Override
	public ChooseCommand chooseRegion(List<Region> choosable, long timeout) {

	    if(choosable.size() > 10)
        {
            //sort by continents
            choosable.sort(new Comparator<Region>() {
                @Override
                public int compare(Region o1, Region o2) {
                    int a = getPreferredContinentPriority(o1.continent);
                    int b = getPreferredContinentPriority(o2.continent);
                    return a > b ? +1 : a < b ? -1 : 0;
                }
            });

            //dotykaji se v australii
            if(!areNeighbours(choosable.get(0),choosable.get(1)))
            {
                return new ChooseCommand(choosable.get(0));
            }

            //dotykaji se v j. americe
            if(!areNeighbours(choosable.get(2),choosable.get(3)))
            {
                return new ChooseCommand(choosable.get(2));
            }

            //Siam and Indonesia
            Region preffered = getPairs(Region.Siam, Region.Indonesia, choosable);
            if( preffered != null)
            {
                return new ChooseCommand(preffered);
            }

            //Central America vs Venezuela
            preffered = getPairs(Region.Central_America,Region.Venezuela, choosable);
            if( preffered != null)
            {
                return new ChooseCommand(preffered);
            }

            //North Africa vs Brazil
            preffered = getPairs(Region.North_Africa, Region.Brazil, choosable);
            if( preffered != null)
            {
                return new ChooseCommand(preffered);
            }

            if(choosable.contains(Region.Siam))
            return new ChooseCommand(Region.Siam);
            else return new ChooseCommand(choosable.stream().filter(x -> x.continent == Continent.Australia).findFirst().orElse(Region.Argentina));
        }
        else{
	        Region select = choosable.stream().filter(x -> x.continent == Continent.Europe).findFirst().orElse(choosable.get(0));
	        return new ChooseCommand(select);
        }

	}

    // Decide where to place armies this turn.
    // state.me.placeArmies is the number of armies available to place.
    @Override
    public List<PlaceCommand> placeArmies(long timeout) {
        PlayerState me = state.players[state.me];
        List<Region> mine = new ArrayList<Region>(me.regions.keySet());
        int numRegions = mine.size();

        // CLONE REGIONS OWNED BY ME
        List<RegionState> mineStates = new ArrayList<RegionState>(me.regions.values());
        System.out.format("mybot: placing %d armies\n", me.placeArmies);

        if(me.placeArmies == 2)
        {
            List<PlaceCommand> ret = new ArrayList<PlaceCommand>();
            Pair<RegionState,RegionState> r = neighbourCanAttack(mineStates);
            if(r != null)
            {
                ret.add(new PlaceCommand(r.getKey().region,2));
                List<MoveCommand> retM = new ArrayList<>();
                retM.add(new MoveCommand(r.getKey(),r.getValue(),r.getValue().armies - 1));
                thisTurn = new Pair<>(ret,retM);
            }
            else ret.add(new PlaceCommand(mine.stream().filter(x -> x.continent == Continent.Europe).findFirst().orElse(mine.get(0)),2));

            return ret;
        }

        System.out.format("compute ACTIONS");
        // COMPUTE ACTIONS
        computeActions(mineStates, me.placeArmies);

        if(thisTurn.getKey() != null)
        {
            return thisTurn.getKey();
        }
        // RANDOM PLACING - NEMELO BY NASTAT
        else{
            System.out.format("compute ACTIONS RANDOMLY");
            List<PlaceCommand> ret = new ArrayList<PlaceCommand>();
            int[] count = new int[numRegions];
            for (int i = 0 ; i < me.placeArmies ; ++i) {
                int r = rand.nextInt(numRegions);
                count[r]++;
            }

            ret = new ArrayList<PlaceCommand>();
            for (int i = 0 ; i < numRegions ; ++i)
                if (count[i] > 0)
                    ret.add(new PlaceCommand(mine.get(i), count[i]));
            return ret;
        }
    }

    private void computeActions(List<RegionState> mineStates, int placeArmies){

	    // DEEP COPY OF MINESTATES
        Map<RegionState,RegionStateM> copyS =  copyStates(mineStates);
        // list of all regions which were not attacked
        List<RegionState> unused = new ArrayList<RegionState>(Arrays.asList(state.regions));

        // results of computations
        List<PlaceCommand> resultPlace = new ArrayList<PlaceCommand>();
        List<MoveCommand> move = new ArrayList<>();


	    //My opponent's continets I can destroy
        List<Pair<RegionState,RegionState>> continents = CanDeleteContinent(mineStates, unused);
        placeArmies =  computeContinents(copyS, unused, continents, placeArmies, resultPlace, move);

        //Continents I can win
        List<RegionState> canWin = CanGainContinent(mineStates, unused);
        placeArmies = computeWinContinents(copyS,unused,canWin,placeArmies,resultPlace,move);

        //lonely regions
        List<Pair<RegionState,RegionState>> lonely = lonelyRegion(unused, copyS);
        placeArmies =  computeLonely(copyS,unused,lonely,placeArmies,resultPlace,move);

        //dobytí v mém kontinentu

        List<RegionState> conquest = regionInContinentWhenIam(copyS,unused);
        placeArmies = computeConquestInContinent(copyS,unused,conquest,placeArmies,resultPlace,move);

        resultPlace = resultPlace.size() == 0 ? null : resultPlace;
        move = move.size() == 0 ? null : move;
        thisTurn = new Pair<>(resultPlace,move);


        //compute how to play
        //todo potrebuju seznam placeComands a movecommands
        //thisTurn = getTransfers(continents, canWin, lonely, conquest, me.placeArmies, mineStates);
    }

    private int computeContinents( Map<RegionState,RegionStateM>  copyS,
                                    List<RegionState>  unused,
                                    List<Pair<RegionState,RegionState>> continents,
                                    int available,
                                    List<PlaceCommand> resultPlace,
                                    List<MoveCommand> move
                                    )
    {
        for (Pair<RegionState,RegionState> pair:continents
                ) {
            //int needed = pair.getValue().armies*2;
            int needed = getRequiredSoldiersToConquerRegion(pair.getValue().armies,winProbability,copyS.get(pair.getKey()).armies + available);
            int add = needed - copyS.get(pair.getKey()).armies+1;
            if(add <= available)
            {
                if(add > 0)
                {
                    resultPlace.add(new PlaceCommand(pair.getKey().region,add));
                    copyS.get(pair.getKey()).armies += add;
                    available = available - add;
                }
                move.add(new MoveCommand(pair.getKey().region,pair.getValue().region,needed));

                unused.remove(pair.getValue());

                copyS.get(pair.getKey()).armies -= needed;
            }
        }
        return available;
    }


    private int computeWinContinents(Map<RegionState,RegionStateM>  copyS,
                                     List<RegionState>  unused,
                                     List<RegionState> wantRegions,
                                     int available,
                                     List<PlaceCommand> resultPlace,
                                     List<MoveCommand> move
    ){
        for (RegionState continent: wantRegions
                ) {
            for (RegionState region:continent.neighbours
                    ) {
                if(copyS.keySet().contains(region))
                {
                    int needed = getRequiredSoldiersToConquerRegion(continent.armies,winProbability,copyS.get(region).armies + available);
                    int add = needed - copyS.get(region).getArmies()+1;
                    if(add <= available)
                    {
                        if(add > 0)
                        {
                            resultPlace.add(new PlaceCommand(region.region,add));
                            copyS.get(region).armies += add;
                            available = available - add;
                        }

                        if(region.armies + add - needed < 1)
                        {
                            break;
                        }
                        move.add(new MoveCommand(region,continent,needed));

                        unused.remove(continent);

                        copyS.get(region).armies -= needed;
                    }
                }

            }
        }
        return available;
    }

    private int computeLonely(Map<RegionState,RegionStateM>  copyS,
    List<RegionState>  unused,
    List<Pair<RegionState,RegionState>> lonelies,
    int available,
    List<PlaceCommand> resultPlace,
    List<MoveCommand> move
    ){

        for (Pair<RegionState,RegionState> pair:lonelies
                ) {
            if(!copyS.keySet().contains(pair.getKey())) continue;
            if(!unused.contains(pair.getValue())) continue;
            int needed = getRequiredSoldiersToConquerRegion(pair.getValue().armies,winProbability,copyS.get(pair.getKey()).armies + available);
            int add = needed - copyS.get(pair.getKey()).getArmies()+1;
            if(add <= available )
            {
                if(add > 0)
                {
                    resultPlace.add(new PlaceCommand(pair.getKey().region,add));
                    copyS.get(pair.getKey()).armies += add;
                    available = available - add;
                }

                move.add(new MoveCommand(pair.getKey().region,pair.getValue().region,needed));


                copyS.get(pair.getKey()).armies -= needed;
                unused.remove(pair.getValue());
            }
        }
	    return available;
    }

    private int computeConquestInContinent(Map<RegionState,RegionStateM>  copyS,
                                           List<RegionState>  unused,
                                           List<RegionState> conquests,
                                           int available,
                                           List<PlaceCommand> resultPlace,
                                           List<MoveCommand> move)
    {
        List<RegionState> pouzitelne = copyS.keySet().stream().filter( x -> copyS.get(x).armies > 1).collect(Collectors.toList());

        for (RegionState continent: conquests
                ) {
            if(!unused.contains(continent)) continue;
            for (RegionState region:continent.neighbours
                    ) {
                if(pouzitelne.contains(region))
                {
                    int needed = getRequiredSoldiersToConquerRegion(continent.armies,winProbability,copyS.get(region).armies + available);
                    int add = needed - copyS.get(region).armies+1;
                    if(add <= available)
                    {
                        if(add > 0)
                        {
                            resultPlace.add(new PlaceCommand(region.region,add));
                            copyS.get(region).armies += add;
                            available = available - add;
                        }

                        move.add(new MoveCommand(region,continent,needed));


                        copyS.get(region).armies -= needed;
                        unused.remove(continent);
                    }
                }

            }
        }
        return available;
    }

    private Map<RegionState,RegionStateM> copyStates(List<RegionState> mineStates)
    {
        Map<RegionState, RegionStateM> result = new HashMap<RegionState,RegionStateM>();
        for (RegionState s:mineStates
             ) {
            result.put(s,new RegionStateM(s.armies,s));
        }
        return result;
    }

    private Region getPairs(Region preffered, Region second, List<Region> choosable)
    {
        List<Region> regions = choosable.stream().filter(x -> (x.equals(second) || x.equals(preffered))).collect(Collectors.toList());;
        if(regions.size() == 1 && regions.get(0).equals(preffered))
        {
            return regions.get(0);
        }
        return null;
    }

	private boolean areNeighbours(Region a, Region b)
    {
        return a.getNeighbours().contains(b);
    }

    public int getPreferredContinentPriority(Continent continent) {
        switch (continent) {
            case Australia:     return 1;
            case South_America: return 2;
            case North_America: return 3;
            case Europe:        return 3;
            case Africa:        return 3;
            case Asia:          return 3;
            default:            return 7;
        }
    }

	private List<RegionState> regionInContinentWhenIam(Map<RegionState,RegionStateM> copyS,
                                                  List<RegionState> unused)
    {
        List<RegionState> result = new ArrayList<>();
        Map<Object, Long> grouped =  copyS.keySet().stream().collect(
                groupingBy(
                        x -> x.region.continent, Collectors.counting()
                )
        );
        List<RegionState> pouzitelne = copyS.keySet().stream().filter( x -> copyS.get(x).armies > 1).collect(Collectors.toList());

        for (RegionState g:pouzitelne
             ) {
            for (RegionState n:g.neighbours
                 ) {
                if(n.owner != state.me && grouped.keySet().contains(n.region.continent))
                {
                    result.add(n);
                }
            }
        }
        return  result;
    }

	private List<Pair<RegionState,RegionState>> lonelyRegion(
            List<RegionState> unused,
            Map<RegionState,RegionStateM> copyS
    )
    {
        List<Pair<RegionState,RegionState>> result = new ArrayList<>();
        List<RegionState> pouzitelne = copyS.keySet().stream().filter( x -> copyS.get(x).armies > 1).collect(Collectors.toList());

        for (RegionState region: pouzitelne
             ) {
            for (RegionState n:region.neighbours
                 ) {
                if(n.owner != 0 && n.owner != state.me)
                {
                    if(unused.contains(n)) continue;
                    boolean lonely = true;
                    for (RegionState nn:n.neighbours
                         ) {

                        if(nn.owner != 0 && nn.owner != state.me){
                            lonely = false;
                            break;
                        }
                    }
                    if(lonely)
                    {
                        result.add(new Pair<RegionState,RegionState>(region,n));
                    }
                }
            }
        }

        return result;
    }


    private int getRequiredSoldiersToConquerRegion(int defenders, double winProbability, int max) {

        for (int a = 1; a <= max; ++a) {
            double chance = attackResults.getAttackersWinChance(a, defenders);
            if (chance >= winProbability) {
                return a;
            }
        }
        return Integer.MAX_VALUE;
    }

    private List<RegionState> CanGainContinent(List<RegionState> mineStates, List<RegionState> unused)
    {
        List<RegionState> result = new ArrayList<>();

        Map<Object, Long> grouped =  mineStates.stream().collect(
                groupingBy(
                        x -> x.region.continent, Collectors.counting()
                )
        );

        for (Map.Entry<Object, Long> entry : grouped.entrySet())
        {
            Continent c = (Continent)entry.getKey();
            if(entry.getValue() == c.getRegions().size() - 1)
            {
                List<RegionState> regions = (mineStates.stream().filter(x -> x.region.continent == c).collect(Collectors.toList()));
                RegionState to = state.region(c.getRegions().stream().filter(x -> !regions.contains(x)).findFirst().orElse(null));
                if(unused.contains(to))
                {
                    result.add(to);
                }
            }
        }
        return result;
    }

	private List<Pair<RegionState,RegionState>> CanDeleteContinent(
	        List<RegionState> mineStates,
            List<RegionState> unused
    )
    {
        List<Pair<RegionState,RegionState>> result = new ArrayList<>();

        //ma nejake kontinenty?
        if(state.players[state.opp].continents.size() == 0) return result;

        //sousedim s nimi?

        Map<Continent, ContinentState> continents = state.players[state.opp].continents;

        for (RegionState region:mineStates
             ) {
            for (RegionState n:region.neighbours
                 ) {
                if(continents.containsKey(n.region.continent))
                {
                    // TODO vice moznosti
                    result.add(new Pair<>(region,n));
                }
            }
        }
        return result;
    }

	private Pair<RegionState,RegionState> neighbourCanAttack(List<RegionState> mine)
	{
        for (RegionState r:mine
             ) {
            for (RegionState n:r.neighbours
                 ) {
                if(n.owner == state.opp && n.armies != 0)
                {
                    return new Pair<>(r,n);
                }
            }
        }
		return null;
	}

	// Decide where to move armies this turn.

	@Override
	public List<MoveCommand> moveArmies(long timeout) {

        List<MoveCommand> ret = new ArrayList<MoveCommand>();
        if(state.getRoundNumber() == 1) return ret ;

        if(thisTurn.getValue() != null)
            return thisTurn.getValue();

        return ret;

	}

	private List<Pair<RegionState,RegionState>> conquerRest(
            int armies,
            List<RegionState> possible,
            List<RegionState> mineStates)
    {
        List<Pair<RegionState,RegionState>> result = new ArrayList<>();
        if(armies <= 0) return null;
        if(mineStates.size() == 0) return null;
        for (RegionState r:possible
             ) {
            for (RegionState n: r.neighbours
                 ) {
                if(n.owner == state.me && mineStates.contains(n))
                {
                    result.add(new Pair<>(r,n));
                }
            }
        }
        return result;
    }

	private void conquerRandomly(List<MoveCommand> commands, int armies){
        for (RegionState rs : state.players[state.me].regions.values()) {
            int count = rand.nextInt(armies);
            if (count > 0) {
                List<Region> neighbors = rs.region.getNeighbours();
                Region to = neighbors.get(rand.nextInt(neighbors.size()));
                commands.add(new MoveCommand(rs.region, to, rs.armies - 1));
            }
        }
    }


	public static void runInternal() {
		Config config = new Config();

		config.bot1Init = "internal:conquest.bot.playground.MyBot";

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

	public static void runExternal() {
		BotParser parser = new BotParser(new MyBot());
		parser.setLogFile(new File("./MyBot.log"));
		parser.run();
	}

	public static void main(String[] args)
	{
		runInternal();

		//JavaBot.exec(new String[]{"conquest.bot.custom.AggressiveBot", "./AggressiveBot.log"});
	}

	class RegionStateM
    {
        private int armies;
        private RegionState regionState;
        public RegionStateM(int armies, RegionState regionState)
        {
            this.armies = armies;
            this.regionState = regionState;
        }


        public int getArmies() {
            return armies;
        }

        public void setArmies(int armies) {
            this.armies = armies;
        }

        public RegionState getRegionState() {
            return regionState;
        }

        public void setRegionState(RegionState regionState) {
            this.regionState = regionState;
        }
    }

}
