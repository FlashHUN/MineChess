package flash.minechess.events;

import flash.minechess.client.gui.screen.BoardScreen;
import flash.minechess.client.gui.screen.ColorPickerScreen;
import flash.minechess.main.Main;
import flash.minechess.util.Match;
import flash.minechess.util.MatchUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;

@OnlyIn(Dist.CLIENT)
public class KeyHandler {

  private final Minecraft minecraft;

  private static final HashMap<String, KeyBinding> keyBindings = new HashMap<>();
  private static final String keyCategory = "key.minechess.category";

  public KeyHandler() {
    this.minecraft = Minecraft.getInstance();
    init();
  }

  private void init() {
    keyBindings.put("BoardMenu", new KeyBinding(Main.getKeyName("boardmenu"), GLFW.GLFW_KEY_B, keyCategory));
    keyBindings.put("ColorPicker", new KeyBinding(Main.getKeyName("colorpicker"), GLFW.GLFW_KEY_C, keyCategory));

    KeyBinding[] _keyBindings = keyBindings.values().toArray(new KeyBinding[0]);
    for (KeyBinding bind : _keyBindings) {
      ClientRegistry.registerKeyBinding(bind);
    }
  }

  @SubscribeEvent
  public void onKeyInput(InputEvent.KeyInputEvent event) {
    if (minecraft.isGameFocused()) {
      if (event.getAction() == 1) {

        if (keyBindings.get("BoardMenu").isPressed()) {
          if (MatchUtil.matchList.size() > 0) {
            Match match = MatchUtil.matchList.get(0);
            minecraft.displayGuiScreen(new BoardScreen(match));
          } else {
            minecraft.player.sendStatusMessage(new TranslationTextComponent(Main.getMessageName("matches.none")).mergeStyle(TextFormatting.RED), true);
          }
        }

        if (keyBindings.get("ColorPicker").isPressed()) {
          minecraft.displayGuiScreen(new ColorPickerScreen());
        }

      }
    }
  }

}
