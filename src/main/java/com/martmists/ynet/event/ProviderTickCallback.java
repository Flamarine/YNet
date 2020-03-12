package com.martmists.ynet.event;

import com.martmists.ynet.api.BaseProvider;
import com.martmists.ynet.blockentities.ControllerBlockEntity;
import com.martmists.ynet.network.ConnectorConfiguration;

import java.util.Set;

public interface ProviderTickCallback<T extends BaseProvider> {
    void interact(Set<ConnectorConfiguration> listeners, ControllerBlockEntity be);
}
