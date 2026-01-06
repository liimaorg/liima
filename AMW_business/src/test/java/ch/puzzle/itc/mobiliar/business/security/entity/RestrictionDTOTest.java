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

package ch.puzzle.itc.mobiliar.business.security.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.puzzle.itc.mobiliar.builders.ContextEntityBuilder;

public class RestrictionDTOTest {

    private RoleEntity rol;
    private PermissionEntity per;

    @BeforeEach
    public void setUp(){
        rol = new RoleEntity();
        rol.setName("testRole");
        per = new PermissionEntity();
        per.setValue("testPermission");
    }

    @Test
    public void constructedWithRestrictionItShouldHaveRightAction(){
        //given
        RestrictionEntity res = new RestrictionEntity();
        res.setAction(Action.READ);
        res.setPermission(per);

        //when
        RestrictionDTO resDTO = new RestrictionDTO(res);

        //then
        assertEquals(per.getValue(), resDTO.getPermissionName());
        assertEquals(res.getAction(), resDTO.getRestriction().getAction());
    }

    @Test
    public void constructedWithRestrictionItShouldHaveRightContext(){
        //given
        RestrictionEntity res = new RestrictionEntity();
        res.setAction(Action.CREATE);
        res.setContext(new ContextEntityBuilder().buildContextEntity("TEST", null, Collections.EMPTY_SET,false));
        res.setPermission(per);
        res.setRole(rol);

        //when
        RestrictionDTO resDTO = new RestrictionDTO(res);

        //then
        assertEquals(rol.getName(), resDTO.getRestriction().getRole().getName());
        assertEquals(res.getContext().getName(), resDTO.getRestriction().getContext().getName());
    }
}
