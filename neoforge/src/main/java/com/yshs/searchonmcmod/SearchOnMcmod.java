package com.yshs.searchonmcmod;

import com.mojang.blaze3d.platform.InputConstants;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
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
    @SuppressWarnings("MissingJavadoc")
    public SearchOnMcmod(ModContainer modContainer) {
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

        // 1. 得到物品的描述 ID
        String descriptionId = event.getItemStack().getItem().getDescriptionId();
        if (StringUtils.isBlank(descriptionId)) {
            return;
        }

        // 2. 转换为注册表名
        String registryName = MainUtil.convertDescriptionIdToRegistryName(descriptionId);

        // 3. 如果注册表名为空气，则不进行搜索
        if ("minecraft:air".equals(registryName)) {
            return;
        }

        // 4. 如果注册表名为空，但物品描述 ID 不为空，则直接搜索
        if (StringUtils.isBlank(registryName) && StringUtils.isNotBlank(descriptionId)) {
            try {
                MainUtil.openSearchPage(descriptionId);
            } catch (Exception e) {
                handleSearchFailure("MC 百科搜索: 打开搜索页面失败", e);
            }
            return;
        }

        CompletableFuture.runAsync(() -> {
            String itemMCMODID;
            try {
                // 5. 查询并得到物品在 MCMOD 中的 ID
                itemMCMODID = MainUtil.fetchItemMCMODID(registryName);
            } catch (Exception e) {
                handleSearchFailure("MC 百科搜索: 无法通过百科 API 获取物品 MCMOD ID", e);
                return;
            }

            try {
                // 6. 如果 mcmodItemID 为 0，则进行搜索
                if ("0".equals(itemMCMODID)) {
                    MainUtil.openSearchPage(localizedName);
                    return;
                }

                // 7. 判断物品页面是否存在，如果不存在则进行搜索
                if (!MainUtil.itemPageExist(itemMCMODID)) {
                    MainUtil.openSearchPage(localizedName);
                    return;
                }

                // 8. 打开 MCMOD 物品页面
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
        InputConstants.Key inputKey = InputConstants.getKey(event.getKeyCode(), event.getScanCode());
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
        InputConstants.Key inputKey = InputConstants.getKey(event.getKeyCode(), event.getScanCode());
        searchKeyState = markReleased(inputKey, SEARCH_ON_MCMOD_KEY, searchKeyState);
        copyNameKeyState = markReleased(inputKey, COPY_ITEM_NAME_KEY, copyNameKeyState);
    }

    /**
     * 客户端 MOD 事件
     */
    @EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
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

    private static void showSearchFailedHint() {
        showToast(SystemToast.SystemToastId.WORLD_ACCESS_FAILURE, Component.translatable("text.searchonmcmod.search_failed"));
    }

    private static void showToast(SystemToast.SystemToastId id, Component message) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> SystemToast.addOrUpdate(minecraft.getToasts(), id, message, null));
    }

    private static void handleSearchFailure(String message, Exception e) {
        log.error(message, e);
        showSearchFailedHint();
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
