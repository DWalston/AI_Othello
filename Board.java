/*    Othello Game Board for CSE 4301
 *    Created by Gregory Thompson and David Walston
 *    Class creates and runs an Othello game, X angainst O
 */
import java.util.ArrayList;
import java.util.Arrays;

public class Board {
   // the current pieces on the Board
   // 8x8 board of tiles (char), may be ' ', 'X', 'O', or '.'
   // ' ' means curently unused, '.' means adjacent to a used tile
   // 'X' and 'O' mean that player's marker is on the tile
   private Character[][] board;
   // list of directions, useful for iterating a tiles neighbors
   private final int[][] rota = {{1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}, {0, 1}};
   // the current player's move, changes once move is made
   private char currentMove;

   // creates a new object at the starting gamestate
   public Board () {
      board = new Character[8][8];
      currentMove = 'X';

      // fill empty board
      for (int i = 0; i < 8; i++) {
         for (int j = 0; j < 8; j++) {
            board[i][j] = ' ';
         }
      }
      
      // fill starting squares
      set(3, 3, 'O');
      set(3, 4, 'X');
      set(4, 3, 'X');
      set(4, 4, 'O');
   }
   // Constructs a new Board object identical to another Board object
   // parameter: Board to be copied
   public Board (Board copy) {
      board = copy.getArray();
      currentMove = copy.getCurrentMove();
   }

   // Attempts to make a move
   // Parameters: coordinates (starting from 0) of the move to be made
   // returns: true if successful, false if not
   // note: on a false return, the Board was not changed and currentPlayer remains the same
   public boolean makeMove(int x, int y) {
      if (x < 0 || x > 7 || y < 0 || y > 7)
        return false;
      // simple test if move is valid
      if (board[x][y] != '.') {
         return false;
      }

      // flag reccords if a direction was filled from the new move
      boolean flag = false;

      // for each direction
      for (int i = 0; i < 8; i++) {
         int[] shift = rota[i];

         // if that direction can be filled
         if (firstScan(x, y, shift[0], shift[1], currentMove)) {
            // fill that direction
            fill(x, y, shift[0], shift[1], currentMove);
            // mark a move as having been made
            flag = true;
         }
      }

      // if a move was made
      if (flag) {
         // mark the tile
         set(x, y, currentMove);
         // next players move
         currentMove = otherPlayer(currentMove);
      }

      // return
      return flag;
   }

   // return a list of all possible moves for a player
   // parameter: the player being checked
   // returns: an array on moves. Each move is an XY pair in an array
   public int[][] getMoves(char player) {
      ArrayList<int[]> out = new ArrayList<int[]>();
      // for each tile
      for (int i = 0; i < 8; i++) {
         for (int j = 0; j < 8; j++) {
            if (board[i][j] != '.') {
               continue;
            }

            // add to output
            if(check(i, j, player)) {
               int[] temp = new int[2];
               temp[0] = i;
               temp[1] = j;
               out.add(temp);
            }
         }
      }

      // convert to array
      int[][] ignoreThis = new int[out.size()][];
      return out.toArray(ignoreThis);
   }

   // returns the number of tiles marked by a player
   public int countTiles(char player) {
      int count = 0;
      for (int i = 0; i < 8; i++) {
         for (int j = 0; j < 8; j++) {
            if(board[i][j] == player) {
               count++;
            }
         }
      }
      return count;
   }

   // returns a printable String representation of the Board
   public String toString() {
      String out = "";
      for (int i = 0; i < 8; i++) {
         for (int j = 0; j < 8; j++) {
            out += board[j][i];
         }
         out += "\n";
      }
      return out;
   }
   // board accessor
   public Character[][] getArray() {
      Character[][] newBoard = board.clone();
      for (int i = 0; i < newBoard.length; i++) {
          newBoard[i] = board[i].clone();
      }
          
      return newBoard;
   }
   // currentMove accessor
   public char getCurrentMove() {
      return currentMove;
   }

   // sets empty spaces(' ') arround a tile as '.'
   // sets tile for a player
   // parameters: coordinates to be set and the player to give the tile to
   private void set(int x, int y, char player) {
      board[x][y] = player;
      for (int i = 0; i < 8; i++) {
         int[] shift = rota[i];
         int[] newXY = {x + shift[0], y + shift[1]};
         if (!(newXY[0] < 0 || newXY[0] > 7 || newXY[1] < 0 || newXY[1] > 7)) {
            if (board[newXY[0]][newXY[1]] == ' ') {
               board[newXY[0]][newXY[1]] = '.';
            }
         }
      }
   }
   // recursively fills tiles in a line until reaching one of it's own
   // parameters: starting coordinate(not changed), direction, and player to give tiles
   private void fill(int x, int y, int xc, int yc, char player) {
      int testX = x + xc;
      int testY = y + yc;
      if(board[testX][testY] == otherPlayer(player)) {
         board[testX][testY] = player;
         fill(testX, testY, xc, yc, player);
      }
   }

   // tests if a tile is a valid move
   // parameters: tile to check and player who would move there
   // returns true if valid, false if not
   private boolean check(int x, int y, char player) {
      // input checking
      if (x < 0 || x > 7 || y < 0 || y > 7) {
         return false;
      }
      if (board[x][y] == 'O' || board[x][y] == 'X') {
         return false;
      }

      // check if each direction is a line of opponent pieces followed by a friendly
      for (int i = 0; i < 8; i++) {
         int[] shift = rota[i];
         int[] newXY = {x + shift[0], y + shift[1]};
         if (!(newXY[0] < 0 || newXY[0] > 7 || newXY[1] < 0 || newXY[1] > 7)) {
            if (board[newXY[0]][newXY[1]] == otherPlayer(player)) {
                if (scan(newXY[0], newXY[1], shift[0], shift[1], player)) {
                  // escape
                  return true;
               }
            }
         }
      }

      // no escape
      return false;
   }
   // same as scan, but checks the starting value
   private boolean firstScan (int x, int y, int xc, int yc, char player) {
      int testX = x + xc;
      int testY = y + yc;
      if (testX < 0 || testY < 0)
          return false;
      else if (testX > 7 || testY > 7)
          return false;
      else if(board[testX][testY] == otherPlayer(player)) {
         return scan (testX, testY, xc, yc, player);
      }

      return false;
   }
   // recursive check to see if a direction is a line of opponents followed by a friendly
   // assumes xy points to a friendly piece
   // parameters coordinates of start point, direction, and querying player
   private boolean scan (int x, int y, int xc, int yc, char player) {
      int testX = x + xc;
      int testY = y + yc;
      // hit the edge
      if (testX < 0 || testX > 7 || testY < 0 || testY > 7) {
         return false;
      }

      // hit friendly
      if (board[testX][testY] == player) {
         return true;
      }

      // hit enemy, recursive call
      if (board[testX][testY] == otherPlayer(player)) {
         return scan (testX, testY, xc, yc, player);
      }

      // hit other (probably '.')
      return false;
   }

   // converts 'X' to 'O' and vice versa
   // other chars return ' '
   private char otherPlayer(char input) {
      if (input == 'O') {
         return 'X';
      } else if (input == 'X') {
         return 'O';
      } else {
         return ' ';
      }
   }

   // for testing
   /*
   public static void main(String[] args) {
      Board test = new Board();
      System.out.println(test.toString());
      test.makeMove(2, 3);
      System.out.println(test.toString());
      test.makeMove(2, 4);
      System.out.println(test.toString());
      test.makeMove(3, 5);
      System.out.println(test.toString());
      System.out.println(Arrays.deepToString(test.getMoves('X')));
      System.out.println(test.countTiles('X'));
   }*/
}
