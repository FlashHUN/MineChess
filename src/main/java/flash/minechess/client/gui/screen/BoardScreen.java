package flash.minechess.client.gui.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import flash.minechess.client.gui.widget.SquareWidget;
import flash.minechess.main.Main;
import flash.minechess.network.PacketDispatcher;
import flash.minechess.network.receive_server.PacketUpdateMatch;
import flash.minechess.util.Match;
import flash.minechess.util.chess.*;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class BoardScreen extends Screen {

  public static int whiteColor = 0xFFFFFFFF;
  public static int blackColor = 0xFF000000;
  public static int highlightedColor = 0xFFFFF38A;
  public static final int squareSize = 20;

  public boolean isWhitePlayer;

  private Match match;
  public Board board;
  public MoveGenerator moveGenerator;
  public Coord selectedSquare;

  public SquareWidget[] squares = new SquareWidget[64];

  public BoardScreen(Match match) {
    super(new TranslationTextComponent(Main.getScreenName("board")));
    this.match = match;
    this.board = match.getBoard();
    if (board.lastMadeMove != null) {
      highlightMove(board.lastMadeMove);
    }
    moveGenerator = new MoveGenerator();
  }

  @Override
  public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
    // Render the file and rank names on the sides of the board
    matrixStack.push();
    {
      matrixStack.translate(width/2-squareSize*4, height/2-squareSize*4, 0);

      matrixStack.translate(squareSize/2, -12, 0);
      for (int file = 0; file < 8; file++) {
        drawCenteredString(matrixStack, font, String.valueOf(BoardRepresentation.fileNames.charAt(file)), squareSize * file, 0, whiteColor);
      }
      matrixStack.translate(-squareSize/2-12, 18, 0);
      for(int rank = 7; rank >= 0; rank--) {
        int cRank = isWhitePlayer ? 8-rank : rank+1;
        drawString(matrixStack, font, String.valueOf(cRank), 0, squareSize*rank, whiteColor);
      }
    }
    matrixStack.pop();

    // Render match status on top of the board
    matrixStack.push();
    {
      matrixStack.translate(width/2, height/2-squareSize*4-30, 0);

      drawCenteredString(matrixStack, font, new TranslationTextComponent(Main.getScreenName("result."+match.getGameState().name().toLowerCase())), 0, 0, whiteColor);
    }
    matrixStack.pop();

    // Render who has to move on the side
    matrixStack.push();
    {
      matrixStack.translate(width/2+squareSize*4+8, height/2, 0);

      String turn = (board.whiteToMove && isWhitePlayer) || (!board.whiteToMove && !isWhitePlayer) ? "self" : "opponent";
      drawString(matrixStack, font, new TranslationTextComponent(Main.getScreenName("turn."+turn)), 0, 0, whiteColor);
    }
    matrixStack.pop();

    super.render(matrixStack, mouseX, mouseY, partialTicks);
  }

  @Override
  protected void init() {
    if (match != null) {
      isWhitePlayer = minecraft.player.getUniqueID().equals(match.getWhitePlayer());
      int x = width / 2 - squareSize * 4;
      int y = height / 2 - squareSize * 4;
      for (int squareIndex = 0; squareIndex < 64; squareIndex++) {
        int file = isWhitePlayer ? 7 - BoardRepresentation.fileIndex(squareIndex) : BoardRepresentation.fileIndex(squareIndex);
        int rank = isWhitePlayer ? 7 - BoardRepresentation.rankIndex(squareIndex) : BoardRepresentation.rankIndex(squareIndex);
        boolean lightSquare = BoardRepresentation.lightSquare(file, rank);
        squares[squareIndex] = this.addButton(new SquareWidget(this, x + file * squareSize, y + rank * squareSize, lightSquare, file, rank));
        squares[squareIndex].setPiece(board.square[squareIndex]);
      }
    }
  }

  @Override
  public void tick() {
  }

  public void highlightLegalMoves(Coord fromSquare) {
    List<Move> moves = moveGenerator.generateMoves(board);
    for (int i = 0; i < moves.size(); i++) {
      Move move = moves.get(i);
      int startSquare = isWhitePlayer ? 63 - move.getStartSquare() : move.getStartSquare();
      if (startSquare == BoardRepresentation.indexFromCoord(fromSquare)) {
        int targetSquare = isWhitePlayer ? 63 - move.getTargetSquare() : move.getTargetSquare();
        Coord coord = BoardRepresentation.coordFromIndex(targetSquare);
        setSquareColor(coord, true);
      }
    }
  }

  private void setSquareColor(Coord coord, boolean highlighted) {
    int squareIndex = indexFromCoord(coord);
    squares[squareIndex].setHighlighted(highlighted);
  }

  private void resetSquareColors() {
    resetSquareColors(true);
  }

  private void resetSquareColors(boolean highlight) {
    for (int rank = 0; rank < 8; rank++) {
      for (int file = 0; file < 8; file++) {
        setSquareColor(new Coord(file, rank), false);
      }
    }
  }

  public void selectSquare(Coord coord) {
    this.selectedSquare = coord;
    setSquareColor(coord, true);
  }

  public void deselectSquare() {
    this.selectedSquare = null;
    resetSquareColors();
  }

  private void highlightMove(Move move) {
    int startSquare = isWhitePlayer ? 63-move.getStartSquare() : move.getStartSquare();
    int targetSquare = isWhitePlayer ? 63-move.getTargetSquare() : move.getTargetSquare();
    setSquareColor(BoardRepresentation.coordFromIndex(startSquare), true);
    setSquareColor(BoardRepresentation.coordFromIndex(targetSquare), true);
  }

  private void updatePosition() {
    for (int rank = 0; rank < 8; rank++) {
      for (int file = 0; file < 8; file++) {
        Coord coord = new Coord(file, rank);
        int squareIndex = indexFromCoord(coord);
        int piece = board.square[squareIndex];
        squares[squareIndex].setPiece(piece);
      }
    }
  }

  public void onMoveMade(Board board, Move move) {
    this.board = board;
    updatePosition();
    resetSquareColors();
    highlightMove(move);
  }

  private int indexFromCoord(int file, int rank) {
    int index = BoardRepresentation.indexFromCoord(file, rank);
    return isWhitePlayer ? 63 - index : index;
  }

  private int indexFromCoord(Coord coord) {
    return indexFromCoord(coord.fileIndex, coord.rankIndex);
  }

  public void tryMakeMove(Coord targetSquare) {
    int startIndex = indexFromCoord(selectedSquare);
    int targetIndex = indexFromCoord(targetSquare);
    boolean moveIsLegal = false;
    Move chosenMove = null;

    List<Move> legalMoves = moveGenerator.generateMoves(board);
    for (int i = 0; i <legalMoves.size(); i++) {
      Move legalMove = legalMoves.get(i);

      if (legalMove.getStartSquare() == startIndex && legalMove.getTargetSquare() == targetIndex) {
        moveIsLegal = true;
        chosenMove = legalMove;
        break;
      }
    }

    if (moveIsLegal) {
      choseMove(chosenMove);
    } else {
      cancelPieceSelection();
    }
  }

  private void choseMove(Move move) {
    deselectSquare();
    PacketDispatcher.sendToServer(new PacketUpdateMatch(match.getWhitePlayer(), match.getBlackPlayer(), move));
  }

  private void cancelPieceSelection() {
    if (this.selectedSquare != null) {
      deselectSquare();
    }
  }
}
