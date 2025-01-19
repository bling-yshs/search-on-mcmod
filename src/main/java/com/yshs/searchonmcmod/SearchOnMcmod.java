package com.yshs.searchonmcmod;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import lombok.SneakyThrows;
import net.minecraft.Util;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static com.yshs.searchonmcmod.KeyBindings.SEARCH_ON_MCMOD_KEY;

@Mod(SearchOnMcmod.MOD_ID)
public class SearchOnMcmod {
    public static final String MOD_ID = "searchonmcmod";
    private static final Logger log = LogUtils.getLogger();
    private static boolean keyDown = false;

    public SearchOnMcmod() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    @SneakyThrows
    public void onRenderTooltipEvent(ItemTooltipEvent event) {
        if (keyDown == false) {
            return;
        }
        keyDown = false;
        // 1. 得到物品的描述ID
        String descriptionId = event.getItemStack().getItem().getDescriptionId();
        // 2. 转换为注册表名
        String registryName = SearchOnMcmod.convertDescriptionIdToRegistryName(descriptionId);
        // 3. 如果注册表名为空或"minecraft:air"，则返回
        if (registryName.isEmpty() || "minecraft:air".equals(registryName)) {
            return;
        }
        // 4.查找并得到物品在MCMOD中的ID
        String urlStr = String.format("https://api.mcmod.cn/getItem/?regname=%s", registryName);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(urlStr))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            return;
        }
        String mcmodItemID = response.body();
        log.info("mcmodItemID: " + mcmodItemID);

        // 5. 如果mcmodItemID为0，则进行搜索
        if ("0".equals(mcmodItemID)) {
            // 得到物品的本地化名称
            String localizedName = event.getItemStack().getHoverName().getString();
            // 然后到https://search.mcmod.cn/s?key=%s去搜索
            String searchUrl = String.format("https://search.mcmod.cn/s?key=%s", localizedName);
            Util.getPlatform().openUri(searchUrl);
            return;
        }

        // 6. 打开MCMOD的物品页面
        String mcmodPageUrl = String.format("https://www.mcmod.cn/item/%s.html", mcmodItemID);
        log.info("mcmodPageUrl: " + mcmodPageUrl);
        Util.getPlatform().openUri(mcmodPageUrl);

    }

    @SubscribeEvent
    public void onKeyPressed(ScreenEvent.KeyboardKeyPressedEvent.Post event) {
        int keyCode = event.getKeyCode();
        InputConstants.Key key = SEARCH_ON_MCMOD_KEY.getKey();
        if (keyCode == key.getValue() && keyDown == false) {
            keyDown = true;
            log.info("按键已按下，keyDown设置为true");
        }
    }

    @SubscribeEvent
    public void onKeyReleased(ScreenEvent.KeyboardKeyReleasedEvent.Post event) {
        int keyCode = event.getKeyCode();
        if (keyCode == SEARCH_ON_MCMOD_KEY.getKey().getValue()) {
            keyDown = false;
            log.info("按键已释放，keyDown设置为false");
        }
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void setup(final FMLCommonSetupEvent event) {
            ClientRegistry.registerKeyBinding(SEARCH_ON_MCMOD_KEY);
        }

    }

    public static String convertDescriptionIdToRegistryName(@NotNull String descriptionId) {
        // 将输入字符串按"."分割
        String[] parts = descriptionId.split("\\.");

        // 返回格式化后的字符串
        if (parts.length >= 2) {
            return parts[1] + ":" + parts[2];
        } else {
            // 如果格式不符合预期，返回空字符串
            return "";
        }
    }
}
