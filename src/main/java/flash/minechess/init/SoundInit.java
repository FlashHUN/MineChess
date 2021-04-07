package flash.minechess.init;

import flash.minechess.main.Main;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import static flash.minechess.main.Main.location;

public class SoundInit {

  public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Main.MODID);

  public static final RegistryObject<SoundEvent> MOVE = SOUNDS.register("move",
      () -> new SoundEvent(location("move")));
  public static final RegistryObject<SoundEvent> CASTLE = SOUNDS.register("castle",
      () -> new SoundEvent(location("castle")));
  public static final RegistryObject<SoundEvent> CAPTURE = SOUNDS.register("capture",
      () -> new SoundEvent(location("capture")));
  public static final RegistryObject<SoundEvent> CHECK = SOUNDS.register("check",
      () -> new SoundEvent(location("check")));

}
