package blackDragon;

import battlecode.common.*;

/** The example funcs player is a player meant to demonstrate basic usage of the most common commands.
 * Robots will move around randomly, occasionally mining and writing useless messages.
 * The HQ will spawn soldiers continuously. 
 */


public class RobotPlayer {
	private static final String RC2Cipher = null;
	private static RobotController rc;
	private static MapLocation rallyPoint;

	public static void run(RobotController myRC) {
		rc = myRC;
		MapLocation nme = null;
		MapLocation[] MapEncamp = rc.senseAllEncampmentSquares();
		Team friend = rc.getTeam();
		Team enemy = rc.getTeam().opponent();
		Team nuteral = rc.getTeam().NEUTRAL;
		MapLocation home = rc.senseHQLocation();
		rallyPoint = findRallyPoint();
		while (true) {
			try {
				if (rc.getType() == RobotType.SOLDIER) {
					if (rc.isActive()) {
						MapLocation myLoc = rc.getLocation();
						int smallestVal = Integer.MAX_VALUE;
						int smallestIdx = -1;
						
						if(Clock.getRoundNum() < 200)
						{
						for (int i = 0; i < MapEncamp.length; i++){
							MapLocation r = MapEncamp[i];
							int val = Math.abs(r.x - myLoc.x) + Math.abs(r.y- myLoc.y);
							
							if (val < smallestVal) {
								smallestVal = val;
								smallestIdx = i;
							}
						}
						MapLocation nearest = MapEncamp[smallestIdx];
						Direction dir = myLoc.directionTo(nearest);
						if (rc.canMove(dir)) {
							moveOrDefuse(dir);
							rc.captureEncampment(RobotType.ARTILLERY);
							}
						}
						else {
							if(rc.getEnergon() < 1000){
								goToLocation(rallyPoint);
							}
							else if(rc.getEnergon() > 1000)
							{
								goToLocation(rc.senseEnemyHQLocation());
							}
						}
					}
				}
				else if (rc.getType() == RobotType.HQ) {
					if (rc.isActive()) {
						if(Clock.getBytecodeNum() > 200)
						{
							Direction north = Direction.NORTH;
							Direction south = Direction.SOUTH;
							Direction east = Direction.EAST;
							Direction west = Direction.WEST;
							Direction north_west = Direction.NORTH_WEST;
							Direction north_east = Direction.NORTH_EAST;
							Direction south_east = Direction.SOUTH_EAST;
							Direction south_west = Direction.SOUTH_WEST;
							Direction[] compass = {north, south, east, west, north_west, north_east, south_west, south_east};
							TerrainTile x = rc.senseTerrainTile(home);
							for(int i = 0; i<8;i++ )
							{
								Direction point = compass[i];
								if (rc.canMove(point))
									rc.spawn(point);
							}
						}
						else if(Clock.getBytecodeNum()<201)
						{
						rc.researchUpgrade(Upgrade.FUSION);
						rc.researchUpgrade(Upgrade.NUKE);
						}
					}
				} 
				else if (rc.getType() == RobotType.ARTILLERY){
					if(rc.isActive()){
					Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class,1000000,rc.getTeam().opponent());
					int closestDist = 1000000;
						MapLocation closestEnemy=null;
						for (int i=0;i<enemyRobots.length;i++){
							Robot arobot = enemyRobots[i];
							RobotInfo arobotInfo = rc.senseRobotInfo(arobot);
							int dist = arobotInfo.location.distanceSquaredTo(rc.getLocation());
							if (dist<closestDist){
								closestDist = dist;
								closestEnemy = arobotInfo.location;
							}
						}
						int dist = rc.getLocation().distanceSquaredTo(closestEnemy);
						if (dist>0&&rc.isActive()){
							Direction dir = rc.getLocation().directionTo(closestEnemy);
							int[] directionOffsets = {0,1,-1,2,-2};
							Direction lookingAtCurrently = dir;
							lookAround: for (int d:directionOffsets){
								lookingAtCurrently = Direction.values()[(dir.ordinal()+d+8)%8];
								if(rc.canMove(lookingAtCurrently)){
									break lookAround;
								}
							}
							rc.move(lookingAtCurrently);
						}
					}
				}
				// End turn
			} catch (Exception e) {
				System.out.println("caught exception before it killed us:");
				e.printStackTrace();
			}
		}
	}
	private static void moveOrDefuse(Direction dir) throws GameActionException{
		MapLocation ahead = rc.getLocation().add(dir);
		if(rc.senseMine(ahead)!= null){
			rc.defuseMine(ahead);
		}else{
			rc.move(dir);			
		}
	}
	private static MapLocation findRallyPoint() {
		MapLocation enemyLoc = rc.senseEnemyHQLocation();
		MapLocation ourLoc = rc.senseHQLocation();
		int x = (enemyLoc.x+3*ourLoc.x)/4;
		int y = (enemyLoc.y+3*ourLoc.y)/4;
		MapLocation rallyPoint = new MapLocation(x,y);
		return rallyPoint;
	}
	private static void goToLocation(MapLocation whereToGo) throws GameActionException {
		int dist = rc.getLocation().distanceSquaredTo(whereToGo);
		if (dist>0&&rc.isActive()){
			Direction dir = rc.getLocation().directionTo(whereToGo);
			int[] directionOffsets = {0,1,-1,2,-2};
			Direction lookingAtCurrently = dir;
			lookAround: for (int d:directionOffsets){
				lookingAtCurrently = Direction.values()[(dir.ordinal()+d+8)%8];
				if(rc.canMove(lookingAtCurrently)){
					break lookAround;
				}
			}
			rc.move(lookingAtCurrently);
		}
	}
}




