package flash.minechess.client.gui.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import flash.minechess.client.gui.widget.ColorPickerWidget;
import flash.minechess.client.gui.widget.ColorSquareWidget;
import flash.minechess.client.gui.widget.HueSliderWidget;
import flash.minechess.main.Main;
import flash.minechess.util.ColorUtil;
import flash.minechess.util.chess.BoardRepresentation;
import flash.minechess.util.chess.Piece;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import static flash.minechess.client.gui.screen.BoardScreen.squareSize;

@OnlyIn(Dist.CLIENT)
public class ColorPickerScreen extends Screen {

  ColorPickerWidget colorPickerWidget;
  HueSliderWidget hueSliderWidget;
  ColorSquareWidget[] squares;

  public static int whiteColor = 0xFFFFFFFF;
  public static int blackColor = 0xFF000000;
  public static int highlightedColor = 0xFFFFF38A;

  private int currentlyEditing;

  public ColorPickerScreen() {
    super(new TranslationTextComponent(Main.getScreenName("colorpicker")));
    squares = new ColorSquareWidget[16];
    currentlyEditing = 0;
  }

  @Override
  public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
    super.render(matrixStack, mouseX, mouseY, partialTicks);
    int editButtonX = width/4+squareSize*2;
    int editButtonY = height/2+squareSize*3+squareSize/2-font.FONT_HEIGHT/2;
    drawCenteredString(matrixStack, font, new TranslationTextComponent(Main.getScreenName("color.edit"+this.currentlyEditing)), editButtonX, editButtonY, 0xFFFFFF);
  }

  @Override
  protected void init() {
    int w = 150;
    int h = 150;
    int x = width/4*3-w/2;
    int y = height/2-h/2;
    colorPickerWidget = this.addButton(new ColorPickerWidget(x, y, w, h));
    hueSliderWidget = this.addButton(new HueSliderWidget(x+w+10, y, 20, h));

    int squareX = width/4;
    int squareY = height/2-squareSize*2;
    for (int i = 0; i < squares.length; i++) {
      int x1 = i >> 2;
      int y1 = i & 0b0011;
      squares[i] = this.addButton(new ColorSquareWidget(squareX+y1*squareSize, squareY+x1*squareSize, BoardRepresentation.lightSquare(y1, x1), y1, x1));
    }
    squares[0].setPiece(Piece.Rook | Piece.Black);
    squares[1].setPiece(Piece.King | Piece.Black);
    squares[2].setPiece(Piece.Queen | Piece.Black);
    squares[3].setPiece(Piece.Pawn | Piece.Black);
    squares[12].setPiece(Piece.Rook | Piece.White);
    squares[13].setPiece(Piece.King | Piece.White);
    squares[14].setPiece(Piece.Queen | Piece.White);
    squares[15].setPiece(Piece.Pawn | Piece.White);

    squares[3].setHighlighted(true);
    squares[7].setHighlighted(true);
    squares[11].setHighlighted(true);
    squares[15].setHighlighted(true);

    this.addButton(new Button(squareX, squareY+squareSize*5, squareSize*4, squareSize, StringTextComponent.EMPTY, press -> {
      if (this.currentlyEditing == 0 || this.currentlyEditing == 3) {
        colorPickerWidget.setHueColor(whiteColor);
        colorPickerWidget.setSelectedColor(whiteColor);
        hueSliderWidget.setHueColor(whiteColor);
        this.currentlyEditing = 1;
      } else if (this.currentlyEditing == 1) {
        colorPickerWidget.setHueColor(blackColor);
        colorPickerWidget.setSelectedColor(blackColor);
        hueSliderWidget.setHueColor(blackColor);
        this.currentlyEditing = 2;
      } else {
        colorPickerWidget.setHueColor(highlightedColor);
        colorPickerWidget.setSelectedColor(highlightedColor);
        hueSliderWidget.setHueColor(highlightedColor);
        this.currentlyEditing = 3;
      }
    }));

    this.addButton(new Button(squareX, squareY-squareSize*2, squareSize*4, squareSize, new TranslationTextComponent(Main.getScreenName("color.save")), press -> {
      ColorUtil.save(whiteColor, blackColor, highlightedColor);
      BoardScreen.whiteColor = whiteColor;
      BoardScreen.blackColor = blackColor;
      BoardScreen.highlightedColor = highlightedColor;
      this.closeScreen();
    }));
  }

  @Override
  public void tick() {
    int sliderHue = hueSliderWidget.getHueColor();
    if (colorPickerWidget.getHueColor() != sliderHue) {
      colorPickerWidget.setHueColor(hueSliderWidget.getHueColor());
      colorPickerWidget.getColorAtMarker();
    }
    if (this.currentlyEditing == 1 && whiteColor != colorPickerWidget.getSelectedColor()) {
      whiteColor = colorPickerWidget.getSelectedColor();
    }
    if (this.currentlyEditing == 2 && blackColor != colorPickerWidget.getSelectedColor()) {
      blackColor = colorPickerWidget.getSelectedColor();
    }
    if (this.currentlyEditing == 3 && highlightedColor != colorPickerWidget.getSelectedColor()) {
      highlightedColor = colorPickerWidget.getSelectedColor();
    }
  }


}
