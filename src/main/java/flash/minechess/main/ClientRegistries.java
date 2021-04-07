package flash.minechess.main;

import flash.minechess.events.KeyHandler;
import flash.minechess.util.ColorUtil;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Main.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientRegistries {

  @SubscribeEvent
  public static void clientSetup(FMLClientSetupEvent event) {

    MinecraftForge.EVENT_BUS.register(new KeyHandler());

    ColorUtil.read();
  }

}
