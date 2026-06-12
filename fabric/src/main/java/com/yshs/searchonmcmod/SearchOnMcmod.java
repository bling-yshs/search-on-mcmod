package com.yshs.searchonmcmod;

import lombok.extern.slf4j.Slf4j;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

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
     * @param itemStack     物品栈
     * @param tooltipFlag   物品信息提示标志
     * @param componentList 物品信息列表
     */
    public void onRenderTooltipEvent(ItemStack itemStack, TooltipFlag tooltipFlag, List<Component> componentList) {
        if (keyDown == false) {
            return;
        }
        keyDown = false;
        if (itemStack.isEmpty()) {
            return;
        }
        // 得到物品的本地化名称
        String searchKeyword = itemStack.getHoverName().getString();
        if (StringUtils.isBlank(searchKeyword)) {
            return;
        }
        try {
            MainUtil.openSearchPage(searchKeyword);
        } catch (Exception e) {
            log.error("MC百科搜索: 打开搜索页面失败", e);
        }
    }

}
