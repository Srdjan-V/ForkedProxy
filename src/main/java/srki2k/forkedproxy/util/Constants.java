package srki2k.forkedproxy.util;

import net.minecraftforge.fml.common.Loader;

public class Constants {
    static boolean integratedTunnelsLoaded;

    public static void init(){
        integratedTunnelsLoaded = Loader.isModLoaded("integratedtunnels");
    }

    public static boolean isIntegratedTunnelsLoaded() {
        return integratedTunnelsLoaded;
    }
}
