package io.github.srdjanv.forkedproxy.common.block;

import io.github.srdjanv.forkedproxy.ForkedProxy;
import io.github.srdjanv.forkedproxy.common.item.ItemBlockAccessProxy;
import net.minecraft.item.Item;
import org.cyclops.cyclopscore.config.ConfigurableProperty;
import org.cyclops.cyclopscore.config.ConfigurableTypeCategory;
import org.cyclops.cyclopscore.config.extendedconfig.BlockContainerConfig;

public class BlockAccessProxyConfig extends BlockContainerConfig {

    public static BlockAccessProxyConfig _instance;

    @ConfigurableProperty(
            category = ConfigurableTypeCategory.GENERAL,
            comment = "The max range of access proxy (square range, not radius), -1:infinite",
            isCommandable = true,
            minimalValue = -1,
            maximalValue = Integer.MAX_VALUE - 1)
    public static int range = -1;

    @ConfigurableProperty(
            category = ConfigurableTypeCategory.GENERAL,
            comment = "If the proxy should start ticking at a random interval to even the load, default=true",
            isCommandable = true)
    public static boolean randomStartTick = true;

    public BlockAccessProxyConfig() {
        super(
                ForkedProxy.INSTANCE,
                true,
                "access_proxy",
                null,
                BlockAccessProxy.class
        );
    }

    @Override
    public Class<? extends Item> getItemBlockClass() {
        return ItemBlockAccessProxy.class;
    }

    @Override
    public boolean isDisableable() {
        return false;
    }
}
