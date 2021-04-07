package flash.minechess.util;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.vector.Matrix4f;

public class ScreenHelper {

  public static int colorLightness(int color, int amount) {
    int r = (int) clamp(((color & 0xFF0000) >> 16) + amount, 0, 255);
    int g = (int) clamp(((color & 0xFF00) >> 8) + amount, 0, 255);
    int b = (int) clamp(((color & 0xFF)) + amount, 0, 255);
    int newColor = (255 << 24) | (r << 16) | (g << 8) | b;
    return newColor;
  }

  public static float clamp(float value, float minValue, float maxValue) {
    return value < minValue ? minValue : (value > maxValue ? maxValue : value);
  }

  public static void fillGradient(MatrixStack matrixStack, int x1, int y1, int x2, int y2, int color) {
    RenderSystem.disableTexture();
    RenderSystem.enableBlend();
    RenderSystem.disableAlphaTest();
    RenderSystem.defaultBlendFunc();
    RenderSystem.shadeModel(7425);
    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder bufferbuilder = tessellator.getBuffer();
    bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
    fillGradient(matrixStack.getLast().getMatrix(), bufferbuilder, x1, y1, x2, y2, 0, color);
    tessellator.draw();
    RenderSystem.shadeModel(7424);
    RenderSystem.disableBlend();
    RenderSystem.enableAlphaTest();
    RenderSystem.enableTexture();
  }

  public static void fillGradient(Matrix4f matrix, BufferBuilder builder, int x1, int y1, int x2, int y2, int z, int color) {
    float r = (float)(color >> 16 & 255) / 255.0F;
    float g = (float)(color >> 8 & 255) / 255.0F;
    float b = (float)(color & 255) / 255.0F;
    // Top Right
    builder.pos(matrix, (float)x2, (float)y1, (float)z).color(r, g, b, 1f).endVertex();
    // Top Left
    builder.pos(matrix, (float)x1, (float)y1, (float)z).color(1f, 1f, 1f, 1f).endVertex();
    // Bottom Left
    builder.pos(matrix, (float)x1, (float)y2, (float)z).color(0f, 0f, 0f, 1f).endVertex();
    // Bottom Right
    builder.pos(matrix, (float)x2, (float)y2, (float)z).color(0f, 0f, 0f, 1f).endVertex();
  }

}
