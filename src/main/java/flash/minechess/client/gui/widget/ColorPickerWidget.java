package flash.minechess.client.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import flash.minechess.util.ScreenHelper;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ColorPickerWidget extends Button {

  private int hueColor;
  private int selectedColor;
  private float markerPosX;
  private float markerPosY;

  public ColorPickerWidget(int x, int y, int width, int height) {
    super(x, y, width, height, StringTextComponent.EMPTY, press -> {});
    this.hueColor = 0xFFFF0000;
    this.selectedColor = this.hueColor;
    this.markerPosX = width;
    this.markerPosY = 0;
  }

  public int getHueColor() {
    return hueColor;
  }

  public void setHueColor(int hueColor) {
    this.hueColor = hueColor;
  }

  public int getSelectedColor() {
    return this.selectedColor;
  }

  public void setSelectedColor(int selectedColor) {
    this.selectedColor = selectedColor;
  }

  @Override
  public void renderWidget(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
    fill(matrixStack, x-1, y-1, x+width+1, y+height+1, 0xFF000000);
    ScreenHelper.fillGradient(matrixStack, x, y, x+width, y+height, this.hueColor);
    renderMarker(matrixStack);
  }

  private void renderMarker(MatrixStack matrixStack) {
    int markerLineLength = 2;
    float hX = x+markerPosX-markerLineLength/2;
    float hY = y+markerPosY-markerLineLength/2;
    matrixStack.push();
    {
      matrixStack.translate(hX-markerLineLength/4.0, hY-markerLineLength/4.0, 0);
      hLine(matrixStack, 0, markerLineLength, -1, 0xFF000000);
      hLine(matrixStack, 0, markerLineLength, markerLineLength + 1, 0xFF000000);
      vLine(matrixStack, -1, -1, markerLineLength+1, 0xFF000000);
      vLine(matrixStack, markerLineLength+1, -1, markerLineLength+1, 0xFF000000);
    }
    matrixStack.pop();
  }

  @Override
  public void onClick(double mouseX, double mouseY) {
    getColor(mouseX, mouseY);
  }

  @Override
  protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
    getColor(mouseX, mouseY);
  }

  public void getColorAtMarker() {
    getColor(x+markerPosX, y+markerPosY);
  }

  private void getColor(double mouseX, double mouseY) {
    double mX = mouseX;
    double mY = mouseY;
    if (mX < x) {
      mX = x;
    } else if (mX > x+width) {
      mX = x+width;
    }
    if (mY < y) {
      mY = y;
    } else if (mY > y+height) {
      mY = y+height;
    }
    float colorX = (float) ((mX-x)/width);
    float colorY = (float) ((mY-y)/height);

    int whiteness = (int) (255*(1-colorX));
    int blackness = (int) (255*colorY);

    int newColor = ScreenHelper.colorLightness(this.hueColor, whiteness);
    newColor = ScreenHelper.colorLightness(newColor, -blackness);

    this.selectedColor = newColor;
    this.markerPosX = (width*colorX);
    this.markerPosY = (height*colorY);
  }
}
