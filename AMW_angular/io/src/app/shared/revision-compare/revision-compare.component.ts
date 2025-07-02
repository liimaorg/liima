import { Component, input, output } from '@angular/core';
import { RevisionInformation } from '../model/revisionInformation';
import { NgSelectModule } from '@ng-select/ng-select';
import { FormsModule } from '@angular/forms';
import { ButtonComponent } from '../button/button.component';
import { NgbDropdown, NgbDropdownItem, NgbDropdownMenu, NgbDropdownToggle } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-revision-compare',
  templateUrl: './revision-compare.component.html',
  styleUrl: './revision-compare.component.scss',
  standalone: true,
  imports: [NgSelectModule, FormsModule, ButtonComponent, NgbDropdown, NgbDropdownMenu, NgbDropdownItem, NgbDropdownToggle]
})
export class RevisionCompareComponent {
  selectedRevision = output<{ revisionId: number; displayName: string }>();
  revisions = input.required<RevisionInformation[]>();
  selectedRevisionName: string;

  selectRevision(revision: RevisionInformation) {
    if (!revision) return;
    this.selectedRevisionName = revision.displayName;
    this.selectedRevision.emit({ revisionId: revision.revision, displayName: revision.displayName });
  }

  resetRevision() {
    this.selectedRevisionName = null;
    this.selectedRevision.emit({ revisionId: null, displayName: null });
  }
}
