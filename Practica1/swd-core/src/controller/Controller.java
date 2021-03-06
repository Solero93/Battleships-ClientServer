package controller;

import controller.gameModes.GameMode;
import controller.gameModes.GameModeFactory;
import exceptions.ReadGridException;
import model.Grid;
import utils.Message;
import utils.enums.Command;
import utils.enums.Orientation;
import utils.enums.ShipType;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Class that represents the Controller of the Game
 */
public class Controller {
    protected Grid myGrid;
    protected GameMode gm;

    /**
     * Creates a Controller object.
     * Also initializes the Grid.
     */
    public Controller() {
        this.myGrid = new Grid();
    }

    /**
     * Creates the GameMode the User wants to play in.
     *
     * @param mode : 0-Manual, 1-Random , 2-BetterAI
     */
    public void createGameMode(int mode) {
        this.gm = new GameModeFactory().createGameMode(mode);
    }

    /**
     * Generates the Grid automatically using backtracking
     */
    public void generateGridAutomatic() {
        List<ShipType> ships = Arrays.asList(ShipType.A, ShipType.B, ShipType.B, ShipType.S, ShipType.S,
                ShipType.D, ShipType.D, ShipType.P, ShipType.P);
        Collections.shuffle(ships); // Randomizing ships
        ArrayList<Orientation> orient = new ArrayList<>();
        Random orientRandom = new Random();
        for (int i = 0; i < ships.size(); i++) { // Randomizing orientation of each ship
            switch (orientRandom.nextInt(2)) {
                case 0:
                    orient.add(Orientation.H);
                    break;
                case 1:
                    orient.add(Orientation.V);
                    break;
            }
        }
        ArrayList<String> shipPositions = new ArrayList<>();
        int i = 0;
        for (String currPosition = "A0"; shipPositions.size() < ships.size();
             currPosition = myGrid.nextPosition(currPosition)) {
            if (currPosition == null) {
                // If there are no more positions to put a ship
                // We do backtracking
                currPosition = shipPositions.remove(i - 1);
                this.myGrid.removeShip(ships.get(i - 1).size, currPosition, orient.get(i - 1));
                i--;
            } else if (this.myGrid.putShip(ships.get(i).size, currPosition, orient.get(i))) {
                // If you could put the ship in place
                // Try to put the next ship
                shipPositions.add(currPosition);
                i++;
            }
        }
    }

    /**
     * Generates Grid from layout file
     *
     * @param filename : File coding the layout
     * @throws IOException
     * @throws ReadGridException
     * @throws IllegalArgumentException
     */
    public void generateGridFromFile(String filename) throws IOException, ReadGridException, IllegalArgumentException {
        // Create hashmap to count number of created ships
        HashMap<ShipType, Integer> numShips = new HashMap<>();
        for (ShipType ship : ShipType.values()) {
            numShips.put(ship, ship.numShips);
        }
        FileReader fileReader = new FileReader(filename);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String currentLine;
        while ((currentLine = bufferedReader.readLine()) != null) {
            String[] shipToPut = currentLine.split(",");
            String shipType = shipToPut[0].toUpperCase(), position = shipToPut[1], orientation = shipToPut[2].toUpperCase();
            int shipLeftOfType = numShips.get(ShipType.valueOf(shipType));
            // If you try to put too many ships of a type or
            // there has been an error while putting, throw error
            if (shipLeftOfType == 0 ||
                    !this.myGrid.putShip(ShipType.valueOf(shipType).size, position, Orientation.valueOf(orientation))) {
                bufferedReader.close();
                throw new ReadGridException();
            }
            numShips.put(ShipType.valueOf(shipType), shipLeftOfType - 1);
        }
        bufferedReader.close();
        // If there are ships that weren't put, throw error
        for (ShipType ship : numShips.keySet()) {
            if (numShips.get(ship) != 0) {
                throw new ReadGridException();
            }
        }
    }

    /**
     * Generate Grid from manual entry of user
     *
     * @param shipType
     * @param position
     * @param orientation
     * @throws ReadGridException
     * @throws IllegalArgumentException
     */
    public void generateGridByUser(String shipType, String position, String orientation) throws ReadGridException, IllegalArgumentException {
        if (!this.myGrid.putShip(ShipType.valueOf(shipType).size, position, Orientation.valueOf(orientation))) {
            throw new ReadGridException();
        }
    }

    /**
     * Generates a position to Hit
     *
     * @return Position to Hit.
     * @throws IOException
     * @see GameMode#generateHitPosition()
     */
    public Message play() throws IOException {
        String hitPosition = this.gm.generateHitPosition();
        return new Message()
                .setCommand(Command.FIRE)
                .setParams(hitPosition);
    }

    /**
     * @see GameMode#commitMove(Command)
     */
    public void commitMove(Message msg) {
        this.gm.commitMove(msg.getCommand());
    }

    /**
     * Hits a cell and returns the message to send to Enemy
     *
     * @param position : position to hit on grid
     * @return The message to send to Enemy
     * @throws IOException
     */
    public Message hitMyCell(String position) throws IOException {
        Command cmd = this.myGrid.hitCell(position);
        if (cmd == Command.ERROR) {
            return new Message()
                    .setCommand(cmd)
                    .setParams("Error while hitting cell");
        } else {
            return new Message()
                    .setCommand(cmd);
        }
    }

    /**
     * Lets print the current Grid
     * For Debug Purposes.
     *
     * @return String of Grid
     */
    public String getCurrentGrid() {
        return this.myGrid.toString();
    }
}