package com.yshs.searchonmcmod;

import com.mojang.blaze3d.platform.InputConstants;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private final AtomicBoolean keyPressedFlag = new AtomicBoolean(false);
    private final AtomicBoolean hasTriggeredSearch = new AtomicBoolean(false);

    /**
     * 构造函数
     */
    public SearchOnMcmod() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    /**
     * 在物品tooltip渲染事件时触发
     *
     * @param event 物品tooltip事件，渲染物品信息时触发
     */
    @SubscribeEvent
    public void onRenderTooltipEvent(ItemTooltipEvent event) {
        if (!keyPressedFlag.get() || hasTriggeredSearch.get()) {
            return;
        }
        hasTriggeredSearch.set(true);
        log.info("触发了MC百科搜索");
        showSearchingHint();
        // 1. 得到物品的描述ID
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
        // 4. 如果注册表明为空，但是物品的描述ID不为空，则进行搜索
        if (StringUtils.isBlank(registryName) && StringUtils.isNotBlank(descriptionId)) {
            try {
                MainUtil.openSearchPage(descriptionId);
            } catch (Exception e) {
                handleSearchFailure("MC百科搜索: 打开搜索页面失败", e);
            }
            return;
        }

        // 得到物品的本地化名称
        String localizedName = event.getItemStack().getHoverName().getString();

        CompletableFuture.runAsync(() -> {
            // 5. 查找并得到物品在MCMOD中的ID
            String itemMCMODID;
            try {
                itemMCMODID = MainUtil.fetchItemMCMODID(registryName);
            } catch (Exception e) {
                handleSearchFailure("MC百科搜索: 无法通过百科 API 获取物品 MCMOD ID", e);
                return;
            }

            try {
                // 6. 如果mcmodItemID为0，则进行搜索
                if ("0".equals(itemMCMODID)) {
                    MainUtil.openSearchPage(localizedName);
                    return;
                }

                // 7. 判断物品页面是否存在，如果不存在则进行搜索
                if (!MainUtil.itemPageExist(itemMCMODID)) {
                    MainUtil.openSearchPage(localizedName);
                    return;
                }

                // 8. 打开MCMOD的物品页面
                MainUtil.openItemPage(itemMCMODID);
            } catch (Exception e) {
                handleSearchFailure("MC百科搜索: 打开MC百科页面失败", e);
            }
        });
    }

    @SubscribeEvent
    public void onKeyPressed(ScreenEvent.KeyPressed.Post event) {
        InputConstants.Key inputKey = InputConstants.getKey(event.getKeyCode(), event.getScanCode());
        if (!SEARCH_ON_MCMOD_KEY.isActiveAndMatches(inputKey)) {
            return;
        }
        if (keyPressedFlag.get()) {
            return;
        }
        keyPressedFlag.set(true);
        // 重置触发标志
        hasTriggeredSearch.set(false);
        log.info("SEARCH_ON_MCMOD_KEY按键已按下，keyPressedFlag设置为true");
    }

    @SubscribeEvent
    public void onKeyReleased(ScreenEvent.KeyReleased.Post event) {
        InputConstants.Key inputKey = InputConstants.getKey(event.getKeyCode(), event.getScanCode());
        InputConstants.Key settingsKey = SEARCH_ON_MCMOD_KEY.getKey();
        KeyModifier keyModifier = SEARCH_ON_MCMOD_KEY.getKeyModifier();
        if (!settingsKey.equals(inputKey) && !keyModifier.matches(inputKey)) {
            return;
        }
        keyPressedFlag.set(false);
        hasTriggeredSearch.set(false);
        log.info("SEARCH_ON_MCMOD_KEY按键已释放，keyPressedFlag设置为false");
    }

    /**
     * 客户端事件
     */
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        /**
         * @param event 事件
         */
        @SubscribeEvent
        public static void registerBindings(RegisterKeyMappingsEvent event) {
            event.register(SEARCH_ON_MCMOD_KEY);
        }

    }

    private static void showSearchingHint() {
        showToast(SystemToast.SystemToastIds.PERIODIC_NOTIFICATION, Component.translatable("text.searchonmcmod.searching"));
    }

    private static void showSearchFailedHint() {
        showToast(SystemToast.SystemToastIds.WORLD_ACCESS_FAILURE, Component.translatable("text.searchonmcmod.search_failed"));
    }

    private static void showToast(SystemToast.SystemToastIds id, Component message) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> SystemToast.addOrUpdate(minecraft.getToasts(), id, message, null));
    }

    private static void handleSearchFailure(String message, Exception e) {
        log.error(message, e);
        showSearchFailedHint();
    }

}
