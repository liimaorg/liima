package ch.puzzle.itc.mobiliar.business.resourcegroup.control;

import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.CopyFromResourceCommand;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.CopyFromResourceUseCase;
import ch.puzzle.itc.mobiliar.business.resourcegroup.boundary.CopyResource;
import ch.puzzle.itc.mobiliar.common.exception.AMWException;

import javax.inject.Inject;

public class CopyFromResourceService implements CopyFromResourceUseCase {

    @Inject
    private CopyResource copyResource;

    @Override
    public void copyFromResource(CopyFromResourceCommand command) {
        try {
            CopyResourceResult copyResourceResult = copyResource.doCopyResource(command.getTargetResourceId(), command.getOriginResourceId());
            if (copyResourceResult.isSuccess()) return;
        } catch (AMWException e) {
            throw new IllegalStateException(e.getMessage());
        }
        throw new IllegalStateException("Copy from resource failed");
    }
}
