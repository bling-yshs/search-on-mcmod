package com.yshs.searchonmcmod;

import com.mojang.blaze3d.platform.InputConstants;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

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
        // 检查是否按下快捷键且还未触发过搜索
        if (!keyPressedFlag.get() || hasTriggeredSearch.get()) {
            return;
        }
        // 设置已触发标志，保证一次按键只触发一次
        hasTriggeredSearch.set(true);
        log.info("触发了MC百科搜索");
        if (event.getItemStack().isEmpty()) {
            return;
        }
        // 得到物品的本地化名称
        String localizedName = event.getItemStack().getHoverName().getString();
        // 得到物品的注册名
        Item item = event.getItemStack().getItem();
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
        if (id == null) {
            handleSearchFailure("MC百科搜索: 无法获取物品注册名", new IllegalStateException("物品未注册: " + item));
            return;
        }
        String registryName = id.toString();
        CompletableFuture.runAsync(() -> {
            String itemMCMODID;
            try {
                itemMCMODID = MainUtil.fetchItemMCMODID(registryName);
            } catch (Exception e) {
                handleSearchFailure("MC百科搜索: 无法通过百科 API 获取物品 MCMOD ID", e);
                return;
            }

            try {
                if ("0".equals(itemMCMODID)) {
                    MainUtil.openSearchPage(localizedName);
                    return;
                }
                MainUtil.openItemPage(itemMCMODID);
            } catch (Exception e) {
                handleSearchFailure("MC百科搜索: 打开 MC 百科页面失败", e);
            }
        });
    }

    private static void handleSearchFailure(String message, Exception e) {
        log.error(message, e);
        showSearchFailedHint();
    }

    private static void showSearchFailedHint() {
        net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
        minecraft.execute(() -> net.minecraft.client.gui.components.toasts.SystemToast.addOrUpdate(
                minecraft.getToasts(),
                net.minecraft.client.gui.components.toasts.SystemToast.SystemToastId.WORLD_ACCESS_FAILURE,
                net.minecraft.network.chat.Component.translatable("text.searchonmcmod.error_see_log"),
                null
        ));
    }

    /**
     * @param event 键盘按下事件
     */
    @SubscribeEvent
    public void onKeyPressed(ScreenEvent.KeyPressed.Pre event) {
        int eventKeyCode = event.getKeyCode();
        InputConstants.Key settingsKey = SEARCH_ON_MCMOD_KEY.getKey();
        if (eventKeyCode != settingsKey.getValue()) {
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

    /**
     * @param event 键盘释放事件
     */
    @SubscribeEvent
    public void onKeyReleased(ScreenEvent.KeyReleased.Post event) {
        int eventKeyCode = event.getKeyCode();
        InputConstants.Key settingsKey = SEARCH_ON_MCMOD_KEY.getKey();
        if (eventKeyCode != settingsKey.getValue()) {
            return;
        }
        keyPressedFlag.set(false);
        // 重置触发标志
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

}
