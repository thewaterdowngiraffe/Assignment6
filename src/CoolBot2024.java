
import battleship.*;

import java.awt.Point;
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



    /**
     * Constructor keeps a copy of the BattleShip instance
     * Create instances of any Data Structures and initialize any variables here
     * @param b previously created battleship instance - should be a new game
     */
    @Override
    public void initialize(BattleShip2 b) {
        battleShip = b;
        gameSize = b.BOARD_SIZE;
        System.out.println(gameSize);
        // Need to use a Seed if you want the same results to occur from run to run
        // This is needed if you are trying to improve the performance of your code

        random = new Random(0xAAAAAAAA);   // Needed for random shooter - not required for more systematic approaches
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
