package flash.minechess.util.chess;

public class Coord implements Comparable<Coord> {
  public final int fileIndex;
  public final int rankIndex;

  public Coord(int fileIndex, int rankIndex) {
    this.fileIndex = fileIndex;
    this.rankIndex = rankIndex;
  }

  public boolean isLightSquare() {
    return (fileIndex + rankIndex) % 2 != 0;
  }

  public int compareTo(Coord other) {
    return (fileIndex == other.fileIndex && rankIndex == other.rankIndex) ? 0 : 1;
  }
}
