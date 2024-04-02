
import battleship.*;

import javax.sql.rowset.serial.SQLOutputImpl;
import java.awt.Point;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

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

    private List<Integer> remainingShips = new ArrayList<Integer>();

    private List<int[]> targetQueue = new ArrayList<int[]>();



    private int getMinShipRemaining(){
        int minimum = remainingShips.get(0);
        for (int i = 1; i < remainingShips.size(); i++) {
            System.out.println(remainingShips.get(i));
            if (minimum > remainingShips.get(i))
                minimum = remainingShips.get(i);
        }
        System.out.println(minimum);
        return minimum;
    }


    private void changeGrid(){

        boolean resume = true;
        int spacing = getMinShipRemaining();
        int[] nextshot = targetQueue.get(0);
        targetQueue.clear();

        for (int i = nextshot[0]; i < gameSize; i++) {
            int target = resume? nextshot[1] : i%spacing;
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
                this.targetQueue.add(tmp);
            }
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
        if(remainingShips.isEmpty()){
            for(int ship: battleShip.getShipSizes()) {
                remainingShips.add(ship);
            }
        }



        makeGrid();
        System.out.println("\n\n");
        for (int i = 0; i < 12; i++) {
            this.targetQueue.remove(0);
        }

        System.out.println(remainingShips);
        remainingShips.removeIf(e -> e.equals(getMinShipRemaining()));
        System.out.println(remainingShips);

        System.out.println(Arrays.toString(targetQueue.get(0)));

        changeGrid();

        for(int[] arr: targetQueue){
            System.out.println(Arrays.toString(arr));
        }



    }// end initialize()


    /**
     * Create a random shot and calls the battleship shoot method
     * Put all logic here (or in other methods called from here)
     * The BattleShip API will call your code until all ships are sunk
     */
    @Override
    public void fireShot() {

        int x = random.nextInt(gameSize);
        int y = random.nextInt(gameSize);

        // Will return true if we hot a ship
        boolean hit = battleShip.shoot(new Point(x,y));

    }// end fireShot()


    /**
     * Authorship of the solution - must return names of all students that contributed to
     * the solution
     * @return names of the authors of the solution
     */
    @Override
    public String getAuthors() {
        return "Mark Yendt (CSAIT Professor),\n Luca Quacquarelli (COMP-10205 Student),\n Keegan (COMP-10205 Student)";
    }// end getAuthors()
}// end CoolBot2024