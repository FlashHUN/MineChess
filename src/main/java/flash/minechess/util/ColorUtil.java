package flash.minechess.util;

import flash.minechess.client.gui.screen.BoardScreen;
import flash.minechess.client.gui.screen.ColorPickerScreen;
import flash.minechess.main.Main;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.io.*;

@OnlyIn(Dist.CLIENT)
public class ColorUtil {

  private static FileWriter fw;

  public static void save(int whiteColor, int blackColor, int highlightColor) {
    try {
      String path = new File(".").getCanonicalPath();
      File directory = new File(path + "/" + Main.MODID);
      if (!directory.exists()) {
        Main.LOGGER.debug("Created MineChess directory");
        directory.mkdirs();
      }
      File colorFile = new File(path + "/" + Main.MODID + "/colors");
      fw = new FileWriter(colorFile);
      fw.write(whiteColor+"\n"+blackColor+"\n"+highlightColor);
      fw.flush();
      fw.close();
    } catch (IOException e){
      Main.LOGGER.debug("Couldn't export colors: " + e.getMessage());
    }
  }

  public static void read() {
    try {
      String path = new File(".").getCanonicalPath();
      File directory = new File(path + "/" + Main.MODID);
      if (!directory.exists()) {
        Main.LOGGER.debug("Created MineChess directory");
        directory.mkdirs();
      }
      File colorFile = new File(path + "/" + Main.MODID + "/colors");
      if (colorFile.exists()) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(colorFile)));
        for (int i = 0; i < 3; i++) {
          String s = reader.readLine();
          int color = Integer.parseInt(s);
          switch (i) {
            case 0:
              BoardScreen.whiteColor = color;
              ColorPickerScreen.whiteColor = color;
              break;
            case 1:
              BoardScreen.blackColor = color;
              ColorPickerScreen.blackColor = color;
              break;
            case 2:
              BoardScreen.highlightedColor = color;
              ColorPickerScreen.highlightedColor = color;
              break;
          }
        }
      }
    } catch (Exception e) {
      Main.LOGGER.debug("Couldn't load colors: " + e.getMessage());
    }
  }
}
