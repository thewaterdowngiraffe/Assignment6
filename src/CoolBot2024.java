
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
    private int bestGame = 10000;

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
    private long time;



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

    /**
     * if not used when project done, delete it.
     * @return
     */
    private int getMaxShipRemaining(){
        int Maximum = remainingShips.get(0);
        for (int i = 1; i < remainingShips.size(); i++) {
            if (Maximum < remainingShips.get(i))
                Maximum = remainingShips.get(i);
        }
        return Maximum;
    }

    /**
     * this regenerates and optimizes the new grid.
     */
    private void changeGrid(){

        //makeGrid();
        // new grid already made at this point
        targetQueue.clear();
        int spacing = getMinShipRemaining();
        for (int i = 0; i <= gameSize; i++) {
            List<Integer> points = new ArrayList<Integer>();

            for (int j = i%spacing; j < gameSize; j+= (spacing)) {
                points.add(0,j);
            }
            List<Integer> Keep = new ArrayList<Integer>();
            int lastPoint = points.get(0);

            for(int point : points) {
                boolean isValid = true;
               // System.out.println(lastPoint);
               // System.out.println(point);
               // System.out.println("status "+ (lastPoint >= point));
                for (int j = point; j <= lastPoint && isValid; j++) {
                    //System.out.print(i+ " "+ j + ": ");
                    isValid = isTargetValid(new int[]{j, i-1});
                    //System.out.println(isValid);
                }
                if(isValid){
                    Keep.add(point);
                    lastPoint = point;
                }
            }

            for(int point : Keep) {
                int[] tmp = new int[]{point,i-1};

                int x = 0;
                for ( ;x < targetQueue.size() && (targetQueue.get(x)[0] < (tmp[0]+1)); ) {
                    x++;
                }
                targetQueue.add(x,tmp);


            }


        }

        //saves about 8 shots
        System.out.print("queue: " + targetQueue.size()+" -> ");
        removeIlligalShots();
        System.out.println(targetQueue.size());
        //System.out.println(targetQueue.size());


        try {
            FileWriter myWriter = new FileWriter("targetGrid.csv");

            for (int[] h : targetQueue) {
                Point point = new Point(h[0], h[1]);
                myWriter.write(String.valueOf(point));
                myWriter.write("\n");
            }


            myWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        //Scanner myObj = new Scanner(System.in);
        //String userName = myObj.nextLine();

        //exit(-1);
    }


    private void makeGrid(){
        int spacing = getMinShipRemaining();
        for (int i = 0; i <= gameSize; i++) {
            for (int j = i%spacing; j < gameSize; j+= (spacing)) {
                int[] tmp = new int[]{i-1,j};
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
        //System.out.println(b);




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
        time = System.nanoTime();

    }// end initialize()






    private boolean delta(int[] A, int[] B){
        if(A[0]-B[0] == 0)
            return (A[1]-B[1] == 1);
        if(A[1]-B[1] == 0)
            return (A[0]-B[0] == 1);
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
                //System.out.println(Arrays.toString(targetQueue.get(i)) +" vs "+ Arrays.toString(hit) +"remove: "+delta(targetQueue.get(i),hit));
                if( !remove && delta(targetQueue.get(i),hit)){
                    //System.out.println("crash");
                    targetQueue.remove(i);
                    //System.out.println("dont");
                    remove= true;
                }
            }
        }

    }



    private void addTargetHit(Point shot) {
        int[][] shotDirections = {{-1,0}, {0,1}, {0,-1},{1,0}};
        for (int[] direction : shotDirections) {
            int nextX = shot.x + direction[0];
            int nextY = shot.y + direction[1];
            if(isPointValid(nextX, nextY) && !shotTaken.contains(new Point(nextX, nextY))) {
                //targetQueue.add(0,new int[]{nextX, nextY});
                killQueue.add(0,new int[]{nextX, nextY});
            }
        }
    }



    private int[] killWalk(Point shot){

        //on first hit, get direction of attack.
        if(pastHits.size() == 1){
            addTargetHit(shot);
        }
        else{
            if(pastHits.size() == 2) {
                killQueue.clear();
            }
            // this is after the second hit.
            int[] shipBoundsMin = new int[]{pastHits.get(0)[0],pastHits.get(0)[1]};
            int[] shipBoundsMax = new int[]{pastHits.get(0)[0],pastHits.get(0)[1]};
            // get direction
            //System.out.print("past hit count ");
            //System.out.println(pastHits.size());
            for(int[] shots: pastHits ){
                //System.out.println(Arrays.toString(shots));
                //System.out.println(shots[1]);
                //System.out.println(shipBoundsMax[1]);
                if(shipBoundsMin[0] >= shots[0]){
                    //System.out.println("new min x");
                    shipBoundsMin[0] = shots[0];
                }
                if(shipBoundsMin[1] >= shots[1]){
                    //System.out.println("new min y");
                    shipBoundsMin[1] = shots[1];
                }
                if(shipBoundsMax[0] <= shots[0]){
                    shipBoundsMax[0] = shots[0];
                    //System.out.println("new max x");

                }
                if(shipBoundsMax[1] <= shots[1]){
                    shipBoundsMax[1] = shots[1];
                    //System.out.println("new max y");

                }
            }

            int xShift = shipBoundsMax[0]-shipBoundsMin[0] == 0? 0 : 1;
            int yShift = shipBoundsMax[1]-shipBoundsMin[1] == 0? 0 : 1;
            // then add one target to the front and end of the boat
            //System.out.println("shifts");
            //System.out.println(xShift);
            //System.out.println(yShift);
            //System.out.println(Arrays.toString(shipBoundsMax));
            //System.out.println(Arrays.toString(shipBoundsMin));
            Point tmp = new Point(shipBoundsMax[0] - xShift,shipBoundsMax[1]-yShift);

            //System.out.print("stats: ");
            //System.out.print(isPointValid(shipBoundsMax[0] - xShift,shipBoundsMax[1] - yShift));
            //System.out.print(" " );
            //System.out.println(!shotTaken.contains(new Point(shipBoundsMin[0] - xShift,shipBoundsMin[1] - yShift)));
            //System.out.println(tmp);

            if(isPointValid(shipBoundsMax[0] + xShift,shipBoundsMax[1]+yShift) && !shotTaken.contains(new Point(shipBoundsMax[0] + xShift,shipBoundsMax[1]+yShift))) {
                //targetQueue.add(0,new int[]{nextX, nextY});
                killQueue.add(0,new int[]{shipBoundsMax[0] + xShift,shipBoundsMax[1]+yShift});
            }
            else if(isPointValid(shipBoundsMin[0] - xShift,shipBoundsMin[1]-yShift) && !shotTaken.contains(new Point(shipBoundsMin[0] - xShift,shipBoundsMin[1]-yShift))) {
                //targetQueue.add(0,new int[]{nextX, nextY});
                killQueue.add(0,new int[]{shipBoundsMin[0] - xShift,shipBoundsMin[1]-yShift});
            }
            if(killQueue.isEmpty()){
                for(Point x: shotTaken){
                    //System.out.println(x);
                }
               //System.out.println(battleShip.numberOfShipsSunk());
               //System.out.println(killQueue.size());
               //System.out.println("problem with the bounds");
                exit(-1);
            }
            // then remove shots that have already been taken.
        }
        //System.out.println("killQueue");
        //System.out.println(battleShip.numberOfShipsSunk());
        //for(int[] k: killQueue){
        //    System.out.println(Arrays.toString(k));
        //}
        // then return the first item of the queue.
        return killQueue.remove(0);
    }

    /**
     * only call if this is the last option
     */

    private void fallback(){
        int gap = getMaxShipRemaining();
        boolean needsShot = false;

        for (int x = gap; x <gameSize&& !needsShot; x++) {
            for (int y = gap; y < gameSize&& !needsShot; y++) {


                int target = 0;
                for (int spots = x - gap; spots <x && !needsShot; spots++) {
                    needsShot = isPointValid(spots,y);
                    target = spots;
                }
                if(needsShot){
                    targetQueue.add(new int[]{target,y});
                }
                for (int spots = y - gap; spots <y && !needsShot; spots++) {
                    needsShot = isPointValid(x,spots);
                    target = spots;
                }
                if(needsShot){
                    targetQueue.add(new int[]{x,target});
                }
            }
            // idk why
            Collections.shuffle(targetQueue);
        }
    }

    private int[] findShips(){
        // this does the walking of the map
        int[] next = targetQueue.remove(0);

        Point shot = new Point(next[0], next[1]);


        while (shotTaken.contains(shot)){
            next = targetQueue.remove(0);
            shot = new Point(next[0], next[1]);
        }
        //System.out.println(shot);
        return next;
    }
    /**
     * Create a random shot and calls the battleship shoot method
     * Put all logic here (or in other methods called from here)
     * The BattleShip API will call your code until all ships are sunk
     */

    @Override
    public void fireShot() {

        //System.out.print("ahhhhhhhhhh: ");
        //System.out.println(battleShip.numberOfShipsSunk());

        if(!battleShip.allSunk()) {

            if( ((System.nanoTime() - time) / 1000) >=10000000){
                battleShip.reportResults();

                System.out.println("to much time");
                exit(-1);
            }

            if (!targetQueue.isEmpty() || (!killQueue.isEmpty() || KillMode) ) {

                int[] next = {0, 0};
                Point shot = new Point(next[0], next[1]);


                if (!KillMode) {
                    //System.out.print("walk mode");
                    //System.out.println(targetQueue.size());
                    // this walks the map
                    next = findShips();
                    //System.out.println(targetQueue.size());



                } else {
                    //this is kill mode
                    //System.out.println("kill mode");

                    next = killWalk(new Point(pastHits.get(0)[0], pastHits.get(0)[1]));
                    //System.out.println(Arrays.toString(next));
                }

                shot = new Point(next[0], next[1]);


                //skip shots that have already been made

                //System.out.println(shot);
                //System.out.println("Ejhere");
                //System.out.println(Arrays.toString(next));

                //System.out.println(shot);
                lastShot = next;
                boolean hit = battleShip.shoot(shot);

                //System.out.print("shoot: ");
                //System.out.println(Arrays.toString(next));


                //System.out.println("waho");


                shotTaken.add(shot);
                //System.out.println(hit);
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
                        //killMode(shot);

                        pastHits.add(0, next);
                    }

                    updateOnSunkShip();
                    //System.out.println("womp");
                }
            }
            else{
                System.out.println("out of targets");
                fallback();
                int[] next = {0, 0};
                Point shot;
                next = targetQueue.remove(0);
                shot = new Point(next[0], next[1]);
                lastShot = next;
                boolean hit = battleShip.shoot(shot);
                shotTaken.add(shot);
                if (hit) {
                    hits.add(next);
                    removeIlligalShots();
                }
                //add a thing to track pervious shot, if it was a hit, we have a direction to go in
                if (!battleShip.allSunk()) {
                    if (hit) {
                        KillMode = true;
                        //killMode(shot);
                        pastHits.add(0, next);
                    }
                    updateOnSunkShip();
                    //System.out.println("womp");
                }





                System.out.println(targetQueue);
                System.out.println(killQueue);
                System.out.println(KillMode);
                System.out.println(Arrays.toString(battleShip.getShipSizes()));

                Iterator itr = shotTaken.iterator();
                try {
                    FileWriter myWriter = new FileWriter("filename.txt");
                    while (itr.hasNext()) {
                        Point point = (Point) itr.next();
                        myWriter.write(String.valueOf(point));
                        myWriter.write("\n");
                    }



                    myWriter.close();
                    myWriter = new FileWriter("shotTaken.csb");

                    for (int[] h : hits) {
                        Point point = new Point(h[0], h[1]);
                        myWriter.write(String.valueOf(point));
                        myWriter.write("\n");
                    }
//
//
                    myWriter.close();


                } catch (IOException e) {
                    System.out.println("An error occurred.");
                    e.printStackTrace();
                }





            }

            if (bestGame > shotTaken.size() && battleShip.allSunk()){
                bestGame = shotTaken.size();
            }
            if (worstGame < shotTaken.size() && battleShip.allSunk()){
                worstGame = shotTaken.size();
                for (int[] item : killQueue) {
                    System.out.println(Arrays.toString(item));
                }
                System.out.println(shotTaken.size());
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
                    myWriter = new FileWriter("shotTaken.csb");

                    for (int[] h : hits) {
                        Point point = new Point(h[0], h[1]);
                        myWriter.write(String.valueOf(point));
                        myWriter.write("\n");
                    }
//
//
                    myWriter.close();


                } catch (IOException e) {
                    System.out.println("An error occurred.");
                    e.printStackTrace();
                }

                System.out.println("worst game?");
                System.out.println(battleShip.allSunk());
                //exit(-1);
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
        return "Mark Yendt (CSAIT Professor\nLuca Quacquarelli (COMP-10205 Student\nKeegan (COMP-10205 Student)\n\nWorst game: " + worstGame +"\nbest game: " + bestGame;
    }// end getAuthors()
}// end CoolBot2024
