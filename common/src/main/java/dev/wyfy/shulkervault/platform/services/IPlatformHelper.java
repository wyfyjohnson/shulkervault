package dev.wyfy.shulkervault.platform.services;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;

import java.util.function.Consumer;

public interface IPlatformHelper {

    /**
     * Gets the name of the current platform
     *
     * @return The name of the current platform.
     */
    String getPlatformName();

    /**
     * Checks if a mod with the given id is loaded.
     *
     * @param modId The mod to check if it is loaded.
     * @return True if the mod is loaded, false otherwise.
     */
    boolean isModLoaded(String modId);

    /**
     * Check if the game is currently in a development environment.
     *
     * @return True if in a development environment, false otherwise.
     */
    boolean isDevelopmentEnvironment();

    /**
     * Gets the name of the environment type as a string.
     *
     * @return The name of the environment type.
     */
    default String getEnvironmentName() {

        return isDevelopmentEnvironment() ? "development" : "production";
    }

    /**
     * Creates a platform-specific inventory handler with the given size and max stack size.
     *
     * @param size The number of slots in the inventory.
     * @param maxStackSize The maximum stack size for each slot.
     * @return A platform-specific inventory handler.
     */
    IInventoryHandler createInventoryHandler(int size, int maxStackSize);

    /**
     * Opens a menu with extra data sent to the client.
     *
     * @param player The server player opening the menu.
     * @param provider The menu provider.
     * @param extraData Consumer that writes extra data to the buffer sent to the client.
     */
    void openExtendedMenu(ServerPlayer player, MenuProvider provider, Consumer<FriendlyByteBuf> extraData);
}