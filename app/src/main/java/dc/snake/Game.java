package dc.snake;

import java.util.ArrayList;
import java.util.Collections;

enum Direction {NONE, RIGHT, LEFT, UP, DOWN}

class Game {
    static final long SPEED = 4;
    private static int length = 3;
    final int[] xCoords, yCoords;
    final int cols, rows;
    private final ArrayList<int[]> emptyCells;
    Direction direction = Direction.NONE;

    public Game(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        emptyCells = new ArrayList<>(cols * rows);
        xCoords = new int[cols * rows];
        yCoords = new int[cols * rows];
    }

    static int getScore() {
        return length - 3;
    }

    static int getLength() {
        return length;
    }

    private void init() {
        xCoords[0] = 2;
        xCoords[1] = 1;
        yCoords[0] = yCoords[1] = yCoords[2] = xCoords[2] = 0;
        direction = Direction.RIGHT;
        length = 3;
        newFood();
    }

    private void newFood() {
        updateEmptyCells();
        Collections.shuffle(emptyCells);
        Food.x = emptyCells.get(0)[0];
        Food.y = emptyCells.get(0)[1];

    }

    private void updateEmptyCells() {
        emptyCells.clear();
        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) {
                if (!isSnake(i, j)) emptyCells.add(new int[]{i, j});
            }
        }

    }

    private boolean isSnake(int x, int y) {
        for (int i = 0; i < length; i++) {
            if (x == xCoords[i] && y == yCoords[i]) return true;
        }
        return false;
    }

    boolean move() {
        if (directionIsNotSpecified()) {
            init();
            return true;
        }
        moveBody();
        moveHead();
        if (foodIsEaten()) {
            if (gameIsOver()) {
                init();
                return false;
            }
            moveTail();
            length++;
            newFood();
        }
        if (snakeBitHerself()) clearBody();
        return true;
    }

    private boolean gameIsOver() {
        return length == rows * cols;
    }

    private boolean directionIsNotSpecified() {
        return direction == Direction.NONE;
    }

    private boolean foodIsEaten() {
        return Food.x == xCoords[0] && Food.y == yCoords[0];
    }

    private boolean snakeBitHerself() {
        for (int i = 1; i < length; i++) {
            if (xCoords[0] == xCoords[i] && yCoords[0] == yCoords[i]) return true;
        }
        return false;
    }

    private void moveHead() {
        switch (direction) {
            case RIGHT:
                if (xCoords[0] == cols - 1) xCoords[0] = 0;
                else xCoords[0]++;
                break;
            case LEFT:
                if (xCoords[0] == 0) xCoords[0] = cols - 1;
                else xCoords[0]--;
                break;
            case UP:
                if (yCoords[0] == 0) yCoords[0] = rows - 1;
                else yCoords[0]--;
                break;
            case DOWN:
                if (yCoords[0] == rows - 1) yCoords[0] = 0;
                else yCoords[0]++;
                break;
        }
    }

    private void moveBody() {
        for (int i = length - 1; i > 0; i--) {
            xCoords[i] = xCoords[i - 1];
            yCoords[i] = yCoords[i - 1];
        }
    }

    private void clearBody() {
        for (int i = 3; i < length; i++) {
            xCoords[i] = yCoords[i] = 0;
        }
        length = 3;
        newFood();
    }

    private void moveTail() {
        xCoords[length] = xCoords[length - 1];
        yCoords[length] = yCoords[length - 1];
    }

    static class Food {
        private static int x;
        private static int y;

        public static int getX() {
            return x;
        }

        public static int getY() {
            return y;
        }
    }
}