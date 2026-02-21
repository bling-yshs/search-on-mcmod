package com.yshs.searchonmcmod;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * NeoForge 客户端配置
 */
public final class SearchOnMcmodConfig {
    public static final ModConfigSpec CLIENT_SPEC;
    private static final Client CLIENT;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        CLIENT = new Client(builder);
        CLIENT_SPEC = builder.build();
    }

    private SearchOnMcmodConfig() {
    }

    public static boolean isSearchingHintEnabled() {
        return CLIENT.showSearchingHint.get();
    }

    public static boolean isCopyToClipboardHintEnabled() {
        return CLIENT.showCopyToClipboardHint.get();
    }

    public static void updateClientOptions(boolean showSearchingHint, boolean showCopyToClipboardHint) {
        CLIENT.showSearchingHint.set(showSearchingHint);
        CLIENT.showCopyToClipboardHint.set(showCopyToClipboardHint);
        CLIENT_SPEC.save();
    }

    private static final class Client {
        private final ModConfigSpec.BooleanValue showSearchingHint;
        private final ModConfigSpec.BooleanValue showCopyToClipboardHint;

        private Client(ModConfigSpec.Builder builder) {
            builder.push("client");

            showSearchingHint = builder
                    .comment("Show the 'Opening MCMOD' toast after triggering search key.")
                    .define("showSearchingHint", false);

            showCopyToClipboardHint = builder
                    .comment("Show a toast after copying hovered item name to clipboard.")
                    .define("showCopyToClipboardHint", false);

            builder.pop();
        }
    }
}
