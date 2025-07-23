/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.michelin.throughputfxproject.entities.cards;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.File;

@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class ChanceRobotCard extends ChanceCard {


    @Override
    public File getBackImage() {
        return new File("./cards/ChanceTheRobot.jpg");
    }

    @Override
    public String getType() {
        return Card.AUTOMATED_CHANCE;
    }

    @Override
    public String getTitle() {
        return Card.AUTOMATED_CHANCE;
    }


    @Override
    public ChanceRobotCard typedClone() {
        return this.toBuilder().build();
    }

    @Override
    public String toString() {
        return "{" + "type='" + getType() + '\'' +
                ", chance='" + getChanceText() + '\'' +
                ", instructions='" + getInstructions() + '\'' +
                '}';
    }
}
