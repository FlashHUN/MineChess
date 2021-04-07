package flash.minechess.client.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HueSliderWidget extends Button {
  public static int[] colors = new int[] { 0xFFFF0000, 0xFFFFFF00, 0xFF00FF00, 0xFF00FFFF, 0xFF0000FF, 0xFFFF00FF, 0xFFFF0000 };

  private int hueColor;
  private float markerPosY;

  public HueSliderWidget(int x, int y, int width, int height) {
    super(x, y, width, height, StringTextComponent.EMPTY, press -> {});
    this.hueColor = 0xFFFF0000;
    this.markerPosY = 0;
  }

  public int getHueColor() {
    return hueColor;
  }

  public void setHueColor(int hueColor) {
    this.hueColor = hueColor;
  }

  @Override
  public void renderWidget(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
    fill(matrixStack, x-1, y-1, x+width+1, y+height+1, 0xFF000000);
    int sectionHeight = height/6;
    for (int i = 0; i < 6; i++) {
      fillGradient(matrixStack, x, y + sectionHeight * i, x + width, y + sectionHeight * (i+1), colors[i], colors[i + 1]);
    }
    renderMarker(matrixStack);
  }

  private void renderMarker(MatrixStack matrixStack) {
    float hY = y+markerPosY;
    matrixStack.push();
    {
      matrixStack.translate(0, hY-0.5, 0);
      hLine(matrixStack, x, x+width, 0, 0xFF000000);
    }
    matrixStack.pop();
  }

  @Override
  public void onClick(double mouseX, double mouseY) {
    getHue(mouseY);
  }

  protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
    getHue(mouseY);
  }

  private void getHue(double mouseY) {
    double mY = mouseY;
    if (mouseY < y) {
      mY = y;
    } else if (mouseY > y+height) {
      mY = y+height;
    }
    float colorY = (float) ((mY-y)/height*6);
    int hueIndex = (int) Math.floor(colorY);
    float hue = colorY-hueIndex;

    int color = colors[hueIndex];
    int color2 = 0xFFFF0000;
    if (hueIndex+1 < 7) {
      color2 = colors[hueIndex+1];
    }
    int r1 = (color & 0xFF0000) >> 16;
    int g1 = (color & 0xFF00) >> 8;
    int b1 = (color & 0xFF);
    int r2 = (color2 & 0xFF0000) >> 16;
    int g2 = (color2 & 0xFF00) >> 8;
    int b2 = (color2 & 0xFF);
    if (hueIndex % 2 != 0) {
      hue = 1-hue;
    }
    if (r1 != r2) {
      r1 = (int) (255 * (hue));
    }
    if (g1 != g2) {
      g1 = (int) (255 * (hue));
    }
    if (b1 != b2) {
      b1 = (int) (255 * (hue));
    }
    this.hueColor = (255 << 24) | (r1 << 16) | (g1 << 8) | b1;
    this.markerPosY = (float) mY-y;
  }
}
