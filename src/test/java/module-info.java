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

module com.michelin.throughputfxproject.test {
    requires com.michelin.throughputfxproject;
    requires org.mockito.junit.jupiter;
    requires org.mockito;
    requires org.junit.jupiter.api;
    requires javafx.controls;
    requires javafx.fxml;
    requires static lombok;


    opens com.michelin.throughputfxproject.test.controller to org.junit.platform.commons;
    opens com.michelin.throughputfxproject.test.service to org.junit.platform.commons;
    opens com.michelin.throughputfxproject.test.state to org.junit.platform.commons;
}