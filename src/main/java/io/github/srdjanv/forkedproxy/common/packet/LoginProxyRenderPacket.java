package io.github.srdjanv.forkedproxy.common.packet;

import io.github.srdjanv.forkedproxy.client.data.AccessProxyClientData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import io.github.srdjanv.forkedproxy.ForkedProxy;
import io.github.srdjanv.forkedproxy.client.data.ProxyPosData;

import java.util.HashMap;

public class LoginProxyRenderPacket extends PacketCodec {

    @CodecField
    private NBTTagCompound proxyData;

    public LoginProxyRenderPacket() {
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    public LoginProxyRenderPacket(NBTTagCompound proxyData) {
        this.proxyData = proxyData;
    }

    @Override
    public void actionClient(World world, EntityPlayer player) {
        for (String dimKey : proxyData.getKeySet()) {
            HashMap<BlockPos, ProxyPosData> proxyPosDataList = new HashMap<>();
            NBTTagCompound dimNbt = (NBTTagCompound) proxyData.getTag(dimKey);

            for (String proxyKey : dimNbt.getKeySet()) {
                NBTTagCompound nbt = (NBTTagCompound) dimNbt.getTag(proxyKey);

                int[] proxyPos = nbt.getIntArray("proxyPos");
                int[] proxyTarget;

                if (nbt.hasKey("proxyTarget")) {
                    proxyTarget = nbt.getIntArray("proxyTarget");
                } else {
                    proxyTarget = proxyPos;
                }

                proxyPosDataList.put(new BlockPos(proxyPos[0], proxyPos[1], proxyPos[2]),
                        new ProxyPosData(new BlockPos(proxyTarget[0], proxyTarget[1], proxyTarget[2]),
                                ValueHelpers.deserialize((NBTTagCompound) nbt.getTag("displayValue")),
                                nbt.getIntArray("displayRotations"),
                                nbt.getBoolean("disableRender")));
            }
            try {
                AccessProxyClientData.putAll(Integer.parseInt(dimKey), proxyPosDataList);
            } catch (NumberFormatException e) {
                ForkedProxy.LOGGER.error("Invalid string for dim id:'{}'", dimKey);
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void actionServer(World world, EntityPlayerMP player) {
    }
}
