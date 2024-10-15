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

package ch.puzzle.itc.mobiliar.business.globalfunction.boundary;

import ch.puzzle.itc.mobiliar.business.database.entity.MyRevisionEntity;
import ch.puzzle.itc.mobiliar.business.globalfunction.control.GlobalFunctionService;
import ch.puzzle.itc.mobiliar.business.globalfunction.entity.GlobalFunctionEntity;
import ch.puzzle.itc.mobiliar.business.security.entity.Permission;
import ch.puzzle.itc.mobiliar.business.security.interceptor.HasPermission;
import ch.puzzle.itc.mobiliar.business.template.control.FreemarkerSyntaxValidator;
import ch.puzzle.itc.mobiliar.business.template.entity.RevisionInformation;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;
import ch.puzzle.itc.mobiliar.common.exception.NotFoundException;
import ch.puzzle.itc.mobiliar.common.exception.ValidationException;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Stateless
public class GlobalFunctionsBoundary {

    @Inject
    EntityManager entityManager;

    @Inject
    GlobalFunctionService functionService;

    @Inject
    FreemarkerSyntaxValidator freemarkerValidator;

    public List<GlobalFunctionEntity> getAllGlobalFunctions() {
        return functionService.getAllGlobalFunctions();
    }

    public List<GlobalFunctionEntity> getAllGlobalFunctions(Date date) {
        return functionService.getAllGlobalFunctionsAtDate(date);
    }

    public GlobalFunctionEntity getFunctionById(Integer globalFunctionId) throws NotFoundException {
        GlobalFunctionEntity gf = entityManager.find(GlobalFunctionEntity.class, globalFunctionId);
        if (gf == null) {
            throw new NotFoundException("Function not found with Id " + globalFunctionId);
        }
        return gf;
    }

    /**
     * Returns a AmwFunctionEntity identified by its id and revision id
     */
    public GlobalFunctionEntity getFunctionByIdAndRevision(Integer functionId, Number revisionId) throws NotFoundException {
        GlobalFunctionEntity globalFunctionEntity = AuditReaderFactory.get(entityManager).find(
                GlobalFunctionEntity.class, functionId, revisionId);
        if (globalFunctionEntity == null) {
            throw new NotFoundException("No function with id " + functionId + " and revision id " + revisionId + " found");
        }
        return globalFunctionEntity;
    }

    /**
     * Returns all RevisionInformation for the specified function id
     */
    public List<RevisionInformation> getFunctionRevisions(Integer functionId) {
        List<RevisionInformation> result = new ArrayList<RevisionInformation>();
        if (functionId != null) {
            AuditReader reader = AuditReaderFactory.get(entityManager);
            List<Number> list = reader.getRevisions(GlobalFunctionEntity.class, functionId);
            for (Number rev : list) {
                Date date = reader.getRevisionDate(rev);
                MyRevisionEntity myRev = entityManager.find(MyRevisionEntity.class, rev);
                result.add(new RevisionInformation(rev, date, myRev.getUsername()));
            }
            Collections.sort(result);
        }
        return result;
    }

    @HasPermission(permission = Permission.MANAGE_GLOBAL_FUNCTIONS)
    public void deleteGlobalFunction(Integer globalFunctionId) throws NotFoundException {
        GlobalFunctionEntity gf = entityManager.find(GlobalFunctionEntity.class, globalFunctionId);
        if (gf == null) {
            throw new NotFoundException("Function not found with Id " + globalFunctionId);
        }
        deleteGlobalFunction(gf);
    }

    @HasPermission(permission = Permission.MANAGE_GLOBAL_FUNCTIONS)
    public void deleteGlobalFunction(GlobalFunctionEntity function) {
        functionService.deleteFunction(function);
    }

    @HasPermission(permission = Permission.MANAGE_GLOBAL_FUNCTIONS)
    public boolean saveGlobalFunction(GlobalFunctionEntity function) throws ValidationException {
        if (function != null) {
            if (StringUtils.isEmpty(function.getName()) || StringUtils.isEmpty(function.getContent())) {
                throw new ValidationException("The function must not be empty");
            }
            try {
                freemarkerValidator.validateFreemarkerSyntax(function.getContent());
            } catch (AMWException e) {
                throw new ValidationException(e.getMessage(), e);
            }
            boolean saved = functionService.saveFunction(function);
            if (!saved) {
                throw new ValidationException("Function with same name already exists");
            }
        }
        return true;
    }

    public boolean isExistingId(Integer id) {
        return functionService.isExistingId(id);
    }
}
