package flash.minechess.main;

import flash.minechess.events.EventHandler;
import flash.minechess.init.CommandInit;
import flash.minechess.init.SoundInit;
import flash.minechess.network.PacketDispatcher;
import flash.minechess.proxy.ClientProxy;
import flash.minechess.proxy.CommonProxy;
import flash.minechess.util.MatchUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Main.MODID)
public class Main {

    public static final String MODID = "minechess";

    public static final Logger LOGGER = LogManager.getLogger();

    public static final CommonProxy proxy = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);

    public Main() {
        final IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        SoundInit.SOUNDS.register(modBus);
        modBus.addListener(this::setup);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new EventHandler());
    }

    private void setup(final FMLCommonSetupEvent event) {
        PacketDispatcher.registerMessages();

        MatchUtil.loadMatches();
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        CommandInit.registerCommands(event.getDispatcher(), event.getEnvironment());
    }

    public static String getMessageName(String name) {
        StringBuilder builder = new StringBuilder("msg.");
        return builder.append(Main.MODID).append(".").append(name).toString();
    }

    public static String getKeyName(String name) {
        StringBuilder builder = new StringBuilder("key.");
        return builder.append(Main.MODID).append(".").append(name).toString();
    }

    public static String getScreenName(String name) {
        StringBuilder builder = new StringBuilder("screen.");
        return builder.append(Main.MODID).append(".").append(name).toString();
    }

    public static ResourceLocation location(String name) {
        return new ResourceLocation(Main.MODID, name);
    }
}
