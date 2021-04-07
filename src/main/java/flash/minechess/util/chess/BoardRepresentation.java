package flash.minechess.util.chess;

public class BoardRepresentation {

  public static final String fileNames = "abcdefgh";
  public static final String rankNames = "12345678";

  public static final int a1 = 0;
  public static final int b1 = 1;
  public static final int c1 = 2;
  public static final int d1 = 3;
  public static final int e1 = 4;
  public static final int f1 = 5;
  public static final int g1 = 6;
  public static final int h1 = 7;

  public static final int a8 = 56;
  public static final int b8 = 57;
  public static final int c8 = 58;
  public static final int d8 = 59;
  public static final int e8 = 60;
  public static final int f8 = 61;
  public static final int g8 = 62;
  public static final int h8 = 63;

  // Rank (0 to 7) of square
  public static int rankIndex(int squareIndex) {
    return squareIndex >> 3;
  }

  // File (0 to 7) of square
  public static int fileIndex(int squareIndex) {
    return squareIndex & 0b000111;
  }

  public static int indexFromCoord(int fileIndex, int rankIndex) {
    return (rankIndex * 8) + fileIndex;
  }

  public static int indexFromCoord(Coord coord) {
    return indexFromCoord (coord.fileIndex, coord.rankIndex);
  }

  public static Coord coordFromIndex(int squareIndex) {
    return new Coord (fileIndex (squareIndex), rankIndex (squareIndex));
  }

  public static boolean lightSquare(int fileIndex, int rankIndex) {
    return (fileIndex + rankIndex) % 2 == 0;
  }

  public static String squareNameFromCoordinate(int fileIndex, int rankIndex) {
    return String.valueOf(fileNames.charAt(fileIndex)) + "" + (rankIndex + 1);
  }

  public static String squareNameFromIndex(int squareIndex) {
    return squareNameFromCoordinate(coordFromIndex (squareIndex));
  }

  public static String squareNameFromCoordinate(Coord coord) {
    return squareNameFromCoordinate(coord.fileIndex, coord.rankIndex);
  }

}
