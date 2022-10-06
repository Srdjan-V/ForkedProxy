package srki2k.forkedproxy.common.block;

import srki2k.forkedproxy.ForkedProxy;
import srki2k.forkedproxy.common.item.ItemBlockAccessProxy;
import net.minecraft.item.Item;
import org.cyclops.cyclopscore.config.ConfigurableProperty;
import org.cyclops.cyclopscore.config.ConfigurableTypeCategory;
import org.cyclops.cyclopscore.config.extendedconfig.BlockContainerConfig;

public class BlockAccessProxyConfig extends BlockContainerConfig {

    public static BlockAccessProxyConfig INSTANCE;

    @ConfigurableProperty(
            category = ConfigurableTypeCategory.BLOCK,
            comment = "The max range of access proxy (square range, not radius), -1:infinite",
            isCommandable = true,
            minimalValue = -1)
    public static int range = -1;

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
