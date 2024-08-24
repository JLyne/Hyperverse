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

package org.incendo.hyperverse.configuration;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Hyperverse configuration options
 */
public interface HyperConfiguration {

    /**
     * Whether or not Hyperverse should
     * import detected worlds automatically
     *
     * @return True if worlds should be imported
     *         automatically
     */
    boolean shouldImportAutomatically();

    /**
     * Whether or not Hyperverse's location
     * persistence system should be enabled
     *
     * @return True if locations should persist
     */
    boolean shouldPersistLocations();

    /**
     * Get the language code that will be used
     * to resolve translations
     *
     * @return Language code
     */
    @NonNull String getLanguageCode();

    /**
     * Whether or not Hyperverse should enforce
     * safe teleportation
     *
     * @return True if Hyperverse should enforce
     *         safe teleportation
     */
    boolean shouldSafeTeleport();

    /**
     * Whether or not Hyperverse should print
     * verbose debugging messages
     *
     * @return True if Hyperverse should print verbose debugging messages
     */
    boolean shouldPrintDebug();

}
