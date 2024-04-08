import battleship.*;
import java.awt.Point;
import java.util.*;
import static java.lang.System.exit;

/**
 * <h1>Battleship Strategy</h1>
 *
 * <p>
 *  The CoolBot2024 program in a statistical bot for playing a modified version of the classic battleship game.
 *  The game is designed to play the battleship game on a 15x15 grid, And it searches for and destroys ships without wasting shots.
 *  The bot can adjust its strategy based on the situation to focus on areas around successful hits and switches tactics if initial plans don't workout.
 *  The bot also keeps track of its performance and make sure it plays within the set time limit.
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

    /** Holds the size of the game board. **/
    private int gameSize;

    /** Tracks the worst game performance based on the shots taken. **/
    private int worstGame = 0;

    /** Tracks the best game performance based on the shots taken. **/
    private int bestGame = 10000;

    /** An instant of the battleship. **/
    private BattleShip2 battleShip;

    /** For generating random values if needed. **/
    private Random random;

    /** Counter for the number of ships sunk. **/
    private int sunkShipCount;

    /** Keep track of the sizes of remaining ships. **/
    private List<Integer> remainingShips;

    /** Queue for target points to shoot at. **/
    private List<int[]> targetQueue;

    /** Queue for targets in the kill mode. **/
    private List<int[]> killQueue;

    /** delete the hits, It's for testing **/
    private List<int[]> hits;

    /** This tracks the last shot made. **/
    private int[] lastShot;

    /** This is a flag to indicate if the bot is in kill mode. **/
    private boolean KillMode;

    /** To make sure, we don't exceed the time limits. **/
    private Set<Point> shotTaken ;

    /** To keep track of the past hits. **/
    private List<int[]> pastHits;

    /** Time tracking for the execution time limits. **/
    private long time;


    /**
     * The getMinShipRemaining method determines the minimum size among the remaining ships.
     * @return The size of the smallest remaining ship.
     */
    private int getMinShipRemaining(){
        if (!remainingShips.isEmpty()) {
            int minimum = remainingShips.get(0);
            for (int i = 1; i < remainingShips.size(); i++) {
                if (minimum > remainingShips.get(i)) {
                    minimum = remainingShips.get(i);
                }// end if() #1
            }// end for loop()
            return minimum;
        }// end if() #2
        return 0;
    }// end getMinShipRemaining()


    /**
     * The getMaxShipRemaining method determines the maximum size among the remaining ships.
     * @return The size of the largest remaining ship.
     */
    private int getMaxShipRemaining(){
        int Maximum = remainingShips.get(0);
        for (int i = 1; i < remainingShips.size(); i++) {
            if (Maximum < remainingShips.get(i)) {
                Maximum = remainingShips.get(i);
            }// end if()
        }// end for loop()
        return Maximum;
    }// getMaxShipRemaining()


    /**
     * The changeGrid method regenerates the grid based on the smallest remaining ship size.
     * The method also clears and repopulates the targetQueue.
     */
    private void changeGrid(){
        // Clear's existing targets from the queue.
        targetQueue.clear();
        // Determines the spacing based on the smallest.
        int spacing = getMinShipRemaining();

        // Iterate over to the grid with adjusted spacing.
        for (int i = 0; i <= gameSize; i++) {
            List<Integer> points = new ArrayList<>();

            // Loop to collect potential points based on spacing.
            for (int j = i%spacing; j < gameSize; j+= (spacing)) {
                points.add(0,j);
            }// end for loop() #2

            // To store valid points.
            List<Integer> Keep = new ArrayList<>();
            // To keep track of the last valid point for comparison.
            int lastPoint = points.get(0);

            // This is to validate points to make sure they follow the game rules.
            for(int point : points) {
                boolean isValid = true;
                // Checks each point until a nonvalid one is found.
                for (int j = point; j <= lastPoint && isValid; j++) {
                    isValid = isTargetValid(new int[]{j, i-1});
                }// end for loop() #4
                if(isValid){
                    Keep.add(point);
                    lastPoint = point;
                }// end if()
            }// end for loop() #3

            // Iterate to add valid points to the targetQueue.
            for(int point : Keep) {
                int[] tmp = new int[]{point,i-1};

                // Sort into the target queue based on the X coordinate.
                int x = 0;
                for ( ;x < targetQueue.size() && (targetQueue.get(x)[0] < (tmp[0]+1)); ) {
                    x++;
                }// end for loop() #5
                targetQueue.add(x,tmp);
            }// end for loop() #6
        }// end for loop() #1
        // This is to clean up cues from illegal Shots.
        removeIllegalShots();
    }// end changeGrid()


    /**
     * The makeGrid populates the initial grid based on game size and ship sizes.
     */
    private void makeGrid(){
        int spacing = getMinShipRemaining();
        for (int i = 0; i < gameSize; i++) {
            for (int j = i%spacing; j < gameSize; j+= (spacing)) {
                int[] tmp = new int[]{i-1,j};

                if(isTargetValid(tmp)) {
                    targetQueue.add(tmp);
                }// end if()
            }// end for loop() #2
        }// end for loop() #1
    }// end makeGrid()


    /**
     * The isTargetValid method checks if the target is valid within the game board and not previously attempted.
     * @param tmp int[] An array containing the X and Y coordinates of the target.
     * @return True if the target is valid and not attempted and false otherwise.
     */
    private boolean isTargetValid(int[] tmp) {
        Point shot = new Point(tmp[0], tmp[1]);
        return isPointValid(shot.x, shot.y) && !shortAttempted(shot);
    }// end isTargetValid()


    /**
     * The shortAttempted Checks if a shot has been previously attempted at the point.
     * @param shot Point The point representing the target location.
     * @return True if the shot was previously attempted and false otherwise.
     */
    private boolean shortAttempted(Point shot) {
        return shotTaken.contains(shot);
    }// end shotAttempted()


    /**
     * The isPointValid Method checks if a point is within the game board boundaries.
     * @param x int The X coordinate of the point.
     * @param y int The Y coordinate of the point.
     * @return True if the point is within the boundaries and false otherwise.
     */
    private boolean isPointValid(int x, int y) {
        return (x >= 0 && x < gameSize && y >= 0 && y < gameSize);
    }// end isPointValid()


    /**
     * The updateOnSunkShip Method updates the game state on a shrinking ship.
     * The method also handles the cleanup and state transition.
     */
    private void updateOnSunkShip() {
        // This is to check if the number of sunk ships has increased.
        int shrinkCount = battleShip.numberOfShipsSunk();
        if (shrinkCount > sunkShipCount) {
            sunkShipCount = shrinkCount;

            // This is to remove recently sunk ships based on the size of past hits.
            boolean removed = false;
            for (int i = 0; i < remainingShips.size(); i++) {
                if(remainingShips.get(i) == pastHits.size() && !removed){
                    remainingShips.remove(i);
                    removed = true;
                }// end if() #2
            }// end for loop()

            // Adjusting the targeting grid based on the new ship configuration.
            changeGrid();

            // This is just to clear the state for the next round of targeting.
            pastHits.clear();
            killQueue.clear();
            KillMode = false;
        }// end if() #1
    }// end updateOnSunkShip()


    /**
     * The initialize method initializes the battleship game state.
     * @param b BattleShip2 An instance representing the current game.
     */
    @Override
    public void initialize(BattleShip2 b) {
        lastShot =new int[]{0,0};
        KillMode = false;
        remainingShips = new ArrayList<>();
        targetQueue = new ArrayList<>();
        shotTaken = new HashSet<>();
        pastHits = new ArrayList<>();
        killQueue = new ArrayList<>();
        hits = new ArrayList<>();
        battleShip = b;
        gameSize = b.BOARD_SIZE; // Setting the size of the board.

        // A seed for reproducing if needed.
        random = new Random(0xAAAAAAAA);
        sunkShipCount = 0;

        // Initializes the remaining ships with the size of all ships in the game.
        for(int ship: battleShip.getShipSizes()) {
            remainingShips.add(ship);
        }// end for loop()
        // Updating the shrunk ships count.
        sunkShipCount = battleShip.numberOfShipsSunk();

        // Setting up the initial targeting grid and starting timing for performance measurement.
        makeGrid();
        time = System.nanoTime();

    }// end initialize()


    /**
     * The delta method checks if two points are adjacent vertical or horizontal.
     * @param A int[] The first point as an array x,y.
     * @param B int[] The second point as an array x,y.
     * @return True if the points are adjacent and false otherwise.
     */
    private boolean delta(int[] A, int[] B){
        // horizontal adjacent check.
        if(A[0]-B[0] == 0) {
            return (A[1] - B[1] == 1);
        }// end if() #1
        // vertical adjacent check.
        if(A[1]-B[1] == 0) {
            return (A[0] - B[0] == 1);
        }// end if() #2
        return false;
    }// end delta()


    /**
     * The removeIllegalShots method removes shots from the target queue to avoid illegal placements.
     */
    private void removeIllegalShots(){
        for (int i = (targetQueue.size() -1); !targetQueue.isEmpty() && i >= 0  ; i--) {
            boolean remove = false;
            for (int[] hit: hits) {
                if( !remove && delta(targetQueue.get(i),hit)){
                    targetQueue.remove(i);
                    remove= true;
                }// end if()
            }// end for loop() #2
        }// end for loop() #1
    }// end removeIllegalShots()


    /**
     * The addTargetHit Method adds potential target points around a hit for targeting in Kill mode.
     * @param shot Point The point representing the target location.
     */
    private void addTargetHit(Point shot) {
        int[][] shotDirections = {{-1,0}, {0,1}, {0,-1},{1,0}};
        for (int[] direction : shotDirections) {
            int nextX = shot.x + direction[0];
            int nextY = shot.y + direction[1];
            if(isPointValid(nextX, nextY) && !shotTaken.contains(new Point(nextX, nextY))) {
                killQueue.add(0,new int[]{nextX, nextY});
            }// end if()
        }// end for loop()
    }// end addTargetHit()


    /**
     * The killWalk method chooses the next target in kill mode based on analyzing of past hits.
     * @param shot Point The point representing the target location.
     * @return The coordinates of the next target in kill mode.
     */
    private int[] killWalk(Point shot){
        //on first hit, get direction of attack.
        if(pastHits.size() == 1){
            // Add the adjacent points as potential next shots.
            addTargetHit(shot);
        } else{
            // This clears the queue for a new start if no more than one hit is found.
            if(pastHits.size() == 2) {
                killQueue.clear();
            }// end if() #1
            // After the second hit determine the ship boundary box based on hits.
            int[] shipBoundsMin = new int[]{pastHits.get(0)[0],pastHits.get(0)[1]};
            int[] shipBoundsMax = new int[]{pastHits.get(0)[0],pastHits.get(0)[1]};
            // get direction
            for(int[] shots: pastHits ){
                if(shipBoundsMin[0] >= shots[0]){
                    shipBoundsMin[0] = shots[0];
                }// end if() #2
                if(shipBoundsMin[1] >= shots[1]){
                    shipBoundsMin[1] = shots[1];
                }// end if() #3
                if(shipBoundsMax[0] <= shots[0]){
                    shipBoundsMax[0] = shots[0];
                }// end if() #4
                if(shipBoundsMax[1] <= shots[1]){
                    shipBoundsMax[1] = shots[1];
                }// end if() #5
            }// end for loop()

            // This calculates potential next shots based on ship position.
            int xShift = shipBoundsMax[0]-shipBoundsMin[0] == 0? 0 : 1;
            int yShift = shipBoundsMax[1]-shipBoundsMin[1] == 0? 0 : 1;
            if(isPointValid(shipBoundsMax[0] + xShift,shipBoundsMax[1]+yShift) && !shotTaken.contains(new Point(shipBoundsMax[0] + xShift,shipBoundsMax[1]+yShift))) {
                killQueue.add(0,new int[]{shipBoundsMax[0] + xShift,shipBoundsMax[1]+yShift});
            } else if(isPointValid(shipBoundsMin[0] - xShift,shipBoundsMin[1]-yShift) && !shotTaken.contains(new Point(shipBoundsMin[0] - xShift,shipBoundsMin[1]-yShift))) {
                killQueue.add(0,new int[]{shipBoundsMin[0] - xShift,shipBoundsMin[1]-yShift});
            }// end else if()
            // Exit if no valid shots are found.
            if(killQueue.isEmpty()){
                exit(-1);
            }// end if() #6
        }// end else

        // then return the first item of the queue.
        return killQueue.remove(0);
    }// end killWalk()


    /**
     * The fallback Method is a fallback strategy for determining the next shot when the standard logic doesn't work.
     */
    private void fallback() {
        // Finding the largest remaining ship.
        int gap = getMaxShipRemaining();

        // Loop to get potential starting points based on the gap.
        boolean needsShot = false;
        for (int x = gap; x <gameSize&& !needsShot; x++) {
            for (int y = gap; y < gameSize&& !needsShot; y++) {

                // Checking the horizontal and vertical possibilities from each starting point.
                int target = 0;
                for (int spots = x - gap; spots <x && !needsShot; spots++) {
                    needsShot = isPointValid(spots,y);
                    target = spots;
                }// end for loop() #3

                // Only Process the vertical check if no horizontal shots are found.
                if(needsShot){
                    targetQueue.add(new int[]{target,y});
                }// end if() #1
                for (int spots = y - gap; spots <y && !needsShot; spots++) {
                    needsShot = isPointValid(x,spots);
                    target = spots;
                }// end for loop() #4
                if(needsShot){
                    targetQueue.add(new int[]{x,target});
                }// end if() #2
            }// end for loop() #2
            // Randomize targetQueue to avoid predictability.
            Collections.shuffle(targetQueue);
        }// end for loop() #1
    }// end fallback()


    /**
     * The findShips method selects the next target from the target queue.
     * The method ensures shots are chosen from the available and unattempted targets.
     * @return The coordinates of the next shot.
     */
    private int[] findShips() {
        // This is to retrieve and validate the next target from the queue.
        int[] next = targetQueue.remove(0);
        Point shot = new Point(next[0], next[1]);

        // Skipping over already attempted shots.
        while (shotTaken.contains(shot)){
            next = targetQueue.remove(0);
            shot = new Point(next[0], next[1]);
        }// end while loop()
        return next;
    }// end findShips()


   /**
    * The checkTimeLimit Method checks if the execution time exceeds the set limit and if so terminates it.
    */
   private void checkTimeLimit() {
       if( ((System.nanoTime() - time) / 1000) >=10000000){
           battleShip.reportResults();
           System.out.println("Exceeded time limit.");
           exit(-1);
       }// end if()
   }// end  checkTimeLimit()


    /**
     * The updateGameScore Method updates the game score after each shot for best and worst games.
     */
    private void updateGameScore() {
        if (bestGame > shotTaken.size() && battleShip.allSunk()){
            bestGame = shotTaken.size();
        }// end if() #1
        if (worstGame < shotTaken.size() && battleShip.allSunk()){
            worstGame = shotTaken.size();
        }// end if() #2
    }// end updateGameScore()


    /**
     * The determineNextShot method determines the next target based on the current mode search or kill.
     * @return The coordinates of the next shot as an int array.
     */
    private int[] determineNextShot() {
        if (!KillMode) {
            return findShips();
        } else {
            return killWalk(new Point(pastHits.get(0)[0], pastHits.get(0)[1]));
        }// end else
    }// end determineNextShot()


    /**
     * The executeAndProcessShot method executes a shot At the determined coordinates and processes the outcome.
     * The method also updates the game state based on hit or miss.
     * @param next int[] The coordinates for the next shot.
     */
    private void executeAndProcessShot(int[] next) {
        // Converting the array two Point.
        Point shot = new Point(next[0], next[1]);
        //skip shots that have already been made.
        lastShot = next;
        boolean hit = battleShip.shoot(shot);

        shotTaken.add(shot);

        // Adding to hit if it was a successful hit.
        if (hit) {
            hits.add(next);
            removeIllegalShots();
        }// end if() #1

        //add a thing to track pervious shot, if it was a hit, we have a direction to go in.
        if (!battleShip.allSunk()) {
            if (hit) {
                KillMode = true;
                pastHits.add(0, next);
            }// end if() #3

            // Checking and updating the game state if ships are sunk.
            updateOnSunkShip();
        }// end if() #2
    }// end executeAndProcessShot()


    /**
     * The fireShot Method executes the game logic for each turn until all ships are sunk.
     * The method also manages the shot duration, execution and state updates.
     */
    @Override
    public void fireShot() {
        // Continue playing the game until all ships have been sunk.
        if(!battleShip.allSunk()) {
            checkTimeLimit();
            // Determining and executing the next shot if there are targets available.
            if (!targetQueue.isEmpty() || (!killQueue.isEmpty() || KillMode) ) {
                int[] next = determineNextShot();
                executeAndProcessShot(next);
            } else {
                // Use the fallback strategy.
                fallback();
                // Execute a fallback shot if the target queue is populated.
                if (!targetQueue.isEmpty()) {
                    int[] next = targetQueue.remove(0);
                    executeAndProcessShot(next);
                }// end if() #3
            }// end else
            // Checking and updating the game state if ships are sunk.
            updateGameScore();
        }// end if() #1
    }// end fireShot()


    /**
     * Authorship of the solution - must return names of all students that contributed to
     * the solution
     * @return names of the authors of the solution
     */
    @Override
    public String getAuthors() {
        return "Mark Yendt (CSAIT Professor\nLuca Quacquarelli (COMP-10205 Student\nKeegan Andrus (COMP-10205 Student)\n\nWorst game: " + worstGame +"\nbest game: " + bestGame;
    }// end getAuthors()


}// end CoolBot2024