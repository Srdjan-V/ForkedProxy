package srki2k.forkedproxy.common.id_network;

import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.network.IEventListenableNetworkElement;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetwork;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.network.TileNetworkElement;
import srki2k.forkedproxy.ForkedProxy;
import srki2k.forkedproxy.common.tileentity.TileAccessProxy;

import javax.annotation.Nullable;

public class AccessProxyNetworkElement extends TileNetworkElement<TileAccessProxy> implements IEventListenableNetworkElement<TileAccessProxy> {

    public AccessProxyNetworkElement(DimPos pos) {
        super(pos);
    }

    @Override
    public boolean onNetworkAddition(INetwork network) {
        if (super.onNetworkAddition(network)) {
            IPartNetwork partNetwork = NetworkHelpers.getPartNetwork(network);
            if (partNetwork == null) {
                return false;
            }
            if (!partNetwork.addVariableContainer(getPos())) {
                ForkedProxy.LOGGER.warn("The access proxy is already existing: " + getPos());
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public void onNetworkRemoval(INetwork network) {
        super.onNetworkRemoval(network);
        IPartNetwork partNetwork = NetworkHelpers.getPartNetwork(network);
        if (partNetwork != null) {
            partNetwork.removeVariableContainer(getPos());
        }
    }

    @Nullable
    @Override
    public TileAccessProxy getNetworkEventListener() {
        return getTile();
    }

    @Override
    protected Class<TileAccessProxy> getTileClass() {
        return TileAccessProxy.class;
    }

    @Override
    public void setPriorityAndChannel(INetwork network, int priority, int channel) {
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public int getChannel() {
        return IPositionedAddonsNetwork.DEFAULT_CHANNEL;
    }
}
