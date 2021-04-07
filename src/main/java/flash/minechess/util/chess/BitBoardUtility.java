package flash.minechess.util.chess;

public class BitBoardUtility {
  public static boolean containsSquare(long bitboard, int square) {
    return ((bitboard >> square) & 1) != 0;
  }
}
