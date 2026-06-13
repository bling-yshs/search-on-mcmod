package com.yshs.searchonmcmod;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/**
 * MC 百科搜索 NeoForge 配置屏幕。
 */
public class SearchOnMcmodConfigScreen extends Screen {
    private static final int OPTION_WIDTH = 310;
    private static final int DONE_BUTTON_WIDTH = 200;

    private final Screen parent;
    private boolean showSearchingHint;
    private boolean showCopyToClipboardHint;

    /**
     * 创建配置屏幕。
     *
     * @param parent 打开配置屏幕前的上级屏幕
     */
    public SearchOnMcmodConfigScreen(Screen parent) {
        super(Component.translatable("config.searchonmcmod.title"));
        this.parent = parent;
        this.showSearchingHint = SearchOnMcmodConfig.isSearchingHintEnabled();
        this.showCopyToClipboardHint = SearchOnMcmodConfig.isCopyToClipboardHintEnabled();
    }

    /**
     * 初始化配置项控件。
     */
    @Override
    protected void init() {
        int left = (this.width - OPTION_WIDTH) / 2;
        int top = this.height / 4;

        addRenderableWidget(CycleButton.onOffBuilder(this.showSearchingHint).create(
                left,
                top,
                OPTION_WIDTH,
                20,
                Component.translatable("config.searchonmcmod.show_searching_hint"),
                (button, value) -> {
                    this.showSearchingHint = value;
                    SearchOnMcmodConfig.updateClientOptions(this.showSearchingHint, this.showCopyToClipboardHint);
                }
        ));

        addRenderableWidget(CycleButton.onOffBuilder(this.showCopyToClipboardHint).create(
                left,
                top + 24,
                OPTION_WIDTH,
                20,
                Component.translatable("config.searchonmcmod.show_copy_to_clipboard_hint"),
                (button, value) -> {
                    this.showCopyToClipboardHint = value;
                    SearchOnMcmodConfig.updateClientOptions(this.showSearchingHint, this.showCopyToClipboardHint);
                }
        ));

        int actionsY = this.height - 27;
        int doneX = (this.width - DONE_BUTTON_WIDTH) / 2;
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                .bounds(doneX, actionsY, DONE_BUTTON_WIDTH, 20)
                .build());
    }

    /**
     * 关闭配置屏幕并返回上级屏幕。
     */
    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    /**
     * 提取配置屏幕渲染状态。
     *
     * @param guiGraphicsExtractor GUI 渲染状态提取器
     * @param mouseX 鼠标 X 坐标
     * @param mouseY 鼠标 Y 坐标
     * @param partialTick 局部 tick
     */
    @Override
    public void extractRenderState(@NotNull GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTick) {
        guiGraphicsExtractor.centeredText(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
        super.extractRenderState(guiGraphicsExtractor, mouseX, mouseY, partialTick);
    }
}
