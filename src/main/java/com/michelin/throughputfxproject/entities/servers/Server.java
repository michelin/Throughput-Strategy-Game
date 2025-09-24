/*
 * Copyright 2025 Manufacture Française des Pneumatiques Michelin
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.michelin.throughputfxproject.entities.servers;

import com.michelin.throughputfxproject.entities.Color;
import com.michelin.throughputfxproject.entities.state.Savable;

import java.util.Set;


@SuppressWarnings("SameReturnValue")
public interface Server extends Savable {
    String TYPE_AUTOMATED = "AUTOMATED";
    String TYPE_HUMAN = "HUMAN";
    String TYPE_PARTNER = "PAIR";

    Color getColor();
    String getType();
    Set<Color> getSkills();
    String getImage();
    String getBackImage();
    String toJSON();
}
