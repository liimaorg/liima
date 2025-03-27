import { Component, input, output } from '@angular/core';
import { RevisionInformation } from '../model/revisionInformation';
import { NgSelectModule } from '@ng-select/ng-select';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-revision-compare',
  templateUrl: './revision-compare.component.html',
  styleUrl: './revision-compare.component.scss',
  standalone: true,
  imports: [NgSelectModule, FormsModule],
})
export class RevisionCompareComponent {
  selectedRevision = output<{ revisionId: number; displayName: string }>();
  revisions = input.required<RevisionInformation[]>();

  selectRevision(revision: RevisionInformation) {
    if (!revision) return;
    this.selectedRevision.emit({ revisionId: revision.revision, displayName: revision.displayName });
  }

  resetRevision() {
    this.selectedRevision.emit({ revisionId: null, displayName: null });
  }
}
