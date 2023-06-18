package io.github.srdjanv.forkedproxy;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cyclops.cyclopscore.config.ConfigHandler;
import org.cyclops.cyclopscore.config.extendedconfig.BlockItemConfigReference;
import org.cyclops.cyclopscore.init.ItemCreativeTab;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.init.RecipeHandler;
import org.cyclops.cyclopscore.proxy.ICommonProxy;
import io.github.srdjanv.forkedproxy.common.block.BlockAccessProxyConfig;
import io.github.srdjanv.forkedproxy.common.compat.integratedtunnels.IntegratedTunnelsCompat;
import io.github.srdjanv.forkedproxy.common.datamanagement.WorldProxyManager;
import io.github.srdjanv.forkedproxy.util.Constants;

@Mod(
        modid = ForkedProxy.MODID,
        name = ForkedProxy.NAME,
        useMetadata = true,
        dependencies = "required-after:forge;required-after:cyclopscore;required-after:integrateddynamics;required-after:mixinbooter",
        guiFactory = "io.github.srdjanv.forkedproxy.GuiConfigOverview$ExtendedConfigGuiFactory"
)
public class ForkedProxy extends ModBase {

    public static final String MODID = "integrated_proxy";
    public static final String NAME = "Forked Proxy";

    public static final Logger LOGGER = LogManager.getLogger(MODID);

    @SidedProxy(clientSide = "io.github.srdjanv.forkedproxy.proxy.ClientProxy", serverSide = "io.github.srdjanv.forkedproxy.proxy.CommonProxy")
    public static ICommonProxy proxy;

    @Mod.Instance(value = MODID)
    public static ForkedProxy INSTANCE;

    public ForkedProxy() {
        super(MODID, NAME);
    }

    @Override
    public ICommonProxy getProxy() {
        return proxy;
    }

    @Override
    @Mod.EventHandler
    public final void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        Constants.init();
    }

    @Override
    @Mod.EventHandler
    public final void init(FMLInitializationEvent event) {
        super.init(event);
        if (Constants.isIntegratedTunnelsLoaded()) {
            IntegratedTunnelsCompat.init();
        }
    }

    @Override
    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
    }

    @Mod.EventHandler
    public void onServerStopping(FMLServerStoppingEvent event) {
        WorldProxyManager.cleanExistingTiles();
    }

    @Override
    protected RecipeHandler constructRecipeHandler() {
        return null;
    }

    @Override
    public CreativeTabs constructDefaultCreativeTab() {
        return new ItemCreativeTab(this, new BlockItemConfigReference(BlockAccessProxyConfig.class));
    }

    @Override
    public void onMainConfigsRegister(ConfigHandler configHandler) {
        configHandler.add(new BlockAccessProxyConfig());
    }

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(MODID)) {
            ConfigManager.sync(MODID, Config.Type.INSTANCE);
        }
    }
}
