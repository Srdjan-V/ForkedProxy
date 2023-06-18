package io.github.srdjanv.forkedproxy;

import net.minecraft.client.gui.GuiScreen;
import org.cyclops.cyclopscore.client.gui.config.ExtendedConfigGuiFactoryBase;
import org.cyclops.cyclopscore.client.gui.config.GuiConfigOverviewBase;
import org.cyclops.cyclopscore.init.ModBase;

public class GuiConfigOverview extends GuiConfigOverviewBase {
    public GuiConfigOverview(GuiScreen parentScreen) {
        super(ForkedProxy.INSTANCE, parentScreen);
    }

    @Override
    public ModBase getMod() {
        return ForkedProxy.INSTANCE;
    }

    public static class ExtendedConfigGuiFactory extends ExtendedConfigGuiFactoryBase {

        @Override
        public Class<? extends GuiConfigOverviewBase> mainConfigGuiClass() {
            return GuiConfigOverview.class;
        }
    }
}
