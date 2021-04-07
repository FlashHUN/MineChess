package flash.minechess.util.chess;

public class PieceList {
  // Indices of squares occupied by given piece type (only elements up to Count are valid, the rest are unused/garbage)
  public int[] occupiedSquares;
  // Map to go from index of a square, to the index in the occupiedSquares array where that square is stored
  int[] map;
  int numPieces;

  public PieceList() {
    this(16);
  }

  public PieceList(int maxPieceCount) {
    occupiedSquares = new int[maxPieceCount];
    map = new int[64];
    numPieces = 0;
  }

  public int getCount() {
    return numPieces;
  }

  public void addPieceAtSquare(int square) {
    occupiedSquares[numPieces] = square;
    map[square] = numPieces;
    numPieces++;
  }

  public void removePieceAtSquare(int square) {
    int pieceIndex = map[square]; // get the index of this element in the occupiedSquares array
    occupiedSquares[pieceIndex] = occupiedSquares[numPieces - 1]; // move last element in array to the place of the removed element
    map[occupiedSquares[pieceIndex]] = pieceIndex; // update map to point to the moved element's new location in the array
    numPieces--;
  }

  public void movePiece(int startSquare, int targetSquare) {
    int pieceIndex = map[startSquare]; // get the index of this element in the occupiedSquares array
    occupiedSquares[pieceIndex] = targetSquare;
    map[targetSquare] = pieceIndex;
  }

  public int getOccupiedSquare(int index) {
    return occupiedSquares[index];
  }
}
