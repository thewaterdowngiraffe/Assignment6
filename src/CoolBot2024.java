
import battleship.*;

import java.awt.Point;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static java.lang.System.exit;

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

    private int worstGame = 0;
    private BattleShip2 battleShip;
    private Random random;

    private int sunkShipCount;

    private List<Integer> remainingShips;

    private List<int[]> targetQueue ;
    private List<int[]> killQueue ;

    // delete the hits, its for testing
    private List<int[]> hits;


    private int[] lastShot;
    private boolean KillMode;
    private Set<Point> shotTaken ;

    private List<int[]> pastHits;



    private int getMinShipRemaining(){
        if (!remainingShips.isEmpty()){
            int minimum = remainingShips.get(0);
            for (int i = 1; i < remainingShips.size(); i++) {
                if (minimum > remainingShips.get(i))
                    minimum = remainingShips.get(i);
            }
            return minimum;
        }
        return 0;
    }
    private int getMaxShipRemaining(){
        int Maximum = remainingShips.get(0);
        for (int i = 1; i < remainingShips.size(); i++) {
            if (Maximum < remainingShips.get(i))
                Maximum = remainingShips.get(i);
        }
        return Maximum;
    }


    private void changeGrid(){
        // this will regenerate the remaining target grid, there is some room for improvement, but im to lazy to do that atm
        // we need to make a prune function to check all queue for repeat moves, these will need to be removed
        boolean resume = true;
        int spacing = getMinShipRemaining();
        //System.out.println("death to me ");
        //System.out.println(battleShip.allSunk());
        //System.out.println( );
        int[] nextshot = lastShot;
        if(!targetQueue.isEmpty()){
             nextshot = targetQueue.get(0);
        }
        //System.out.println("death to me jl");

        targetQueue.clear();
        nextshot[0] = nextshot[0]-(spacing-1) >= 0? (nextshot[0]-(spacing-1))%spacing: 0;

        nextshot[0] = nextshot[0]-(spacing-1) >= 0? (nextshot[0]-1): 0;

        for (int i = nextshot[0]; i < gameSize; i++) {
            int target = resume?
                    nextshot[1] - (spacing-1) >=0 ?  nextshot[1] - (spacing-1): 0
                    :
                    i%spacing;
            resume = false;

            for (int j = target; j < gameSize; j+= (spacing)) {
                int[] tmp = new int[]{i,j};
                this.targetQueue.add(tmp);
            }
        }
    }
    private void makeGrid(){
        int spacing = getMinShipRemaining();
        for (int i = 0; i < gameSize; i++) {
            for (int j = i%spacing; j < gameSize; j+= (spacing)) {
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

        return (x >= 0 && x < gameSize && y >= 0 && y < gameSize);
    }// end isPointValid()




    private void updateOnSunkShip() {
        int shrinkCount = battleShip.numberOfShipsSunk();
        if (shrinkCount > sunkShipCount) {
            sunkShipCount = shrinkCount;
            boolean removed = false;
            //System.out.println(remainingShips.size());

            for (int i = 0; i < remainingShips.size(); i++) {


                if(remainingShips.get(i) == pastHits.size() && !removed){
                    remainingShips.remove(i);
                    removed = true;
                }
            }
            //System.out.println("wompt");

            changeGrid();
            //System.out.println("wompt");

            pastHits.clear();
            killQueue.clear();
            KillMode = false;


            try {
                FileWriter myWriter = new FileWriter("match.csv");


                for (int[] h : hits) {
                    Point point = new Point(h[0], h[1]);
                    myWriter.write(String.valueOf(point));
                    myWriter.write("\n");
                }

                myWriter.close();

                myWriter = new FileWriter("matchHits.csv");

                Iterator itr = shotTaken.iterator();
                while (itr.hasNext()) {
                    Point point = (Point) itr.next();
                    myWriter.write(String.valueOf(point));
                    myWriter.write("\n");
                }


                myWriter.close();
            } catch (IOException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }
            Scanner myObj = new Scanner(System.in);
            String userName = myObj.nextLine();



        }
    }



    /**
     * Constructor keeps a copy of the BattleShip instance
     * Create instances of any Data Structures and initialize any variables here
     * @param b previously created battleship instance - should be a new game
     */
    @Override
    public void initialize(BattleShip2 b) {
        lastShot =new int[]{0,0};
        KillMode = false;
        remainingShips = new ArrayList<Integer>();
        targetQueue = new ArrayList<int[]>();
        shotTaken = new HashSet<>();
        pastHits = new ArrayList<int[]>();
        killQueue = new ArrayList<int[]>();
        hits = new ArrayList<int[]>();
        battleShip = b;
        gameSize = b.BOARD_SIZE;
        System.out.println(b);



        // Need to use a Seed if you want the same results to occur from run to run
        // This is needed if you are trying to improve the performance of your code

        random = new Random(0xAAAAAAAA);   // Needed for random shooter - not required for more systematic approaches
        sunkShipCount = 0;


        // keep this it

        for(int ship: battleShip.getShipSizes()) {
            remainingShips.add(ship);
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
        //for(int[] arr: targetQueue){
        //    System.out.println(Arrays.toString(arr));
        //}



    }// end initialize()




    private void addTargetHit(Point shot) {
        int[][] shotDirections = {{-1,0}, {1,0}, {0,-1}, {0,1}};

        for (int[] direction : shotDirections) {
            int nextX = shot.x + direction[0];
            int nextY = shot.y + direction[1];
            if(isPointValid(nextX, nextY) && !shotTaken.contains(new Point(nextX, nextY))) {
                //targetQueue.add(0,new int[]{nextX, nextY});
                killQueue.add(0,new int[]{nextX, nextY});
            }
        }
    }

    private boolean delta(int[] A, int[] B){
        if(A[0]-B[0] == 0)
            return !(A[1]-B[1] == 1);
        if(A[1]-B[1] == 0)
            return !(A[0]-B[0] == 1);
        return false;
    }
    private void removeIlligalShots(){
        //System.out.println(targetQueue.size());
        for (int i = (targetQueue.size() -1); !targetQueue.isEmpty() && i >= 0  ; i--) {
            //System.out.println(i);
            //System.out.println(targetQueue.size());

            boolean remove = false;
            for (int[] hit: hits)
            {
                //System.out.println("plz no");
                if( !remove && delta(targetQueue.get(i),hit)){
                    //System.out.println("crash");
                    targetQueue.remove(i);
                    //System.out.println("dont");
                    remove= true;
                }
            }
        }

    }

    private int[] killMode(Point shot){
        System.out.println(killQueue.size());
        System.out.println(pastHits.size());
        if(pastHits.size() >= 2){

            //if(killQueue.size()> 0){
            //    System.out.println("failTest");
            //    System.out.println(Arrays.toString(killQueue.get(0)));
            //    return killQueue.remove(0);
            //}
            System.out.println("kill walk");

            int[] shipBoundsMin = pastHits.get(0);
            int[] shipBoundsMax = pastHits.get(0);
            for(int[] shots: pastHits ){
                System.out.println(Arrays.toString(shots));
                System.out.println(shots[1]);
                System.out.println(shipBoundsMax[1]);
                if(shipBoundsMin[0] > shots[0]){
                    shipBoundsMin[0] = shots[0];
                }
                if(shipBoundsMin[1] > shots[1]){
                    shipBoundsMin[1] = shots[1];
                }
                if(shipBoundsMax[0] < shots[0]){
                    shipBoundsMax[0] = shots[0];
                }
                if(shipBoundsMax[1] < shots[1]){
                    shipBoundsMax[1] = shots[1];
                }
            }
            System.out.println("ship bounds");
            System.out.println(Arrays.toString(shipBoundsMin));
            System.out.println(Arrays.toString(shipBoundsMax));
            if(shipBoundsMax[0]-shipBoundsMin[0] == 0){
                //ship is on x axis
                System.out.println("x");

                int maxShipSize = getMaxShipRemaining();
                int currentHitSize = shipBoundsMax[1]-shipBoundsMin[1];
                killQueue.clear();
                for (int i = 0; i < maxShipSize - currentHitSize; i++) {
                    killQueue.add(new int[] {shipBoundsMax[0],shipBoundsMax[1]+1+i});
                    killQueue.add(new int[] {shipBoundsMax[0],shipBoundsMin[1]-1-i});
                }



                System.out.println("this is a test");
                System.out.println(killQueue.size());
                System.out.println();
                for (int i = (killQueue.size() -1); i >= 0 ; i--) {
                    //System.out.println(i);
                    //System.out.println(Arrays.toString(killQueue.get(i)));
                    //System.out.println(isTargetValid(killQueue.get(i)));
                    if(!isTargetValid(killQueue.get(i)))
                        killQueue.remove(i);
                }

            }
            if(shipBoundsMax[1]-shipBoundsMin[1] == 0){
                //ship on y axis
                //System.out.println("this thing");
                int maxShipSize = getMaxShipRemaining();
                int currentHitSize = shipBoundsMax[0]-shipBoundsMin[0];
                for (int i = 0; i < maxShipSize - currentHitSize; i++) {
                    killQueue.add(new int[] {shipBoundsMax[1],shipBoundsMax[0]+1+i});
                    killQueue.add(new int[] {shipBoundsMax[1],shipBoundsMin[0]-1-i});
                }
                //System.out.println("this is a test");
                //System.out.println(killQueue.size());
                for (int i = killQueue.size()-1; i > 0 ; i--) {
                    //System.out.println(i);
                    //System.out.println(Arrays.toString(killQueue.get(i)));
                    //System.out.println(isTargetValid(killQueue.get(i)));
                    if(!isTargetValid(killQueue.get(i)))
                        killQueue.remove(i);
                }


                int[] next = targetQueue.get(0);
                Point target = new Point(next[0],next[1]);

                while (shotTaken.contains(target)){
                    next = targetQueue.remove(0);
                    target = new Point(next[0], next[1]);
                }

            }


        }
        else{
            addTargetHit(shot);
        }

        return killQueue.remove(0);
    }

    private int[] findShips(){
        // this does the walking of the map
        int[] next = targetQueue.remove(0);

        Point shot = new Point(next[0], next[1]);


        while (shotTaken.contains(shot)){
            next = targetQueue.remove(0);
            shot = new Point(next[0], next[1]);
        }
        System.out.println(shot);
        return next;
    }
    /**
     * Create a random shot and calls the battleship shoot method
     * Put all logic here (or in other methods called from here)
     * The BattleShip API will call your code until all ships are sunk
     */

    @Override
    public void fireShot() {

        System.out.println("ahhhhhhhhhh");
        if(!battleShip.allSunk()) {


            if (!targetQueue.isEmpty() || !killQueue.isEmpty()) {

                int[] next = {0, 0};
                Point shot = new Point(next[0], next[1]);
                ;

                if (!KillMode) {

                    // this walks the map
                    next = findShips();
                    System.out.println("walk mode error");


                } else {
                    //this is kill mode

                    next = killMode(new Point(pastHits.get(0)[0], pastHits.get(0)[1]));
                    //System.out.println(Arrays.toString(next));
                    System.out.println("kill mode error");
                }

                shot = new Point(next[0], next[1]);


                //skip shots that have already been made

                //System.out.println(shot);
                //System.out.println("Ejhere");
                //System.out.println(Arrays.toString(next));

                //System.out.println(shot);
                lastShot = next;
                boolean hit = battleShip.shoot(shot);

                System.out.print("shoot: ");
                System.out.println(Arrays.toString(next));


                //System.out.println("waho");


                shotTaken.add(shot);

                if (hit) {
                    //System.out.println("death");
                    //System.out.println("deathnt");

                    hits.add(next);
                    removeIlligalShots();
                }

                //add a thing to track pervious shot, if it was a hit, we have a direction to go in
                if (!battleShip.allSunk()) {

                    if (hit) {

                        KillMode = true;
                        killMode(shot);

                        pastHits.add(0, next);
                    }

                    updateOnSunkShip();
                    //System.out.println("womp");


                }

                // print each match to file for analisis
                if (battleShip.allSunk()) {


                    try {
                        FileWriter myWriter = new FileWriter("match.csv");


                        for (int[] h : hits) {
                            Point point = new Point(h[0], h[1]);
                            myWriter.write(String.valueOf(point));
                            myWriter.write("\n");
                        }

                        myWriter.close();

                        myWriter = new FileWriter("matchHits.csv");

                        Iterator itr = shotTaken.iterator();
                        while (itr.hasNext()) {
                            Point point = (Point) itr.next();
                            myWriter.write(String.valueOf(point));
                            myWriter.write("\n");
                        }


                        myWriter.close();
                    } catch (IOException e) {
                        System.out.println("An error occurred.");
                        e.printStackTrace();
                    }
                }

            } else if (worstGame < shotTaken.size()){
                worstGame = shotTaken.size();
                for (int[] item : killQueue) {
                    System.out.println(Arrays.toString(item));
                }
                System.out.println(battleShip.numberOfShipsSunk());
                System.out.println(battleShip.getShipSizes().length);
                System.out.println(remainingShips);

                Iterator itr = shotTaken.iterator();

                // check element is present or not. if not loop will
                // break.
                try {
                    FileWriter myWriter = new FileWriter("filename.txt");
                    while (itr.hasNext()) {
                        Point point = (Point) itr.next();
                        myWriter.write(String.valueOf(point));
                        myWriter.write("\n");
                    }
                    myWriter.close();
                } catch (IOException e) {
                    System.out.println("An error occurred.");
                    e.printStackTrace();
                }


                System.out.println(battleShip.allSunk());
                exit(-1);
                //System.out.println("error");
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
