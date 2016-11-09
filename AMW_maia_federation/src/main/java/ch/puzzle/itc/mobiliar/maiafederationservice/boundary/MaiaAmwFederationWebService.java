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

package ch.puzzle.itc.mobiliar.maiafederationservice.boundary;

import ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederationservicetypes.v1_0.UpdateRequest;
import ch.mobi.xml.datatype.ch.mobi.maia.amw.maiaamwfederationservicetypes.v1_0.UpdateResponse;
import ch.mobi.xml.datatype.common.commons.v3.CallContext;
import ch.mobi.xml.service.ch.mobi.maia.amw.maiaamwfederationservice.v1_0.BusinessException;
import ch.mobi.xml.service.ch.mobi.maia.amw.maiaamwfederationservice.v1_0.MaiaAmwFederationPortType;
import ch.mobi.xml.service.ch.mobi.maia.amw.maiaamwfederationservice.v1_0.TechnicalException;
import ch.mobi.xml.service.ch.mobi.maia.amw.maiaamwfederationservice.v1_0.ValidationException;

import javax.inject.Inject;
import javax.jws.WebService;

@WebService(endpointInterface = "ch.mobi.xml.service.ch.mobi.maia.amw.maiaamwfederationservice.v1_0.MaiaAmwFederationPortType")
public class MaiaAmwFederationWebService implements MaiaAmwFederationPortType {

    @Inject
    private MaiaAmwFederationServiceApplicationBean maiaAmwFederationServiceApplicationBean;

    @Override
    public UpdateResponse update(CallContext callContext, String fcOwner, UpdateRequest update) throws BusinessException, TechnicalException, ValidationException {
        return maiaAmwFederationServiceApplicationBean.update(callContext, fcOwner, update);
    }

    @Override
    public void ping() {
        maiaAmwFederationServiceApplicationBean.ping();
    }
}
