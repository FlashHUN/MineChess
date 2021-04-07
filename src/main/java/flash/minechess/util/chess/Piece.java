package flash.minechess.util.chess;

public class Piece {

  public static final int None = 0;
  public static final int King = 1;
  public static final int Pawn = 2;
  public static final int Knight = 3;
  public static final int Bishop = 5;
  public static final int Rook = 6;
  public static final int Queen = 7;

  public static final int White = 8;
  public static final int Black = 16;

  static final int typeMask = 0b00111;
  static final int blackMask = 0b10000;
  static final int whiteMask = 0b01000;
  static final int colourMask = whiteMask | blackMask;

  public static boolean isColour(int piece, int colour) {
    return (piece & colourMask) == colour;
  }

  public static int colour(int piece) {
    return piece & colourMask;
  }

  public static int pieceType(int piece) {
    return piece & typeMask;
  }

  public static boolean isRookOrQueen(int piece) {
    return (piece & 0b110) == 0b110;
  }

  public static boolean isBishopOrQueen(int piece) {
    return (piece & 0b101) == 0b101;
  }

  public static boolean isSlidingPiece(int piece) {
    return (piece & 0b100) != 0;
  }

}
