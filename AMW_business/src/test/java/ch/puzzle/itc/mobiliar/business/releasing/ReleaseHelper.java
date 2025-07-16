package ch.puzzle.itc.mobiliar.business.releasing;

import java.util.Date;

import ch.puzzle.itc.mobiliar.business.releasing.entity.ReleaseEntity;

public class ReleaseHelper {
    public static ReleaseEntity createRL(String name, Date date) {
		ReleaseEntity releaseEntity = new ReleaseEntity();
		releaseEntity.setName(name);
        if (date == null) {
            releaseEntity.setInstallationInProductionAt(new Date());
        } else {
            releaseEntity.setInstallationInProductionAt(date);
        }
		return releaseEntity;
	}
}
