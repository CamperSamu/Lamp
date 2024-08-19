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
package revxrsal.commands.bukkit.parameters;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.exception.InvalidPlayerException;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.parameter.ParameterType;
import revxrsal.commands.stream.MutableStringStream;
import revxrsal.commands.stream.StringStream;

import java.util.List;

import static revxrsal.commands.util.Collections.map;

/**
 * A parameter type for {@link OfflinePlayer} types
 * <p>
 * If the player inputs {@code me} or {@code self}, the parser will return the
 * executing player (or give an error if the sender is not a player)
 */
public final class OfflinePlayerParameterType implements ParameterType<BukkitCommandActor, OfflinePlayer> {

    @Override
    @SuppressWarnings("deprecation")
    public OfflinePlayer parse(@NotNull MutableStringStream input, @NotNull ExecutionContext<BukkitCommandActor> context) {
        String name = input.readString();
        if (name.equals("self") || name.equals("me") || name.equals("@s"))
            return context.actor().requirePlayer();
        OfflinePlayer player = Bukkit.getOfflinePlayer(name);
        if (exists(player))
            return player;
        throw new InvalidPlayerException(name);
    }

    @Override
    public @NotNull List<String> defaultSuggestions(@NotNull StringStream input, @NotNull BukkitCommandActor actor, @NotNull ExecutionContext<BukkitCommandActor> context) {
        return map(Bukkit.getOnlinePlayers(), Player::getName);
    }

    private static boolean exists(OfflinePlayer player) {
        return player.hasPlayedBefore() || player.isOnline() || player.getFirstPlayed() != 0L;
    }
}