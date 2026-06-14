package com.yshs.searchonmcmod;

import lombok.extern.slf4j.Slf4j;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.yshs.searchonmcmod.KeyBindings.SEARCH_ON_MCMOD_KEY;

/**
 * 主类
 */
@Slf4j
public class SearchOnMcmod implements ModInitializer {
    /**
     * 是否按下按键
     */
    public static boolean keyDown = false;
    public static final String MOD_ID = "searchonmcmod";
    @Override
    public void onInitialize() {
        // 注册按键绑定
        KeyBindingHelper.registerKeyBinding(SEARCH_ON_MCMOD_KEY);
        // 渲染物品信息时触发
        ItemTooltipCallback.EVENT.register(this::onRenderTooltipEvent);
    }

    /**
     * 当渲染物品tooltip时触发的
     *
     * @param itemStack      物品栈
     * @param tooltipContext 物品信息上下文
     * @param tooltipFlag    物品信息提示标志
     * @param componentList  物品信息列表
     */
    public void onRenderTooltipEvent(ItemStack itemStack, Item.TooltipContext tooltipContext, TooltipFlag tooltipFlag, List<Component> componentList) {
        if (keyDown == false) {
            return;
        }
        keyDown = false;

        if (itemStack.isEmpty()) {
            return;
        }

        // 得到物品的本地化名称
        String localizedName = itemStack.getHoverName().getString();
        // 得到物品的注册名
        Item item = itemStack.getItem();
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
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
                handleSearchFailure("MC百科搜索: 打开MC百科页面失败", e);
            }
        });
    }

    /**
     * 处理 MC 百科搜索失败
     *
     * @param message 日志消息
     * @param e       异常
     */
    private static void handleSearchFailure(String message, Exception e) {
        log.error(message, e);
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            player.displayClientMessage(Component.translatable("text.searchonmcmod.error_see_log"), false);
        }
    }

}
