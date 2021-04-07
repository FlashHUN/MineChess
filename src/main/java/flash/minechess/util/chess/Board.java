package flash.minechess.util.chess;

public class Board {

  public static final int whiteIndex = 0;
  public static final int blackIndex = 1;

  // Stores piece code for each square on the board.
  // Piece code is defined as piecetype | colour code
  public int[] square;

  public boolean whiteToMove;
  public int colourToMove;
  public int opponentColour;
  public int colourToMoveIndex;

  public boolean lastMadeMoveIsCapture;
  public Move lastMadeMove;

  public long currentGameState;

  public int plyCount; // Total plies played in game
  public int fiftyMoveCounter; // Num ply since last pawn move or capture

  public int[] kingSquare; // index of square of white and black king

  public PieceList[] rooks;
  public PieceList[] bishops;
  public PieceList[] queens;
  public PieceList[] knights;
  public PieceList[] pawns;

  PieceList[] allPieceLists;

  static final long whiteCastleKingsideMask = 0b1111111111111101;
  static final long whiteCastleQueensideMask = 0b1111111111111110;
  static final long blackCastleKingsideMask = 0b1111111111110111;
  static final long blackCastleQueensideMask = 0b1111111111111011;

  static final long whiteCastleMask = whiteCastleKingsideMask & whiteCastleQueensideMask;
  static final long blackCastleMask = blackCastleKingsideMask & blackCastleQueensideMask;

  PieceList getPieceList(int pieceType, int colourIndex) {
    return allPieceLists[colourIndex * 8 + pieceType];
  }

  // Make a move on the board
  // The inSearch parameter controls whether this move should be recorded in the game history (for detecting three-fold repetition)
  public void makeMove(Move move) {
    this.makeMove(move, false);
  }

  public void makeMove(Move move, boolean inSearch) {
    long originalCastleState = currentGameState & 15;
    long newCastleState = originalCastleState;
    currentGameState = 0;

    int opponentColourIndex = 1 - colourToMoveIndex;
    int moveFrom = move.getStartSquare();
    int moveTo = move.getTargetSquare();

    int capturedPieceType = Piece.pieceType (square[moveTo]);
    int movePiece = square[moveFrom];
    int movePieceType = Piece.pieceType (movePiece);

    int moveFlag = move.getMoveFlag();
    boolean isPromotion = move.isPromotion();
    boolean isEnPassant = moveFlag == Move.Flag.EnPassantCapture;

    // Handle captures
    currentGameState |= (capturedPieceType << 8);
    if (capturedPieceType != 0 && !isEnPassant) {
      getPieceList(capturedPieceType, opponentColourIndex).removePieceAtSquare(moveTo);
    }

    // Move pieces in piece lists
    if (movePieceType == Piece.King) {
      kingSquare[colourToMoveIndex] = moveTo;
      newCastleState &= (whiteToMove) ? whiteCastleMask : blackCastleMask;
    } else {
      getPieceList(movePieceType, colourToMoveIndex).movePiece(moveFrom, moveTo);
    }

    int pieceOnTargetSquare = movePiece;

    // Handle promotion
    if (isPromotion) {
      int promoteType = 0;
      switch (moveFlag) {
        case Move.Flag.PromoteToQueen:
          promoteType = Piece.Queen;
          queens[colourToMoveIndex].addPieceAtSquare(moveTo);
          break;
        case Move.Flag.PromoteToRook:
          promoteType = Piece.Rook;
          rooks[colourToMoveIndex].addPieceAtSquare(moveTo);
          break;
        case Move.Flag.PromoteToBishop:
          promoteType = Piece.Bishop;
          bishops[colourToMoveIndex].addPieceAtSquare(moveTo);
          break;
        case Move.Flag.PromoteToKnight:
          promoteType = Piece.Knight;
          knights[colourToMoveIndex].addPieceAtSquare(moveTo);
          break;

      }
      pieceOnTargetSquare = promoteType | colourToMove;
      pawns[colourToMoveIndex].removePieceAtSquare(moveTo);
    } else {
      // Handle other special moves (en-passant, and castling)
      switch (moveFlag) {
        case Move.Flag.EnPassantCapture:
          int epPawnSquare = moveTo + ((colourToMove == Piece.White) ? -8 : 8);
          currentGameState |= (square[epPawnSquare] << 8); // add pawn as capture type
          square[epPawnSquare] = 0; // clear ep capture square
          pawns[opponentColourIndex].removePieceAtSquare(epPawnSquare);
          break;
        case Move.Flag.Castling:
          boolean kingside = moveTo == BoardRepresentation.b1 || moveTo == BoardRepresentation.b8;
          int castlingRookFromIndex = (kingside) ? moveTo - 1 : moveTo + 2;
          int castlingRookToIndex = (kingside) ? moveTo + 1 : moveTo - 1;

          square[castlingRookFromIndex] = Piece.None;
          square[castlingRookToIndex] = Piece.Rook | colourToMove;

          rooks[colourToMoveIndex].movePiece(castlingRookFromIndex, castlingRookToIndex);
          break;
      }
    }

    // Update the board representation:
    square[moveTo] = pieceOnTargetSquare;
    square[moveFrom] = 0;

    // Pawn has moved two forwards, mark file with en-passant flag
    if (moveFlag == Move.Flag.PawnTwoForward) {
      int file = BoardRepresentation.fileIndex (moveFrom) + 1;
      currentGameState |= (file << 4);
    }

    // Piece moving to/from rook square removes castling right for that side
    if (originalCastleState != 0) {
      if (moveTo == BoardRepresentation.h1 || moveFrom == BoardRepresentation.h1) {
        newCastleState &= whiteCastleKingsideMask;
      } else if (moveTo == BoardRepresentation.a1 || moveFrom == BoardRepresentation.a1) {
        newCastleState &= whiteCastleQueensideMask;
      }
      if (moveTo == BoardRepresentation.h8 || moveFrom == BoardRepresentation.h8) {
        newCastleState &= blackCastleKingsideMask;
      } else if (moveTo == BoardRepresentation.a8 || moveFrom == BoardRepresentation.a8) {
        newCastleState &= blackCastleQueensideMask;
      }
    }

    currentGameState |= newCastleState;
    currentGameState |= (long) fiftyMoveCounter << 14;

    // Change side to move
    whiteToMove = !whiteToMove;
    colourToMove = (whiteToMove) ? Piece.White : Piece.Black;
    opponentColour = (whiteToMove) ? Piece.Black : Piece.White;
    colourToMoveIndex = 1 - colourToMoveIndex;
    plyCount++;
    fiftyMoveCounter++;

    if (!inSearch) {
      if (movePieceType == Piece.Pawn || capturedPieceType != Piece.None) {
        fiftyMoveCounter = 0;
      }
    }

    if (capturedPieceType != Piece.None) {
      lastMadeMoveIsCapture = true;
    } else {
      lastMadeMoveIsCapture = false;
    }
    lastMadeMove = move;

  }

  // Load the starting position
  public void loadStartPosition() {
    loadPosition(FenUtility.startFen);
  }

  // Load custom position from fen string
  public void loadPosition(String fen) {
    initialize();
    FenUtility.LoadedPositionInfo loadedPosition = FenUtility.positionFromFen(fen);

    // Load pieces into board array and piece lists
    for (int squareIndex = 0; squareIndex < 64; squareIndex++) {
      int piece = loadedPosition.squares[squareIndex];
      square[squareIndex] = piece;

      if (piece != Piece.None) {
        int pieceType = Piece.pieceType (piece);
        int pieceColourIndex = (Piece.isColour (piece, Piece.White)) ? whiteIndex : blackIndex;
        if (Piece.isSlidingPiece (piece)) {
          if (pieceType == Piece.Queen) {
            queens[pieceColourIndex].addPieceAtSquare(squareIndex);
          } else if (pieceType == Piece.Rook) {
            rooks[pieceColourIndex].addPieceAtSquare(squareIndex);
          } else if (pieceType == Piece.Bishop) {
            bishops[pieceColourIndex].addPieceAtSquare(squareIndex);
          }
        } else if (pieceType == Piece.Knight) {
          knights[pieceColourIndex].addPieceAtSquare(squareIndex);
        } else if (pieceType == Piece.Pawn) {
          pawns[pieceColourIndex].addPieceAtSquare(squareIndex);
        } else if (pieceType == Piece.King) {
          kingSquare[pieceColourIndex] = squareIndex;
        }
      }
    }

    // Side to move
    whiteToMove = loadedPosition.whiteToMove;
    colourToMove = (whiteToMove) ? Piece.White : Piece.Black;
    opponentColour = (whiteToMove) ? Piece.Black : Piece.White;
    colourToMoveIndex = (whiteToMove) ? 0 : 1;

    // Create gamestate
    int whiteCastle = ((loadedPosition.whiteCastleKingside) ? 1 << 1 : 0) | ((loadedPosition.whiteCastleQueenside) ? 1 << 0 : 0);
    int blackCastle = ((loadedPosition.blackCastleKingside) ? 1 << 3 : 0) | ((loadedPosition.blackCastleQueenside) ? 1 << 2 : 0);
    int epState = loadedPosition.epFile << 4;
    int initialGameState = (int) (whiteCastle | blackCastle | epState);
    currentGameState = initialGameState;
    plyCount = loadedPosition.plyCount;
  }

  void initialize() {
    square = new int[64];
    kingSquare = new int[2];

    lastMadeMove = null;

    plyCount = 0;
    fiftyMoveCounter = 0;

    knights = new PieceList[] { new PieceList (10), new PieceList (10) };
    pawns = new PieceList[] { new PieceList (8), new PieceList (8) };
    rooks = new PieceList[] { new PieceList (10), new PieceList (10) };
    bishops = new PieceList[] { new PieceList (10), new PieceList (10) };
    queens = new PieceList[] { new PieceList (9), new PieceList (9) };
    PieceList emptyList = new PieceList (0);
    allPieceLists = new PieceList[] {
        emptyList,
        emptyList,
        pawns[whiteIndex],
        knights[whiteIndex],
        emptyList,
        bishops[whiteIndex],
        rooks[whiteIndex],
        queens[whiteIndex],
        emptyList,
        emptyList,
        pawns[blackIndex],
        knights[blackIndex],
        emptyList,
        bishops[blackIndex],
        rooks[blackIndex],
        queens[blackIndex],
    };
  }
}
