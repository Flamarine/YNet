package com.martmists.ynet.screens;

import com.martmists.ynet.YNetMod;
import com.martmists.ynet.api.BaseProvider;
import com.martmists.ynet.blockentities.ControllerBlockEntity;
import com.martmists.ynet.containers.ControllerContainer;
import com.martmists.ynet.network.Channel;
import com.martmists.ynet.network.ConnectorConfiguration;
import com.martmists.ynet.network.Network;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import spinnery.common.BaseContainerScreen;
import spinnery.widget.*;
import spinnery.widget.api.Position;
import spinnery.widget.api.Size;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ControllerScreen extends BaseContainerScreen<ControllerContainer> {
    private static List<Class<? extends BaseProvider>> TYPE_LOOP = new ArrayList<>(YNetMod.PROVIDER_NAMES.keySet());

    static {
        TYPE_LOOP.add(null);
    }

    private final ControllerBlockEntity sourceBlockEntity;
    private int channelNum;
    private Channel currentChannel;
    private ConnectorConfiguration currentConfig;
    private WButton[] channelButtons = new WButton[9];
    private WButton configButtonClicked;

    public ControllerScreen(ControllerContainer linkedContainer) {
        super(new LiteralText(""), linkedContainer, linkedContainer.getPlayerInventory().player);
        PlayerEntity player = linkedContainer.getPlayerInventory().player;
        this.sourceBlockEntity = (ControllerBlockEntity) player.world.getBlockEntity(linkedContainer.packet.readBlockPos());
        assert sourceBlockEntity != null;

        WInterface mainInterface = getInterface();
        WPanel mainPanel = mainInterface.createChild(
                WPanel.class,
                Position.of(0, 0, 0),
                Size.of(320, 155)
        );
        mainPanel.center();

        WPanel channelSettingsPanel = mainPanel.createChild(
                WPanel.class,
                Position.of(mainPanel, 169, 5, 100),
                Size.of(145, 73)
        );

        channelNum = 0;
        currentChannel = sourceBlockEntity.channels[0];
        channelSettingsPanel.setLabel("Channel 1");

        channelSettingsPanel.createChild(
                WStaticText.class,
                Position.of(channelSettingsPanel, 4, 22),
                Size.of(2, 20)
        ).setText("Mode:");
        WButton channelTypeButton = channelSettingsPanel.createChild(
                WButton.class,
                Position.of(channelSettingsPanel, 40, 20),
                Size.of(100, 14)
        );
        channelTypeButton.setLabel("Disabled");
        channelTypeButton.setOnMouseClicked((WButton w, int mouseX, int mouseY, int mouseButton) -> {
            if (channelSettingsPanel.getZ() != 100) {
                return;
            }
            int currentIndex = TYPE_LOOP.indexOf((currentChannel != null) ? currentChannel.providerType : null);
            Class<? extends BaseProvider> nextType;
            try {
                nextType = TYPE_LOOP.get(currentIndex + ((mouseButton == 0) ? 1 : -1));
            } catch (IndexOutOfBoundsException e) {
                nextType = TYPE_LOOP.get((mouseButton == 0) ? 0 : TYPE_LOOP.size() - 1);
            }
            if (TYPE_LOOP.get(currentIndex) == null) {
                currentChannel = new Channel();
            }
            currentChannel.providerType = nextType;
            currentChannel.connectorSettings = new HashSet<>();
            sourceBlockEntity.channels[channelNum] = currentChannel;
            if (nextType == null) {
                currentChannel = null;
                channelButtons[channelNum].overrideStyle("background.on", 0xff8b8b8b);
                channelButtons[channelNum].overrideStyle("background.off", 0xff8b8b8b);
            } else {
                channelButtons[channelNum].overrideStyle("background.on", YNetMod.COLOR_MAP.get(nextType));
                channelButtons[channelNum].overrideStyle("background.off", YNetMod.COLOR_MAP.get(nextType));
            }
            channelTypeButton.setLabel(YNetMod.PROVIDER_NAMES.getOrDefault(nextType, "Disabled").replace(":", "."));
            sourceBlockEntity.markDirty();
        });

        WPanel connectorSettingsPanel = mainPanel.createChild(
                WPanel.class,
                Position.of(mainPanel, 169, 5, 0),
                Size.of(145, 73)
        );
        connectorSettingsPanel.setLabel("Connector settings");
        connectorSettingsPanel.createChild(
                WStaticText.class,
                Position.of(connectorSettingsPanel, 4, 22),
                Size.of(2, 20)
        ).setText("Mode:");
        WButton stateButton = connectorSettingsPanel.createChild(
                WButton.class,
                Position.of(connectorSettingsPanel, 40, 20),
                Size.of(100, 14)
        );
        stateButton.setOnMouseClicked((WButton w, int mouseX, int mouseY, int mouseButton) -> {
            if (currentChannel == null ||
                    currentChannel.providerType == null ||
                    connectorSettingsPanel.getZ() != 100 ||
                    currentConfig == null ||
                    !Network.tMap.get(player.world.getBlockState(currentConfig.providerPos).getBlock().getClass()).contains(currentChannel.providerType)) {
                return;
            }
            ConnectorConfiguration.State nextState;
            switch (currentConfig.state) {
                case DISABLED:
                    nextState = ConnectorConfiguration.State.INPUT;
                    break;
                case INPUT:
                    nextState = ConnectorConfiguration.State.OUTPUT;
                    break;
                case OUTPUT:
                    nextState = ConnectorConfiguration.State.DISABLED;
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + currentConfig.state);
            }
            currentConfig.state = nextState;
            stateButton.setLabel(nextState.name());
            if (currentConfig.state != ConnectorConfiguration.State.DISABLED) {
                configButtonClicked.setLabel((currentConfig.state == ConnectorConfiguration.State.INPUT) ? "I" : "O");
                int inputColor = 0xff0077be;
                int outputColor = 0xffffcc99;
                configButtonClicked.overrideStyle("background.on", (currentConfig.state == ConnectorConfiguration.State.INPUT) ? inputColor : outputColor);
                configButtonClicked.overrideStyle("background.off", (currentConfig.state == ConnectorConfiguration.State.INPUT) ? inputColor : outputColor);
            } else {
                configButtonClicked.overrideStyle("background.on", 0xff8b8b8b);
                configButtonClicked.overrideStyle("background.off", 0xff8b8b8b);
            }
            sourceBlockEntity.markDirty();
        });

        for (int i = 0; i < 9; i++) {
            int f = i;
            WButton button = mainPanel.createChild(
                    WButton.class,
                    Position.of(mainPanel, 25 + 14 * i, 5),
                    Size.of(12, 14)
            );
            channelButtons[f] = button;
            button.setLabel(Integer.toString(i + 1));
            button.setOnMouseClicked((WButton widget, int mouseX, int mouseY, int mouseButton) -> {
                currentChannel = sourceBlockEntity.channels[f];
                connectorSettingsPanel.setZ(0);
                channelSettingsPanel.setZ(100);

                channelNum = f;
                channelSettingsPanel.setLabel("Channel " + (f + 1));

                if (currentChannel != null) {
                    channelTypeButton.setLabel(new TranslatableText(YNetMod.PROVIDER_NAMES.getOrDefault(currentChannel.providerType, "Disabled").replace(":", ".")));
                }
            });

            if (sourceBlockEntity.channels[i] != null && sourceBlockEntity.channels[i].providerType != null) {
                button.overrideStyle("background.on", YNetMod.COLOR_MAP.get(sourceBlockEntity.channels[i].providerType));
                button.overrideStyle("background.off", YNetMod.COLOR_MAP.get(sourceBlockEntity.channels[i].providerType));
            }
        }

        WVerticalScrollableContainer blockList = mainPanel.createChild(
                WVerticalScrollableContainer.class,
                Position.of(mainPanel, 4, 20),
                Size.of(163, 130)
        );

        Set<BlockPos> blocksToShow = sourceBlockEntity.network.getProviders(player.world);
        int i = 0;
        for (BlockPos p : blocksToShow) {
            WPanel row = blockList.createChild(
                    WPanel.class,
                    Position.of(blockList, 0, 26 * i),
                    Size.of(154, 25)
            );
            i++;
            WItem item = row.createChild(
                    WItem.class,
                    Position.of(row, 4, 4),
                    Size.of(16, 16)
            );
            item.setItemStack(new ItemStack(player.world.getBlockState(p).getBlock().asItem()));

            for (int j = 0; j < 9; j++) {
                int k = j;
                WButton button = row.createChild(
                        WButton.class,
                        Position.of(row, 21 + 14 * j, 5),
                        Size.of(12, 14)
                );
                // Get configuration for block in channel
                Channel channel = sourceBlockEntity.channels[j];

                if (channel != null) {
                    ConnectorConfiguration config = channel.connectorSettings.stream().filter((s) -> s.providerPos.equals(p)).findFirst().orElse(null);
                    if (config == null) {
                        config = new ConnectorConfiguration();
                        config.providerPos = p;
                        channel.connectorSettings.add(config);
                    }

                    if (config.state != ConnectorConfiguration.State.DISABLED) {
                        button.setLabel((config.state == ConnectorConfiguration.State.INPUT) ? "I" : "O");
                        int inputColor = 0xff0077be;
                        int outputColor = 0xffffcc99;
                        button.overrideStyle("background.on", (config.state == ConnectorConfiguration.State.INPUT) ? inputColor : outputColor);
                        button.overrideStyle("background.off", (config.state == ConnectorConfiguration.State.INPUT) ? inputColor : outputColor);
                    } else {
                        button.overrideStyle("background.on", 0xff8b8b8b);
                        button.overrideStyle("background.off", 0xff8b8b8b);
                    }
                }

                button.setOnMouseClicked((WButton widget, int mouseX, int mouseY, int mouseButton) -> {
                    if (sourceBlockEntity.channels[k] != null) {
                        currentConfig = sourceBlockEntity.channels[k].connectorSettings.stream().filter((s) -> s.providerPos.equals(p)).findFirst().orElse(null);
                        if (currentConfig == null) {
                            currentConfig = new ConnectorConfiguration();
                            currentConfig.providerPos = p;
                            sourceBlockEntity.channels[k].connectorSettings.add(currentConfig);
                        }
                        stateButton.setLabel(currentConfig.state.name());
                        connectorSettingsPanel.setZ(100);
                        channelSettingsPanel.setZ(0);
                        configButtonClicked = button;
                    }
                });

            }
        }

        WSlot.addPlayerInventory(Position.of(mainPanel, 170, 83, 1), Size.of(16, 16), mainInterface);
    }
}
