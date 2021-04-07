package flash.minechess.util.chess;

import java.util.ArrayList;
import java.util.List;

import static flash.minechess.util.chess.BoardRepresentation.*;
import static flash.minechess.util.chess.PrecomputedMoveData.*;

public class MoveGenerator {

  public enum PromotionMode { All, QueenOnly, QueenAndKnight }

  public PromotionMode promotionsToGenerate = PromotionMode.All;

  // ---- Instance variables ----
  List<Move> moves;
  boolean isWhiteToMove;
  int friendlyColour;
  int opponentColour;
  int friendlyKingSquare;
  int friendlyColourIndex;
  int opponentColourIndex;

  boolean inCheck;
  boolean inDoubleCheck;
  boolean pinsExistInPosition;
  long checkRayBitmask;
  long pinRayBitmask;
  long opponentKnightAttacks;
  long opponentAttackMapNoPawns;
  public long opponentAttackMap;
  public long opponentPawnAttackMap;
  long opponentSlidingAttackMap;

  boolean genQuiets;
  Board board;

  // Generates list of legal moves in current position.
  // Quiet moves (non captures) can optionally be excluded. This is used in quiescence search.
  public List<Move> generateMoves(Board board) {
    return generateMoves(board, true);
  }

  public List<Move> generateMoves(Board board, boolean includeQuietMoves) {
    this.board = board;
    genQuiets = includeQuietMoves;
    init();

    calculateAttackData();
    generateKingMoves();

    // Only king moves are valid in a double check position, so can return early.
    if (inDoubleCheck) {
      return moves;
    }

    generateSlidingMoves();
    generateKnightMoves();
    generatePawnMoves();

    return moves;
  }

  // Note, this will only return correct value after GenerateMoves() has been called in the current position
  public boolean inCheck() {
    return inCheck;
  }

  void init() {
    moves = new ArrayList<>(64);
    inCheck = false;
    inDoubleCheck = false;
    pinsExistInPosition = false;
    checkRayBitmask = 0;
    pinRayBitmask = 0;

    isWhiteToMove = board.colourToMove == Piece.White;
    friendlyColour = board.colourToMove;
    opponentColour = board.opponentColour;
    friendlyKingSquare = board.kingSquare[board.colourToMoveIndex];
    friendlyColourIndex = (board.whiteToMove) ? Board.whiteIndex : Board.blackIndex;
    opponentColourIndex = 1 - friendlyColourIndex;
  }

  void generateKingMoves() {
    for (int i = 0; i < kingMoves[friendlyKingSquare].length; i++) {
      int targetSquare = kingMoves[friendlyKingSquare][i];
      int pieceOnTargetSquare = board.square[targetSquare];

      // Skip squares occupied by friendly pieces
      if (Piece.isColour (pieceOnTargetSquare, friendlyColour)) {
        continue;
      }

      boolean isCapture = Piece.isColour (pieceOnTargetSquare, opponentColour);
      if (!isCapture) {
        // King can't move to square marked as under enemy control, unless he is capturing that piece
        // Also skip if not generating quiet moves
        if (!genQuiets || squareIsInCheckRay(targetSquare)) {
          continue;
        }
      }

      // Safe for king to move to this square
      if (!squareIsAttacked(targetSquare)) {
        moves.add (new Move (friendlyKingSquare, targetSquare));

        // Castling:
        if (!inCheck && !isCapture) {
          // Castle kingside
          if ((targetSquare == c1 || targetSquare == c8) && hasKingsideCastleRight()) {
            int castleKingsideSquare = targetSquare - 1;
            if (board.square[castleKingsideSquare] == Piece.None) {
              if (!squareIsAttacked(castleKingsideSquare)) {
                moves.add (new Move (friendlyKingSquare, castleKingsideSquare, Move.Flag.Castling));
              }
            }
          }
          // Castle queenside
          else if ((targetSquare == e1 || targetSquare == e8) && hasQueensideCastleRight()) {
            int castleQueensideSquare = targetSquare + 1;
            if (board.square[castleQueensideSquare] == Piece.None && board.square[castleQueensideSquare + 1] == Piece.None) {
              if (!squareIsAttacked(castleQueensideSquare)) {
                moves.add (new Move (friendlyKingSquare, castleQueensideSquare, Move.Flag.Castling));
              }
            }
          }
        }
      }
    }
  }

  void generateSlidingMoves() {
    PieceList rooks = board.rooks[friendlyColourIndex];
    for (int i = 0; i < rooks.getCount(); i++) {
      generateSlidingPieceMoves(rooks.getOccupiedSquare(i), 0, 4);
    }

    PieceList bishops = board.bishops[friendlyColourIndex];
    for (int i = 0; i < bishops.getCount(); i++) {
      generateSlidingPieceMoves(bishops.getOccupiedSquare(i), 4, 8);
    }

    PieceList queens = board.queens[friendlyColourIndex];
    for (int i = 0; i < queens.getCount(); i++) {
      generateSlidingPieceMoves(queens.getOccupiedSquare(i), 0, 8);
    }

  }

  void generateSlidingPieceMoves(int startSquare, int startDirIndex, int endDirIndex) {
    boolean isPinned = isPinned(startSquare);

    // If this piece is pinned, and the king is in check, this piece cannot move
    if (inCheck && isPinned) {
      return;
    }

    for (int directionIndex = startDirIndex; directionIndex < endDirIndex; directionIndex++) {
      int currentDirOffset = directionOffsets[directionIndex];

      // If pinned, this piece can only move along the ray towards/away from the friendly king, so skip other directions
      if (isPinned && !isMovingAlongRay(currentDirOffset, friendlyKingSquare, startSquare)) {
        continue;
      }

      for (int n = 0; n < numSquaresToEdge[startSquare][directionIndex]; n++) {
        int targetSquare = startSquare + currentDirOffset * (n + 1);
        int targetSquarePiece = board.square[targetSquare];

        // Blocked by friendly piece, so stop looking in this direction
        if (Piece.isColour (targetSquarePiece, friendlyColour)) {
          break;
        }
        boolean isCapture = targetSquarePiece != Piece.None;

        boolean movePreventsCheck = squareIsInCheckRay(targetSquare);
        if (movePreventsCheck || !inCheck) {
          if (genQuiets || isCapture) {
            moves.add (new Move (startSquare, targetSquare));
          }
        }
        // If square not empty, can't move any further in this direction
        // Also, if this move blocked a check, further moves won't block the check
        if (isCapture || movePreventsCheck) {
          break;
        }
      }
    }
  }

  void generateKnightMoves() {
    PieceList myKnights = board.knights[friendlyColourIndex];

    for (int i = 0; i < myKnights.getCount(); i++) {
      int startSquare = myKnights.getOccupiedSquare(i);

      // Knight cannot move if it is pinned
      if (isPinned(startSquare)) {
        continue;
      }

      for (int knightMoveIndex = 0; knightMoveIndex < knightMoves[startSquare].length; knightMoveIndex++) {
        int targetSquare = knightMoves[startSquare][knightMoveIndex];
        int targetSquarePiece = board.square[targetSquare];
        boolean isCapture = Piece.isColour (targetSquarePiece, opponentColour);
        if (genQuiets || isCapture) {
          // Skip if square contains friendly piece, or if in check and knight is not interposing/capturing checking piece
          if (Piece.isColour (targetSquarePiece, friendlyColour) || (inCheck && !squareIsInCheckRay(targetSquare))) {
            continue;
          }
          moves.add (new Move (startSquare, targetSquare));
        }
      }
    }
  }

  void generatePawnMoves() {
    PieceList myPawns = board.pawns[friendlyColourIndex];
    int pawnOffset = (friendlyColour == Piece.White) ? 8 : -8;
    int startRank = (board.whiteToMove) ? 1 : 6;
    int finalRankBeforePromotion = (board.whiteToMove) ? 6 : 1;

    int enPassantFile = ((int) (board.currentGameState >> 4) & 15) - 1;
    int enPassantSquare = -1;
    if (enPassantFile != -1) {
      enPassantSquare = 8 * ((board.whiteToMove) ? 5 : 2) + enPassantFile;
    }

    for (int i = 0; i < myPawns.getCount(); i++) {
      int startSquare = myPawns.getOccupiedSquare(i);
      int rank = rankIndex (startSquare);
      boolean oneStepFromPromotion = rank == finalRankBeforePromotion;

      if (genQuiets) {

        int squareOneForward = startSquare + pawnOffset;

        // Square ahead of pawn is empty: forward moves
        if (board.square[squareOneForward] == Piece.None) {
          // Pawn not pinned, or is moving along line of pin
          if (!isPinned(startSquare) || isMovingAlongRay(pawnOffset, startSquare, friendlyKingSquare)) {
            // Not in check, or pawn is interposing checking piece
            if (!inCheck || squareIsInCheckRay(squareOneForward)) {
              if (oneStepFromPromotion) {
                makePromotionMoves(startSquare, squareOneForward);
              } else {
                moves.add (new Move (startSquare, squareOneForward));
              }
            }

            // Is on starting square (so can move two forward if not blocked)
            if (rank == startRank) {
              int squareTwoForward = squareOneForward + pawnOffset;
              if (board.square[squareTwoForward] == Piece.None) {
                // Not in check, or pawn is interposing checking piece
                if (!inCheck || squareIsInCheckRay(squareTwoForward)) {
                  moves.add (new Move (startSquare, squareTwoForward, Move.Flag.PawnTwoForward));
                }
              }
            }
          }
        }
      }

      // Pawn captures.
      for (int j = 0; j < 2; j++) {
        // Check if square exists diagonal to pawn
        if (numSquaresToEdge[startSquare][pawnAttackDirections[friendlyColourIndex][j]] > 0) {
          // move in direction friendly pawns attack to get square from which enemy pawn would attack
          int pawnCaptureDir = directionOffsets[pawnAttackDirections[friendlyColourIndex][j]];
          int targetSquare = startSquare + pawnCaptureDir;
          int targetPiece = board.square[targetSquare];

          // If piece is pinned, and the square it wants to move to is not on same line as the pin, then skip this direction
          if (isPinned(startSquare) && !isMovingAlongRay(pawnCaptureDir, friendlyKingSquare, startSquare)) {
            continue;
          }

          // Regular capture
          if (Piece.isColour (targetPiece, opponentColour)) {
            // If in check, and piece is not capturing/interposing the checking piece, then skip to next square
            if (inCheck && !squareIsInCheckRay(targetSquare)) {
              continue;
            }
            if (oneStepFromPromotion) {
              makePromotionMoves(startSquare, targetSquare);
            } else {
              moves.add (new Move (startSquare, targetSquare));
            }
          }

          // Capture en-passant
          if (targetSquare == enPassantSquare) {
            int epCapturedPawnSquare = targetSquare + ((board.whiteToMove) ? -8 : 8);
            if (!inCheckAfterEnPassant(startSquare, targetSquare, epCapturedPawnSquare)) {
              moves.add (new Move (startSquare, targetSquare, Move.Flag.EnPassantCapture));
            }
          }
        }
      }
    }
  }

  void makePromotionMoves(int fromSquare, int toSquare) {
    moves.add (new Move (fromSquare, toSquare, Move.Flag.PromoteToQueen));
    if (promotionsToGenerate == PromotionMode.All) {
      moves.add (new Move (fromSquare, toSquare, Move.Flag.PromoteToKnight));
      moves.add (new Move (fromSquare, toSquare, Move.Flag.PromoteToRook));
      moves.add (new Move (fromSquare, toSquare, Move.Flag.PromoteToBishop));
    } else if (promotionsToGenerate == PromotionMode.QueenAndKnight) {
      moves.add (new Move (fromSquare, toSquare, Move.Flag.PromoteToKnight));
    }

  }

  boolean isMovingAlongRay(int rayDir, int startSquare, int targetSquare) {
    int moveDir = directionLookup[targetSquare - startSquare + 63];
    return (rayDir == moveDir || -rayDir == moveDir);
  }

  //boolean isMovingAlongRay(int directionOffset, int absRayOffset) {
  //return !((directionOffset == 1 || directionOffset == -1) && absRayOffset >= 7) && absRayOffset % directionOffset == 0;
  //}

  boolean isPinned(int square) {
    return pinsExistInPosition && ((pinRayBitmask >> square) & 1) != 0;
  }

  boolean squareIsInCheckRay(int square) {
    return inCheck && ((checkRayBitmask >> square) & 1) != 0;
  }

  boolean hasKingsideCastleRight() {
    int mask = (board.whiteToMove) ? 1 : 4;
    return (board.currentGameState & mask) != 0;
  }

  boolean hasQueensideCastleRight() {
    int mask = (board.whiteToMove) ? 2 : 8;
    return (board.currentGameState & mask) != 0;
  }

  void genSlidingAttackMap() {
    opponentSlidingAttackMap = 0;

    PieceList enemyRooks = board.rooks[opponentColourIndex];
    for (int i = 0; i < enemyRooks.getCount(); i++) {
      updateSlidingAttackPiece(enemyRooks.getOccupiedSquare(i), 0, 4);
    }

    PieceList enemyQueens = board.queens[opponentColourIndex];
    for (int i = 0; i < enemyQueens.getCount(); i++) {
      updateSlidingAttackPiece(enemyQueens.getOccupiedSquare(i), 0, 8);
    }

    PieceList enemyBishops = board.bishops[opponentColourIndex];
    for (int i = 0; i < enemyBishops.getCount(); i++) {
      updateSlidingAttackPiece(enemyBishops.getOccupiedSquare(i), 4, 8);
    }
  }

  void updateSlidingAttackPiece(int startSquare, int startDirIndex, int endDirIndex) {

    for (int directionIndex = startDirIndex; directionIndex < endDirIndex; directionIndex++) {
      int currentDirOffset = directionOffsets[directionIndex];
      for (int n = 0; n < numSquaresToEdge[startSquare][directionIndex]; n++) {
        int targetSquare = startSquare + currentDirOffset * (n + 1);
        int targetSquarePiece = board.square[targetSquare];
        opponentSlidingAttackMap |= (long)1 << targetSquare;
        if (targetSquare != friendlyKingSquare) {
          if (targetSquarePiece != Piece.None) {
            break;
          }
        }
      }
    }
  }

  void calculateAttackData() {
    genSlidingAttackMap();
    // Search squares in all directions around friendly king for checks/pins by enemy sliding pieces (queen, rook, bishop)
    int startDirIndex = 0;
    int endDirIndex = 8;

    if (board.queens[opponentColourIndex].getCount() == 0) {
      startDirIndex = (board.rooks[opponentColourIndex].getCount() > 0) ? 0 : 4;
      endDirIndex = (board.bishops[opponentColourIndex].getCount() > 0) ? 8 : 4;
    }

    for (int dir = startDirIndex; dir < endDirIndex; dir++) {
      boolean isDiagonal = dir > 3;

      int n = numSquaresToEdge[friendlyKingSquare][dir];
      int directionOffset = directionOffsets[dir];
      boolean isFriendlyPieceAlongRay = false;
      long rayMask = 0;

      for (int i = 0; i < n; i++) {
        int squareIndex = friendlyKingSquare + directionOffset * (i + 1);
        rayMask |= (long)1 << squareIndex;
        int piece = board.square[squareIndex];

        // This square contains a piece
        if (piece != Piece.None) {
          if (Piece.isColour (piece, friendlyColour)) {
            // First friendly piece we have come across in this direction, so it might be pinned
            if (!isFriendlyPieceAlongRay) {
              isFriendlyPieceAlongRay = true;
            }
            // This is the second friendly piece we've found in this direction, therefore pin is not possible
            else {
              break;
            }
          }
          // This square contains an enemy piece
          else {
            int pieceType = Piece.pieceType (piece);

            // Check if piece is in bitmask of pieces able to move in current direction
            if (isDiagonal && Piece.isBishopOrQueen (pieceType) || !isDiagonal && Piece.isRookOrQueen (pieceType)) {
              // Friendly piece blocks the check, so this is a pin
              if (isFriendlyPieceAlongRay) {
                pinsExistInPosition = true;
                pinRayBitmask |= rayMask;
              }
              // No friendly piece blocking the attack, so this is a check
              else {
                checkRayBitmask |= rayMask;
                inDoubleCheck = inCheck; // if already in check, then this is double check
                inCheck = true;
              }
              break;
            } else {
              // This enemy piece is not able to move in the current direction, and so is blocking any checks/pins
              break;
            }
          }
        }
      }
      // Stop searching for pins if in double check, as the king is the only piece able to move in that case anyway
      if (inDoubleCheck) {
        break;
      }

    }

    // Knight attacks
    PieceList opponentKnights = board.knights[opponentColourIndex];
    opponentKnightAttacks = 0;
    boolean isKnightCheck = false;

    for (int knightIndex = 0; knightIndex < opponentKnights.getCount(); knightIndex++) {
      int startSquare = opponentKnights.getOccupiedSquare(knightIndex);
      opponentKnightAttacks |= knightAttackBitboards[startSquare];

      if (!isKnightCheck && BitBoardUtility.containsSquare(opponentKnightAttacks, friendlyKingSquare)) {
        isKnightCheck = true;
        inDoubleCheck = inCheck; // if already in check, then this is double check
        inCheck = true;
        checkRayBitmask |= (long)1 << startSquare;
      }
    }

    // Pawn attacks
    PieceList opponentPawns = board.pawns[opponentColourIndex];
    opponentPawnAttackMap = 0;
    boolean isPawnCheck = false;

    for (int pawnIndex = 0; pawnIndex < opponentPawns.getCount(); pawnIndex++) {
      int pawnSquare = opponentPawns.getOccupiedSquare(pawnIndex);
      long pawnAttacks = pawnAttackBitboards[pawnSquare][opponentColourIndex];
      opponentPawnAttackMap |= pawnAttacks;

      if (!isPawnCheck && BitBoardUtility.containsSquare(pawnAttacks, friendlyKingSquare)) {
        isPawnCheck = true;
        inDoubleCheck = inCheck; // if already in check, then this is double check
        inCheck = true;
        checkRayBitmask |= (long)1 << pawnSquare;
      }
    }

    int enemyKingSquare = board.kingSquare[opponentColourIndex];

    opponentAttackMapNoPawns = opponentSlidingAttackMap | opponentKnightAttacks | kingAttackBitboards[enemyKingSquare];
    opponentAttackMap = opponentAttackMapNoPawns | opponentPawnAttackMap;
  }

  boolean squareIsAttacked(int square) {
    return BitBoardUtility.containsSquare(opponentAttackMap, square);
  }

  boolean inCheckAfterEnPassant(int startSquare, int targetSquare, int epCapturedPawnSquare) {
    // Update board to reflect en-passant capture
    board.square[targetSquare] = board.square[startSquare];
    board.square[startSquare] = Piece.None;
    board.square[epCapturedPawnSquare] = Piece.None;

    boolean inCheckAfterEpCapture = false;
    if (squareAttackedAfterEPCapture(epCapturedPawnSquare, startSquare)) {
      inCheckAfterEpCapture = true;
    }

    // Undo change to board
    board.square[targetSquare] = Piece.None;
    board.square[startSquare] = Piece.Pawn | friendlyColour;
    board.square[epCapturedPawnSquare] = Piece.Pawn | opponentColour;
    return inCheckAfterEpCapture;
  }

  boolean squareAttackedAfterEPCapture(int epCaptureSquare, int capturingPawnStartSquare) {
    if (BitBoardUtility.containsSquare(opponentAttackMapNoPawns, friendlyKingSquare)) {
      return true;
    }

    // Loop through the horizontal direction towards ep capture to see if any enemy piece now attacks king
    int dirIndex = (epCaptureSquare < friendlyKingSquare) ? 2 : 3;
    for (int i = 0; i < numSquaresToEdge[friendlyKingSquare][dirIndex]; i++) {
      int squareIndex = friendlyKingSquare + directionOffsets[dirIndex] * (i + 1);
      int piece = board.square[squareIndex];
      if (piece != Piece.None) {
        // Friendly piece is blocking view of this square from the enemy.
        if (Piece.isColour (piece, friendlyColour)) {
          break;
        }
        // This square contains an enemy piece
        else {
          if (Piece.isRookOrQueen (piece)) {
            return true;
          } else {
            // This piece is not able to move in the current direction, and is therefore blocking any checks along this line
            break;
          }
        }
      }
    }

    // check if enemy pawn is controlling this square (can't use pawn attack bitboard, because pawn has been captured)
    for (int i = 0; i < 2; i++) {
      // Check if square exists diagonal to friendly king from which enemy pawn could be attacking it
      if (numSquaresToEdge[friendlyKingSquare][pawnAttackDirections[friendlyColourIndex][i]] > 0) {
        // move in direction friendly pawns attack to get square from which enemy pawn would attack
        int piece = board.square[friendlyKingSquare + directionOffsets[pawnAttackDirections[friendlyColourIndex][i]]];
        if (piece == (Piece.Pawn | opponentColour)) // is enemy pawn
        {
          return true;
        }
      }
    }

    return false;
  }
}
