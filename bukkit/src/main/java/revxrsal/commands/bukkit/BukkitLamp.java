/*
 * This file is part of lamp, licensed under the MIT License.
 *
 *  Copyright (c) Revxrsal <reflxction.github@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */
package revxrsal.commands.bukkit;

import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.LampBuilderVisitor;
import revxrsal.commands.brigadier.types.ArgumentTypes;
import revxrsal.commands.bukkit.actor.ActorFactory;
import revxrsal.commands.bukkit.annotation.CommandPermission;
import revxrsal.commands.bukkit.brigadier.BrigadierRegistryHook;
import revxrsal.commands.bukkit.exception.BukkitExceptionHandler;
import revxrsal.commands.bukkit.hooks.BukkitCommandHooks;
import revxrsal.commands.bukkit.parameters.EntitySelectorParameterTypeFactory;
import revxrsal.commands.bukkit.parameters.OfflinePlayerParameterType;
import revxrsal.commands.bukkit.parameters.PlayerParameterType;
import revxrsal.commands.bukkit.parameters.WorldParameterType;
import revxrsal.commands.bukkit.sender.BukkitPermissionFactory;
import revxrsal.commands.bukkit.sender.BukkitSenderResolver;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.exception.CommandExceptionHandler;
import revxrsal.commands.parameter.ContextParameter;

import static revxrsal.commands.bukkit.util.BukkitUtils.legacyColorize;
import static revxrsal.commands.bukkit.util.BukkitVersion.isBrigadierSupported;

/**
 * Includes modular building blocks for hooking into the Bukkit
 * platform.
 * <p>
 * Accept individual functions using {@link Lamp.Builder#accept(LampBuilderVisitor)}
 */
public final class BukkitLamp {

    /**
     * Makes the default format for {@link CommandActor#reply(String)} and {@link CommandActor#error(String)}
     * take the legacy ampersand ChatColor-coded format
     *
     * @param <A> The actor type
     * @return The visitor
     */
    public static <A extends BukkitCommandActor> @NotNull LampBuilderVisitor<A> legacyColorCodes() {
        return builder -> builder
                .defaultMessageSender((actor, message) -> actor.sendRawMessage(legacyColorize(message)))
                .defaultErrorSender((actor, message) -> actor.sendRawMessage(legacyColorize("&c" + message)));
    }

    /**
     * Handles the default Bukkit exceptions
     *
     * @param <A> The actor type
     * @return The visitor
     */
    public static <A extends BukkitCommandActor> @NotNull LampBuilderVisitor<A> bukkitExceptionHandler() {
        //noinspection unchecked
        return builder -> builder.exceptionHandler((CommandExceptionHandler<A>) new BukkitExceptionHandler());
    }

    /**
     * Resolves the sender type {@link CommandSender}, {@link Player} and {@link ConsoleCommandSender}
     * for parameters that come first in the command.
     *
     * @param <A> The actor type
     * @return The visitor
     */
    public static <A extends BukkitCommandActor> @NotNull LampBuilderVisitor<A> bukkitSenderResolver() {
        return builder -> builder.senderResolver(new BukkitSenderResolver());
    }

    /**
     * Registers the following parameter types:
     * <ul>
     *     <li>{@link Player}</li>
     *     <li>{@link OfflinePlayer}</li>
     *     <li>{@link World}</li>
     *     <li>{@link EntitySelector}</li>
     * </ul>
     *
     * @param <A> The actor type
     * @return The visitor
     */
    public static <A extends BukkitCommandActor> @NotNull LampBuilderVisitor<A> bukkitParameterTypes() {
        return builder -> builder.parameterTypes()
                .addParameterTypeLast(Player.class, new PlayerParameterType())
                .addParameterTypeLast(OfflinePlayer.class, new OfflinePlayerParameterType())
                .addParameterTypeLast(World.class, new WorldParameterType())
                .addParameterTypeFactoryLast(new EntitySelectorParameterTypeFactory());
    }

    /**
     * Adds a registration hook that injects Lamp commands into Bukkit
     *
     * @param plugin The plugin instance to bind commands to
     * @return The visitor
     */
    public static @NotNull LampBuilderVisitor<BukkitCommandActor> registrationHooks(@NotNull JavaPlugin plugin) {
        return registrationHooks(plugin, ActorFactory.defaultFactory());
    }

    /**
     * Adds a registration hook that injects Lamp commands into Bukkit.
     * <p>
     * This function allows to specify a custom {@link ActorFactory} to
     * use custom implementations of {@link BukkitCommandActor}
     *
     * @param plugin       The plugin instance to bind commands to
     * @param actorFactory The actor factory. This allows for creating custom {@link BukkitCommandActor}
     *                     implementations
     * @return The visitor
     */
    public static <A extends BukkitCommandActor> @NotNull LampBuilderVisitor<A> registrationHooks(
            @NotNull JavaPlugin plugin,
            @NotNull ActorFactory<A> actorFactory
    ) {
        BukkitCommandHooks hooks = new BukkitCommandHooks(plugin, actorFactory);
        return builder -> builder.hooks()
                .onCommandRegistered(hooks)
                .onCommandUnregistered(hooks);
    }

    /**
     * Adds {@link Plugin} dependencies and type resolvers
     *
     * @param plugin Plugin to supply
     * @param <A>    The actor type
     * @return The visitor
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <A extends BukkitCommandActor> @NotNull LampBuilderVisitor<A> pluginContextParameters(JavaPlugin plugin) {
        return builder -> {
            builder.parameterTypes().addContextParameterLast(Plugin.class, (parameter, input, context) -> plugin);
            builder.parameterTypes().addContextParameterLast(plugin.getClass(), (ContextParameter) (parameter, input, context) -> plugin);
            builder.dependency(Plugin.class, plugin);
            builder.dependency((Class) plugin.getClass(), plugin);
        };
    }

    /**
     * Adds support for the {@link CommandPermission} annotation
     *
     * @param <A> The actor type
     * @return This visitor
     */
    public static <A extends BukkitCommandActor> @NotNull LampBuilderVisitor<A> bukkitPermissions() {
        return builder -> builder
                .permissionFactory(BukkitPermissionFactory.INSTANCE);
    }

    /**
     * Adds a registration hook that injects Lamp commands into Bukkit's Brigadier.
     * <p>
     * This function allows to specify a custom {@link ActorFactory} to
     * use custom implementations of {@link BukkitCommandActor}
     *
     * @param plugin The plugin instance to bind commands to
     * @return The visitor
     */
    public static @NotNull LampBuilderVisitor<BukkitCommandActor> brigadier(
            @NotNull JavaPlugin plugin
    ) {
        ArgumentTypes.Builder<BukkitCommandActor> builder = BukkitArgumentTypes.builder();
        return brigadier(plugin, builder.build(), ActorFactory.defaultFactory());
    }

    /**
     * Adds a registration hook that injects Lamp commands into Bukkit's Brigadier.
     *
     * @param argumentTypes The argument types registry. See {@link BukkitArgumentTypes} for
     *                      Bukkit types
     * @param plugin        The plugin instance to bind commands to
     * @return The visitor
     */
    public static @NotNull LampBuilderVisitor<BukkitCommandActor> brigadier(
            @NotNull JavaPlugin plugin,
            @NotNull ArgumentTypes<? super BukkitCommandActor> argumentTypes
    ) {
        return brigadier(plugin, argumentTypes, ActorFactory.defaultFactory());
    }

    /**
     * Adds a registration hook that injects Lamp commands into Bukkit's Brigadier.
     * <p>
     * This function allows to specify a custom {@link ActorFactory} to
     * use custom implementations of {@link BukkitCommandActor}
     *
     * @param plugin        The plugin instance to bind commands to
     * @param argumentTypes The argument types registry. See {@link BukkitArgumentTypes} for
     *                      Bukkit types
     * @param actorFactory  The actor factory. This allows for creating custom {@link BukkitCommandActor}
     *                      implementations
     * @return The visitor
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <A extends BukkitCommandActor> @NotNull LampBuilderVisitor<A> brigadier(
            @NotNull JavaPlugin plugin,
            @NotNull ArgumentTypes<? super A> argumentTypes,
            @NotNull ActorFactory<A> actorFactory
    ) {
        if (isBrigadierSupported()) {
            return builder -> builder.hooks()
                    .onCommandRegistered(new BrigadierRegistryHook<>(((ArgumentTypes) argumentTypes), actorFactory, plugin));
        }
        return LampBuilderVisitor.nothing();
    }

    /**
     * Returns a {@link Lamp.Builder} that contains the default registrations
     * for the Bukkit platform
     *
     * @param plugin        The plugin instance
     * @param argumentTypes The argument types registry. See {@link BukkitArgumentTypes}
     * @param actorFactory  The actor factory for creating custom implementations of {@link BukkitCommandActor}
     * @param <A>           The actor type
     * @return A {@link Lamp.Builder}
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <A extends BukkitCommandActor> Lamp.Builder<A> defaultBuilder(
            @NotNull JavaPlugin plugin,
            @NotNull ArgumentTypes<? super A> argumentTypes,
            @NotNull ActorFactory<A> actorFactory
    ) {
        return Lamp.builder(BukkitCommandActor.class)
                .accept(legacyColorCodes())
                .accept(bukkitSenderResolver())
                .accept(bukkitParameterTypes())
                .accept(bukkitExceptionHandler())
                .accept(bukkitPermissions())
                .accept(registrationHooks(plugin))
                .accept(brigadier(plugin, (ArgumentTypes) argumentTypes, actorFactory))
                .accept(pluginContextParameters(plugin));
    }

    /**
     * Returns a {@link Lamp.Builder} that contains the default registrations
     * for the Bukkit platform
     *
     * @param plugin        The plugin instance
     * @param argumentTypes The argument types registry. See {@link BukkitArgumentTypes}
     * @return A {@link Lamp.Builder}
     */
    public static Lamp.Builder<BukkitCommandActor> defaultBuilder(
            @NotNull JavaPlugin plugin,
            @NotNull ArgumentTypes<BukkitCommandActor> argumentTypes
    ) {
        return defaultBuilder(plugin, argumentTypes, ActorFactory.defaultFactory());
    }

    /**
     * Returns a {@link Lamp.Builder} that contains the default registrations
     * for the Bukkit platform
     *
     * @param plugin The plugin instance
     * @return A {@link Lamp.Builder}
     */
    public static Lamp.Builder<BukkitCommandActor> defaultBuilder(@NotNull JavaPlugin plugin) {
        ArgumentTypes.Builder<BukkitCommandActor> argumentTypes = BukkitArgumentTypes.builder();
        return defaultBuilder(plugin, argumentTypes.build());
    }
}