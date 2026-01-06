/*
 * AMW - Automated Middleware allows you to manage the configurations of
 * your Java EE applications on an unlimited number of different environments
 * with various versions, including the automated deployment of those apps.
 * Copyright (C) 2013-2016 by Puzzle ITC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ch.puzzle.itc.mobiliar.business.generator.control.extracted;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GenerationModusTest {

    @Test
    public void testIsSendNotificationGenerationModus() throws Exception {
        assertTrue(GenerationModus.DEPLOY.isSendNotificationOnErrorGenerationModus());
        assertTrue(GenerationModus.PREDEPLOY.isSendNotificationOnErrorGenerationModus());
        assertFalse(GenerationModus.SIMULATE.isSendNotificationOnErrorGenerationModus());
        assertFalse(GenerationModus.TEST.isSendNotificationOnErrorGenerationModus());
    }

    @Test
    public void testIsSendNotificationOnSuccessGenerationModus() throws Exception {
        assertTrue(GenerationModus.DEPLOY.isSendNotificationOnSuccessGenerationModus());
        assertFalse(GenerationModus.PREDEPLOY.isSendNotificationOnSuccessGenerationModus());
        assertFalse(GenerationModus.SIMULATE.isSendNotificationOnSuccessGenerationModus());
        assertFalse(GenerationModus.TEST.isSendNotificationOnSuccessGenerationModus());
    }
}