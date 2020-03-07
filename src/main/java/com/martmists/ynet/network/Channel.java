package com.martmists.ynet.network;

import com.martmists.ynet.api.BaseProvider;

import java.util.Set;

public class Channel {
    public Class<? extends BaseProvider> providerType;
    public Set<ConnectorConfiguration> connectorSettings;
}
