package com.yshs.searchonmcmod;

import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.yshs.searchonmcmod.KeyBindings.SEARCH_ON_MCMOD_KEY;

/**
 * 主类
 */
@Mod(modid = SearchOnMcmod.MOD_ID)
public class SearchOnMcmod {
    /**
     * MOD ID
     */
    public static final String MOD_ID = "searchonmcmod";
    private static final Logger log = LogManager.getLogger();
    private final AtomicBoolean allowOpenUrl = new AtomicBoolean(false);
    private final AtomicBoolean keyPressedFlag = new AtomicBoolean(false);

    /**
     * 构造函数
     */
    public SearchOnMcmod() {
        MinecraftForge.EVENT_BUS.register(this);
        ClientRegistry.registerKeyBinding(SEARCH_ON_MCMOD_KEY);
    }

    /**
     * 在物品tooltip渲染事件时触发
     *
     * @param event 物品tooltip事件，渲染物品信息时触发
     */
    @SubscribeEvent
    @SneakyThrows
    public void onRenderTooltipEvent(ItemTooltipEvent event) {
        if (!allowOpenUrl.getAndSet(false)) {
            return;
        }
        log.info("allowOpenUrl设置为false");
        // 1. 得到物品的注册表名
        ResourceLocation registryName = event.getItemStack().getItem().getRegistryName();
        if (registryName == null) {
            // 如果注册表名为空，直接搜索物品的本地化名称
            MainUtil.openSearchPage(event.getItemStack().getDisplayName());
            return;
        }
        String registryNameStr = registryName.toString();
        // 3. 如果注册表名为空气，则不进行搜索
        if ("minecraft:air".equals(registryNameStr)) {
            return;
        }
        // 4. 得到物品的metadata(如果有的话)
        int metadata = event.getItemStack().getMetadata();
        // 5. 得到物品的本地化名称
        String localizedName = event.getItemStack().getDisplayName();
        // 6. 异步获取物品的MCMODID
        CompletableFuture.runAsync(() -> {
            Optional<String> optionalItemMCMODID;
            try {
                optionalItemMCMODID = MainUtil.fetchItemMCMODID(registryNameStr, metadata);
            } catch (Exception e) {
                log.error("MC百科搜索: 无法通过百科 API 获取物品 MCMOD ID，请检查您的网络情况", e);
                // 发送提示消息
                Minecraft.getMinecraft().player.sendMessage(new TextComponentTranslation("text.searchonmcmod.mcmodid_not_found"));
                return;
            }
            if (!optionalItemMCMODID.isPresent()) {
                return;
            }
            String itemMCMODID = optionalItemMCMODID.get();

            // 6. 如果mcmodItemID为0，则进行搜索
            if ("0".equals(itemMCMODID)) {
                // 到https://search.mcmod.cn/s?key=%s去搜索
                MainUtil.openSearchPage(localizedName);
                return;
            }

            // 7. 判断物品页面是否存在，如果不存在则进行搜索
            if (!MainUtil.itemPageExist(itemMCMODID)) {
                // 到https://search.mcmod.cn/s?key=%s去搜索
                MainUtil.openSearchPage(localizedName);
                return;
            }

            // 8. 打开MCMOD的物品页面
            MainUtil.openItemPage(itemMCMODID);

        });
    }

    /**
     * 在按键按下事件时触发
     *
     * @param event 按键按下事件，监听是否按下搜索按键
     */
    @SubscribeEvent
    public void onKeyPressed(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        if (Keyboard.isKeyDown(SEARCH_ON_MCMOD_KEY.getKeyCode()) && !keyPressedFlag.get()) {
            keyPressedFlag.set(true);
            allowOpenUrl.set(true);
            log.info("按键已按下，keyPressedFlag和allowOpenUrl设置为true");
        }
    }

    /**
     * 在按键释放事件时触发
     *
     * @param event 按键释放事件，监听是否释放搜索按键
     */
    @SubscribeEvent
    public void onKeyReleased(GuiScreenEvent.KeyboardInputEvent.Post event) {
        if (!Keyboard.isKeyDown(SEARCH_ON_MCMOD_KEY.getKeyCode()) && keyPressedFlag.get()) {
            keyPressedFlag.set(false);
            log.info("按键已释放，keyPressedFlag设置为false");
        }
    }

}
