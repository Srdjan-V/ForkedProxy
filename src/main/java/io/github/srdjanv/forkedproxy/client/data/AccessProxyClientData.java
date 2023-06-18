package io.github.srdjanv.forkedproxy.client.data;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;

import java.util.Collection;
import java.util.HashMap;

@SideOnly(Side.CLIENT)
public class AccessProxyClientData {

    private AccessProxyClientData() {
    }

    private static final HashMap<Integer, HashMap<BlockPos, ProxyPosData>> proxyPosDataHashMap = new HashMap<>();


    public static void putAll(int proxyDim, HashMap<BlockPos, ProxyPosData> proxyPosDataList) {
        proxyPosDataHashMap.put(proxyDim, proxyPosDataList);
    }

    public static void putTarget(int dim, BlockPos proxy, BlockPos target) {
        ProxyPosData proxyPosData = getProxyDimMap(dim, proxy);
        proxyPosData.setTarget(target);
    }

    public static void putVariable(int dim, BlockPos proxy, IValue variable) {
        ProxyPosData proxyPosData = getProxyDimMap(dim, proxy);
        proxyPosData.setVariable(variable);
    }

    public static void putRotation(int dim, BlockPos proxy, int[] rotation) {
        ProxyPosData proxyPosData = getProxyDimMap(dim, proxy);
        proxyPosData.setRotation(rotation);
    }

    public static void putDisable(int dim, BlockPos proxy, boolean disable) {
        ProxyPosData proxyPosData = getProxyDimMap(dim, proxy);
        proxyPosData.setDisable(disable);
    }

    private static ProxyPosData getProxyDimMap(int dim, BlockPos proxy) {
        HashMap<BlockPos, ProxyPosData> proxyDimMap = proxyPosDataHashMap.computeIfAbsent(dim, k -> new HashMap<>());
        ProxyPosData proxyPosData = proxyDimMap.get(proxy);
        if (proxyPosData == null) {
            proxyPosData = new ProxyPosData();
            proxyDimMap.put(proxy, proxyPosData);
        }
        return proxyPosData;
    }


    public static void remove(int proxyDim, BlockPos proxyPos) {
        HashMap<BlockPos, ProxyPosData> proxyDimMap = proxyPosDataHashMap.get(proxyDim);
        if (proxyDimMap != null) {
            proxyDimMap.remove(proxyPos);
            if (proxyDimMap.isEmpty()) {
                proxyPosDataHashMap.remove(proxyDim);
            }
        }
    }

    public static boolean dimContainsProxy(int dim) {
        return proxyPosDataHashMap.get(dim) != null;
    }

    public static Collection<ProxyPosData> getProxysInDim(int dim) {
        return proxyPosDataHashMap.get(dim).values();
    }

    public static ProxyPosData getProxyData(int dim, BlockPos blockPos) {
        HashMap<BlockPos, ProxyPosData> dimProxyMap = proxyPosDataHashMap.get(dim);
        if (dimProxyMap == null) {
            return null;
        }

        return dimProxyMap.get(blockPos);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.player.world.isRemote && event.player.equals(Minecraft.getMinecraft().player)) {
            proxyPosDataHashMap.clear();
        }
    }
}