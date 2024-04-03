
import battleship.*;

import java.awt.Point;
import java.util.*;

/**
 * <h1></h1>
 *
 * <p>
 *  A Sample random shooter - Takes no precaution on double shooting and has no strategy once
 *  a ship is hit - This is not a good solution to the problem!
 * </p>
 *
 * <p>
 * I Luca Quacquarelli, Keegan Andrus, 000838997, 000880159, certify that this material is my original work.
 * No other person's work has been used without suitable acknowledgment and I have not made my work available to anyone else.
 * </p>
 *
 * @author mark.yendt@mohawkcollege.ca (Dec 2021)
 * @author Keegan Andrus <keegan.Andrus@mohawkcollege.ca> (Apr 2024)
 * @author Luca Quacquarelli <luca.quacquarelli@mohawkcollege.ca> (Apr 2024)
 * @version JDK 21
 * @package COMP-10205 – Assignment#6 Battleship Strategy
 */

public class CoolBot2024 implements BattleShipBot {
    private int gameSize;
    private BattleShip2 battleShip;
    private Random random;

    private int sunkShipCount;

    private List<Integer> remainingShips = new ArrayList<Integer>();

    private List<int[]> targetQueue = new ArrayList<int[]>();
    private Set<Point> shotTaken = new HashSet<>();



    private int getMinShipRemaining(){
        int minimum = remainingShips.get(0);
        for (int i = 1; i < remainingShips.size(); i++) {
            if (minimum > remainingShips.get(i))
                minimum = remainingShips.get(i);
        }
        return minimum;
    }


    private void changeGrid(){
        // this will regenerate the remaining target grid, there is some room for improvement, but im to lazy to do that atm
        // we need to make a prune function to check all queue for repeat moves, these will need to be removed
        boolean resume = true;
        int spacing = getMinShipRemaining();
        int[] nextshot = targetQueue.get(0);
        targetQueue.clear();
        nextshot[0] = nextshot[0]-(spacing-1) >= 0? nextshot[0]-(spacing-1): 0;

        for (int i = nextshot[0]; i <= gameSize; i++) {
            int target = resume?
                    nextshot[1] - (spacing-1) >=0 ? 0 : nextshot[1] - (spacing-1)
                    :
                    i%spacing;
            resume = false;

            for (int j = target; j <= gameSize; j+= (spacing)) {
                int[] tmp = new int[]{i,j};
                this.targetQueue.add(tmp);
            }
        }
    }
    private void makeGrid(){
        int spacing = getMinShipRemaining();
        for (int i = 0; i <= gameSize; i++) {
            for (int j = i%spacing; j <= gameSize; j+= (spacing)) {
                int[] tmp = new int[]{i,j};
                //this.targetQueue.add(tmp);

                if(isTargetValid(tmp)) {
                    targetQueue.add(tmp);
                }
            }
        }
    }// end makeGrid()

    private boolean isTargetValid(int[] tmp) {
        Point shot = new Point(tmp[0], tmp[1]);
        return isPointValid(shot.x, shot.y) && !shortAttempted(shot);
    }// end isTargetValid()

    private boolean shortAttempted(Point shot) {
        return shotTaken.contains(shot);
    }// end shotAttempted()


    private boolean isPointValid(int x, int y) {
        return x >= 0 && x < gameSize && y >= 0 && y < gameSize;
    }// end isPointValid()


    private void addTargetHit(Point shot) {
        int[][] shotDirections = {{-1,0}, {1,0}, {0,-1}, {0,1}};

        for (int[] direction : shotDirections) {
            int nextX = shot.x + direction[0];
            int nextY = shot.y + direction[1];
            if(isPointValid(nextX, nextY) && !shotTaken.contains(new Point(nextX, nextY))) {
                targetQueue.add(new int[]{nextX, nextY});
            }
        }
    }

    private void updateOnSunkShip() {
        int shrinkCount = battleShip.numberOfShipsSunk();
        if (shrinkCount > sunkShipCount) {
            sunkShipCount = shrinkCount;
        }
    }



    /**
     * Constructor keeps a copy of the BattleShip instance
     * Create instances of any Data Structures and initialize any variables here
     * @param b previously created battleship instance - should be a new game
     */
    @Override
    public void initialize(BattleShip2 b) {

        remainingShips = new ArrayList<Integer>();
        targetQueue = new ArrayList<int[]>();


        battleShip = b;
        gameSize = b.BOARD_SIZE;
        System.out.println(b);



        // Need to use a Seed if you want the same results to occur from run to run
        // This is needed if you are trying to improve the performance of your code

        random = new Random(0xAAAAAAAA);   // Needed for random shooter - not required for more systematic approaches
        sunkShipCount = 0;


        // keep this it
        if(remainingShips.isEmpty()){
            for(int ship: battleShip.getShipSizes()) {
                remainingShips.add(ship);
            }
        }
        sunkShipCount = battleShip.numberOfShipsSunk();



        // run at start
        makeGrid();


//
//        for(int[] arr: targetQueue){
//            System.out.println(Arrays.toString(arr));
//        }
//        System.out.println("\n\n");
//        for (int i = 0; i < 12; i++) {
//            this.targetQueue.remove(0);
//        }
//        for(int[] arr: targetQueue){
//            System.out.println(Arrays.toString(arr));
//        }
//        System.out.println("\n\n");
//
//        System.out.println(remainingShips);
//
//
//        // this is a test thing to remove the smallest item from the list this is for testing only
//        remainingShips.removeIf(e -> e.equals(getMinShipRemaining()));
//        remainingShips.removeIf(e -> e.equals(getMinShipRemaining()));
//        System.out.println(remainingShips);
//
//        System.out.println(Arrays.toString(targetQueue.get(0)));
//
//
//
//        // on ship kill run this to regenerate queue
//        changeGrid();
//
//        //testing stuff
//        for(int[] arr: targetQueue){
//            System.out.println(Arrays.toString(arr));
//        }



    }// end initialize()


    /**
     * Create a random shot and calls the battleship shoot method
     * Put all logic here (or in other methods called from here)
     * The BattleShip API will call your code until all ships are sunk
     */
    @Override
    public void fireShot() {

        if(!targetQueue.isEmpty()) {
            int[] next = targetQueue.remove(0);
            Point shot = new Point(next[0], next[1]);

            if(!shotTaken.contains(shot)) {
                boolean hit = battleShip.shoot(shot);
                shotTaken.add(shot);

                if (hit) {
                    addTargetHit(shot);
                }

                updateOnSunkShip();
            }
        }
        // Will return true if we hot a ship

    }// end fireShot()



    /**
     * Authorship of the solution - must return names of all students that contributed to
     * the solution
     * @return names of the authors of the solution
     */
    @Override
    public String getAuthors() {
        return "Mark Yendt (CSAIT Professor\nLuca Quacquarelli (COMP-10205 Student\nKeegan (COMP-10205 Student)";
    }// end getAuthors()
}// end CoolBot2024
