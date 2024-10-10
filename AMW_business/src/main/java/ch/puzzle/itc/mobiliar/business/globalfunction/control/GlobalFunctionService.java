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

package ch.puzzle.itc.mobiliar.business.globalfunction.control;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import ch.puzzle.itc.mobiliar.business.database.control.AmwAuditReader;
import ch.puzzle.itc.mobiliar.business.globalfunction.entity.GlobalFunctionEntity;

public class GlobalFunctionService {
    
    @Inject
    private GlobalFunctionRepository globalFunctionRepository;
    @Inject
    private AmwAuditReader amwAuditReader;

    /**
     * get Global Function from History at Date date
     */
    public List<GlobalFunctionEntity> getAllGlobalFunctionsAtDate(Date date){
        
        List<GlobalFunctionEntity> allGlobalFunctions = globalFunctionRepository.getAllGlobalFunctions();
        
        List<GlobalFunctionEntity> result = new ArrayList<>();
        for (GlobalFunctionEntity globalFunctionEntity : allGlobalFunctions) {
            GlobalFunctionEntity globalFunctionFromHistory = amwAuditReader
                      .getByDate(GlobalFunctionEntity.class, date, globalFunctionEntity.getId());
            result.add(globalFunctionFromHistory);
        }

        return result;
    }

    public List<GlobalFunctionEntity> getAllGlobalFunctions() {
        return globalFunctionRepository.getAllGlobalFunctions();
    }

    public boolean saveFunction(GlobalFunctionEntity gFunction) {
        return globalFunctionRepository.saveFunction(gFunction);
    }

    public void deleteFunction(GlobalFunctionEntity gFunction)  {
        globalFunctionRepository.deleteFunction(gFunction);
    }

    public boolean isExistingId(Integer id) {
        return globalFunctionRepository.isExistingId(id);
    }
    
}
