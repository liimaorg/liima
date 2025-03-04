import { Component, input, output } from '@angular/core';
import { ButtonComponent } from '../button/button.component';
import { RevisionInformation } from '../model/revisionInformation';
import { NgSelectModule } from '@ng-select/ng-select';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-revision-compare',
  templateUrl: './revision-compare.component.html',
  styleUrl: './revision-compare.component.scss',
  standalone: true,
  imports: [ButtonComponent, NgSelectModule, FormsModule],
})
export class RevisionCompareComponent {
  selectedRevision = output<{ revisionId: number; displayName: string }>();
  revisions = input.required<RevisionInformation[]>();
  selection: RevisionInformation;

  selectRevision(revision: RevisionInformation) {
    if (!revision) return;
    this.selectedRevision.emit({ revisionId: revision.revision, displayName: revision.displayName });
  }

  resetRevision() {
    this.selection = null;
    this.selectedRevision.emit({ revisionId: null, displayName: null });
  }
}
