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
package revxrsal.commands.sponge;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.Lamp;
import revxrsal.commands.sponge.actor.SpongeCommandActor;

/**
 * Creates {@link Lamp} instances that contain relevant registrations
 * for the Sponge platform.
 */
public final class SpongeLamp {

    /**
     * Returns a {@link Lamp.Builder} that contains the default registrations
     * for the Sponge platform
     *
     * @param plugin The plugin instance to register with
     * @return A {@link Lamp.Builder} containing the default
     */
    public static Lamp.Builder<SpongeCommandActor> builder(@NotNull Object plugin) {
        return builder(SpongeLampConfig.createDefault(plugin));
    }

    /**
     * Returns a {@link Lamp.Builder} that contains the default registrations
     * for the Sponge platform, as well as any customizations in the given
     * {@link SpongeLampConfig}
     *
     * @param config The Lamp configuration
     * @return A {@link Lamp.Builder}
     */
    public static <A extends SpongeCommandActor> Lamp.Builder<A> builder(@NotNull SpongeLampConfig<A> config) {
        return Lamp.<A>builder()
                .accept(config);
    }
}
