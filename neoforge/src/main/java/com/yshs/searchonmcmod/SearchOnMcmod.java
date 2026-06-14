package com.yshs.searchonmcmod;

import com.mojang.blaze3d.platform.InputConstants;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.CompletableFuture;

import static com.yshs.searchonmcmod.KeyBindings.COPY_ITEM_NAME_KEY;
import static com.yshs.searchonmcmod.KeyBindings.SEARCH_ON_MCMOD_KEY;

/**
 * 主类
 */
@Mod(SearchOnMcmod.MOD_ID)
@Slf4j
public class SearchOnMcmod {
    /**
     * MOD ID
     */
    public static final String MOD_ID = "searchonmcmod";

    private KeyPressState searchKeyState = KeyPressState.RELEASED;
    private KeyPressState copyNameKeyState = KeyPressState.RELEASED;

    /**
     * 构造函数
     */
    public SearchOnMcmod(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.CLIENT, SearchOnMcmodConfig.CLIENT_SPEC);
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, (container, parent) -> new SearchOnMcmodConfigScreen(parent));
        NeoForge.EVENT_BUS.register(this);
    }

    /**
     * 在物品 tooltip 渲染事件时触发
     *
     * @param event 物品 tooltip 事件，渲染物品信息时触发
     */
    @SubscribeEvent
    public void onRenderTooltipEvent(ItemTooltipEvent event) {
        boolean shouldCopyName = copyNameKeyState == KeyPressState.PRESSED_UNCONSUMED;
        boolean shouldSearch = searchKeyState == KeyPressState.PRESSED_UNCONSUMED;
        copyNameKeyState = consumePressOnce(copyNameKeyState);
        searchKeyState = consumePressOnce(searchKeyState);
        if (!shouldCopyName && !shouldSearch) {
            return;
        }

        String localizedName = event.getItemStack().getHoverName().getString();

        if (shouldCopyName) {
            copyHoveredItemName(localizedName);
        }

        if (!shouldSearch) {
            return;
        }

        log.info("触发了 MC 百科搜索");
        showSearchingHint();

        if (event.getItemStack().isEmpty()) {
            return;
        }

        // 得到物品的注册名
        Item item = event.getItemStack().getItem();
        Identifier id = BuiltInRegistries.ITEM.getKey(item);
        if (id == null) {
            handleSearchFailure("MC 百科搜索: 无法获取物品注册名", new IllegalStateException("物品未注册: " + item));
            return;
        }
        String registryName = id.toString();
        CompletableFuture.runAsync(() -> {
            String itemMCMODID;
            try {
                itemMCMODID = MainUtil.fetchItemMCMODID(registryName);
            } catch (Exception e) {
                handleSearchFailure("MC 百科搜索: 无法通过百科 API 获取物品 MCMOD ID", e);
                return;
            }

            try {
                if ("0".equals(itemMCMODID)) {
                    MainUtil.openSearchPage(localizedName);
                    return;
                }
                MainUtil.openItemPage(itemMCMODID);
            } catch (Exception e) {
                handleSearchFailure("MC 百科搜索: 打开 MC 百科页面失败", e);
            }
        });
    }

    /**
     * 处理按键按下事件，并更新按键状态标记。
     *
     * @param event NeoForge 屏幕事件总线中的按键按下事件
     */
    @SubscribeEvent
    public void onKeyPressed(ScreenEvent.KeyPressed.Post event) {
        InputConstants.Key inputKey = InputConstants.Type.KEYSYM.getOrCreate(event.getKeyCode());
        searchKeyState = markPressed(inputKey, SEARCH_ON_MCMOD_KEY, searchKeyState);
        copyNameKeyState = markPressed(inputKey, COPY_ITEM_NAME_KEY, copyNameKeyState);
    }

    /**
     * 处理按键释放事件，并重置按键状态标记。
     *
     * @param event NeoForge 屏幕事件总线中的按键释放事件
     */
    @SubscribeEvent
    public void onKeyReleased(ScreenEvent.KeyReleased.Post event) {
        InputConstants.Key inputKey = InputConstants.Type.KEYSYM.getOrCreate(event.getKeyCode());
        searchKeyState = markReleased(inputKey, SEARCH_ON_MCMOD_KEY, searchKeyState);
        copyNameKeyState = markReleased(inputKey, COPY_ITEM_NAME_KEY, copyNameKeyState);
    }

    /**
     * 客户端 MOD 事件
     */
    @EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
    public static class ClientModEvents {

        /**
         * 注册按键绑定事件
         *
         * @param event 注册按键绑定事件
         */
        @SubscribeEvent
        public static void registerBindings(RegisterKeyMappingsEvent event) {
            event.register(SEARCH_ON_MCMOD_KEY);
            event.register(COPY_ITEM_NAME_KEY);
        }

    }

    private static KeyPressState markPressed(InputConstants.Key inputKey, KeyMapping keyMapping, KeyPressState state) {
        if (!keyMapping.isActiveAndMatches(inputKey)) {
            return state;
        }
        if (state == KeyPressState.RELEASED) {
            return KeyPressState.PRESSED_UNCONSUMED;
        }
        return state;
    }

    private static KeyPressState markReleased(InputConstants.Key inputKey, KeyMapping keyMapping, KeyPressState state) {
        if (!keyMapping.getKey().equals(inputKey)) {
            return state;
        }
        return KeyPressState.RELEASED;
    }

    private static KeyPressState consumePressOnce(KeyPressState state) {
        if (state == KeyPressState.PRESSED_UNCONSUMED) {
            return KeyPressState.PRESSED_CONSUMED;
        }
        return state;
    }

    private static void showSearchingHint() {
        if (!SearchOnMcmodConfig.isSearchingHintEnabled()) {
            return;
        }
        showToast(SystemToast.SystemToastId.PERIODIC_NOTIFICATION, Component.translatable("text.searchonmcmod.searching"));
    }

    private static void copyHoveredItemName(String localizedName) {
        if (StringUtils.isBlank(localizedName)) {
            log.warn("复制鼠标指针下方物品名称失败：名称为空");
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.keyboardHandler.setClipboard(localizedName);
        log.info("已复制鼠标指针下方物品名称到剪贴板: {}", localizedName);
        showCopyToClipboardHint();
    }

    private static void showCopyToClipboardHint() {
        if (!SearchOnMcmodConfig.isCopyToClipboardHintEnabled()) {
            return;
        }
        showToast(SystemToast.SystemToastId.PERIODIC_NOTIFICATION, Component.translatable("text.searchonmcmod.copy_to_clipboard"));
    }

    private static void showToast(SystemToast.SystemToastId id, Component message) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> SystemToast.addOrUpdate(minecraft.getToastManager(), id, message, null));
    }

    /**
     * 处理 MC 百科搜索失败
     *
     * @param message 日志消息
     * @param e       异常
     */
    private static void handleSearchFailure(String message, Exception e) {
        log.error(message, e);
        showToast(SystemToast.SystemToastId.WORLD_ACCESS_FAILURE, Component.translatable("text.searchonmcmod.error_see_log"));
    }

    private enum KeyPressState {
        // 按键松开
        RELEASED,
        // 按键按下未消费
        PRESSED_UNCONSUMED,
        // 按键按下已消费
        PRESSED_CONSUMED
    }
}
