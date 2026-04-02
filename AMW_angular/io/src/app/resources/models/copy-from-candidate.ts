export interface CopyFromRelease {
  releaseId: number;
  releaseName: string;
  resourceId: number;
}

export interface CopyFromCandidate {
  groupId: number;
  groupName: string;
  releases: CopyFromRelease[];
}
