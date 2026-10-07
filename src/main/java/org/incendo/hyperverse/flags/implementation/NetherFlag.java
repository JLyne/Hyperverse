//
//  Hyperverse - A minecraft world management plugin
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program. If not, see <http://www.gnu.org/licenses/>.
//

package org.incendo.hyperverse.flags.implementation;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.hyperverse.configuration.Messages;
import org.incendo.hyperverse.flags.FlagParseException;
import org.incendo.hyperverse.flags.WorldFlag;
import org.incendo.hyperverse.util.WorldUtil;

public final class NetherFlag extends WorldFlag<String, NetherFlag> {

    public static final NetherFlag NETHER_FLAG_DEFAULT = new NetherFlag("");

    private NetherFlag(final @NonNull String value) {
        super(value, Messages.flagDescriptionNether);
    }

    @Override
    public NetherFlag parse(final @NonNull String input) throws FlagParseException {
        if (WorldUtil.validateName(input)) {
            return this.flagOf(input);
        }
        throw new FlagParseException(this, input, "A world name may only contain (up to) 16 alphanumerical characters, - and _");
    }

    @Override
    public NetherFlag merge(final @NonNull String newValue) {
        return this.flagOf(newValue);
    }

    @Override
    public String toString() {
        return this.getValue();
    }

    @Override
    public String getExample() {
        return "nether_world";
    }

    @Override
    public NetherFlag flagOf(final @NonNull String value) {
        return new NetherFlag(value);
    }

    //FIXME: Needs proper argument type
    @Override
    public ArgumentType<String> getArgumentType() {
        return StringArgumentType.word();
    }

    @Override
    public Class<String> valueClass() {
        return String.class;
    }
}
