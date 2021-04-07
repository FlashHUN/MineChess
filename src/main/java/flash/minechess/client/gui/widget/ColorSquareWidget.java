package flash.minechess.client.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import flash.minechess.main.Main;
import flash.minechess.util.ScreenHelper;
import flash.minechess.util.chess.Piece;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import static flash.minechess.client.gui.screen.BoardScreen.squareSize;
import static flash.minechess.client.gui.screen.ColorPickerScreen.*;

@OnlyIn(Dist.CLIENT)
public class ColorSquareWidget extends Button {

  private static final ResourceLocation PIECES = new ResourceLocation(Main.MODID, "textures/gui/pieces.png");

  private int pieceType, pieceColor;
  private int file, rank;
  private boolean lightSquare, highlighted;

  public ColorSquareWidget(int x, int y, boolean lightSquare, int file, int rank) {
    super(x, y, squareSize, squareSize, StringTextComponent.EMPTY, pressed -> {});

    this.file = file;
    this.rank = rank;
    this.pieceType = Piece.None;
    this.pieceColor = Piece.None;

    this.lightSquare = lightSquare;
  }

  public void setPiece(int piece) {
    this.pieceType = Piece.pieceType(piece);
    this.pieceColor = Piece.colour(piece);
  }

  public void setHighlighted(boolean highlighted) {
    this.highlighted = highlighted;
  }

  private int getColor() {
    return highlighted ? (lightSquare ? highlightedColor : ScreenHelper.colorLightness(highlightedColor, -40)) : (lightSquare ? whiteColor : blackColor);
  }

  @Override
  public void renderWidget(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
    fill(matrixStack, x, y, x+width, y+height, getColor());
    if (pieceType != 0) {
      Minecraft minecraft = Minecraft.getInstance();
      minecraft.getTextureManager().bindTexture(PIECES);
      int colorHEX = pieceColor == Piece.White ? whiteColor : ScreenHelper.colorLightness(blackColor, 40);
      int r = (colorHEX & 0xFF0000) >> 16;
      int g = (colorHEX & 0xFF00) >> 8;
      int b = colorHEX & 0xFF;
      RenderSystem.color4f(r / 255f, g / 255f, b / 255f, 1f);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.enableDepthTest();
      int uOffset = pieceType < 4 ? pieceType - 1 : pieceType - 2;
      blit(matrixStack, x, y, squareSize, squareSize, uOffset*200, 0, 200, 200, 1200, 200);
    }
  }

  @Override
  public void onPress() {

  }

  @Override
  public void playDownSound(SoundHandler handler) {
  }

}
