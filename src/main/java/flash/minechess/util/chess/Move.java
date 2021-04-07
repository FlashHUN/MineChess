package flash.minechess.util.chess;

public class Move {
  public static final class Flag {
    public static final int None = 0;
    public static final int EnPassantCapture = 1;
    public static final int Castling = 2;
    public static final int PromoteToQueen = 3;
    public static final int PromoteToKnight = 4;
    public static final int PromoteToRook = 5;
    public static final int PromoteToBishop = 6;
    public static final int PawnTwoForward = 7;
  }

  final int moveValue;

  static final int startSquareMask = 0b0000000000111111;
  static final int targetSquareMask = 0b0000111111000000;
  static final int flagMask = 0b1111000000000000;

  public Move(int moveValue) {
    this.moveValue = moveValue;
  }

  public Move(int startSquare, int targetSquare) {
    moveValue = (int) (startSquare | targetSquare << 6);
  }

  public Move(int startSquare, int targetSquare, int flag) {
    moveValue = (int) (startSquare | targetSquare << 6 | flag << 12);
  }

  public int getStartSquare() {
    return moveValue & startSquareMask;
  }

  public int getTargetSquare() {
    return (moveValue & targetSquareMask) >> 6;
  }

  public boolean isPromotion() {
    int flag = getMoveFlag();
    return flag == Flag.PromoteToQueen || flag == Flag.PromoteToRook || flag == Flag.PromoteToKnight || flag == Flag.PromoteToBishop;
  }

  public int getMoveFlag() {
    return moveValue >> 12;
  }

  public int getPromotionPieceType() {
    switch (getMoveFlag()) {
      case Flag.PromoteToRook:
        return Piece.Rook;
      case Flag.PromoteToKnight:
        return Piece.Knight;
      case Flag.PromoteToBishop:
        return Piece.Bishop;
      case Flag.PromoteToQueen:
        return Piece.Queen;
      default:
        return Piece.None;
    }
  }

  public static Move getInvalidMove() {
      return new Move (0);
  }

  public static boolean isSameMove(Move a, Move b) {
    return a.moveValue == b.moveValue;
  }

  public int getValue() {
    return moveValue;
  }

  public boolean isInvalid() {
      return moveValue == 0;
  }

  public String getName() {
    return BoardRepresentation.squareNameFromIndex(getStartSquare()) + "-" + BoardRepresentation.squareNameFromIndex(getTargetSquare());
  }
}
