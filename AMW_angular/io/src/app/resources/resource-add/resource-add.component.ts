import { ChangeDetectionStrategy, Component, EventEmitter, inject, Input, Output, signal } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { FormsModule } from '@angular/forms';
import { ModalHeaderComponent } from '../../shared/modal-header/modal-header.component';
import { ButtonComponent } from '../../shared/button/button.component';
import { ResourceType } from '../models/resource-type';
import { Release } from '../../settings/releases/release';
import { NgSelectModule } from '@ng-select/ng-select';

@Component({
  selector: 'app-resource-add',
  imports: [FormsModule, ModalHeaderComponent, ButtonComponent, NgSelectModule],
  templateUrl: './resource-add.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ResourceAddComponent {
  activeModal = inject(NgbActiveModal);

  private readonly resourceTypeSignal = signal<ResourceType | null>(null);
  private readonly releasesSignal = signal<Release[]>([]);
  private readonly selectedReleaseNameSignal = signal<string>('');

  // ng-bootstrap modal inputs are assigned through componentInstance; keep setter-backed signals until
  // https://github.com/ng-bootstrap/ng-bootstrap/issues/4664 is resolved.
  @Input({ required: true })
  set resourceType(value: ResourceType) {
    this.resourceTypeSignal.set(value);
  }

  get resourceType(): ResourceType {
    return this.resourceTypeSignal() as ResourceType;
  }

  @Input({ required: true })
  set releases(value: Release[]) {
    this.releasesSignal.set(value);
  }

  get releases(): Release[] {
    return this.releasesSignal();
  }

  @Input({ required: true })
  set selectedReleaseName(value: string) {
    this.selectedReleaseNameSignal.set(value);
  }

  get selectedReleaseName(): string {
    return this.selectedReleaseNameSignal();
  }
  resourceName!: string;
  @Output() saveResource: EventEmitter<any> = new EventEmitter<any>();

  getTitle() {
    if (!this.resourceType) return;
    return this.resourceType.name ? `Create new instance for ${this.resourceType.name}` : `Create new instance`;
  }

  save() {
    if (this.isValidForm()) {
      const resourceToAdd = {
        name: this.resourceName,
        type: this.resourceType.name,
        releaseName: this.selectedReleaseName,
      };
      this.saveResource.emit(resourceToAdd);
      this.activeModal.close();
    }
  }

  cancel() {
    this.activeModal.close();
  }

  setSelectedRelease($event: any) {
    this.selectedReleaseName = $event;
  }

  isValidForm() {
    const REGEXP_ALPHANUMERIC_WITH_UNDERSCORE_HYPHEN = /^[a-zA-Z0-9_-]+$/;
    return this.resourceName ? REGEXP_ALPHANUMERIC_WITH_UNDERSCORE_HYPHEN.test(this.resourceName) : false;
  }
}
