package srki2k.forkedproxy.client.data;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;

import java.util.HashMap;

@SideOnly(Side.CLIENT)
public class AccessProxyClientData {

    private AccessProxyClientData() {
    }

    private static final AccessProxyClientData _instance = new AccessProxyClientData();

    public static AccessProxyClientData getInstance() {
        return _instance;
    }

    private final HashMap<DimPos, DimPos> target_map = new HashMap<>();
    private final HashMap<DimPos, IValue> variable_map = new HashMap<>();
    private final HashMap<DimPos, int[]> rotation_map = new HashMap<>();
    private final HashMap<DimPos, Boolean> disable_map = new HashMap<>();


    public void putAll(DimPos proxy, DimPos target, boolean disable, int[] rotation, IValue value) {
        target_map.put(proxy, target);
        disable_map.put(proxy, disable);
        rotation_map.put(proxy, rotation);
        variable_map.put(proxy, value);
    }

    public void putTarget(DimPos proxy, DimPos target) {
        target_map.put(proxy, target);
    }

    public void putVariable(DimPos proxy, IValue value) {
        variable_map.put(proxy, value);
    }

    public void putRotation(DimPos proxy, int[] value) {
        rotation_map.put(proxy, value);
    }

    public void putDisable(DimPos proxy, boolean disable) {
        disable_map.put(proxy, disable);
    }


    public void remove(DimPos proxy) {
        target_map.remove(proxy);
        variable_map.remove(proxy);
        rotation_map.remove(proxy);
        disable_map.remove(proxy);
    }

    public HashMap<DimPos, DimPos> getTargetMap() {
        return target_map;
    }

    public DimPos getTarget(BlockPos pos, int dim) {
        return target_map.get(DimPos.of(dim, pos));
    }

    public IValue getVariable(DimPos dimPos) {
        return variable_map.get(dimPos);
    }

    public int[] getRotation(DimPos dimPos) {
        return rotation_map.get(dimPos);
    }

    public boolean getDisable(DimPos dimPos) {
        return disable_map.getOrDefault(dimPos, false);
    }

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.player.world.isRemote && event.player.equals(Minecraft.getMinecraft().player)) {
            target_map.clear();
            variable_map.clear();
            rotation_map.clear();
            disable_map.clear();
        }
    }
}
