import { Component, input, output, signal, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { IconComponent } from '../icon/icon.component';
import { PropertyTag } from '../../settings/property-types/property-tag';

@Component({
  selector: 'app-tag-input',
  imports: [CommonModule, IconComponent],
  templateUrl: './tag-input.component.html',
  styleUrl: './tag-input.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TagInputComponent {
  tags = input.required<PropertyTag[]>();
  canEdit = input<boolean>(true);
  placeholder = input<string>('Type a tag and press Enter');
  tagType = input<string>('LOCAL');

  tagsChange = output<PropertyTag[]>();

  newTagInput = signal<string>('');

  addTag() {
    const tagName = this.newTagInput().trim();
    if (tagName && !this.tags().some((t) => t.name === tagName)) {
      const updatedTags: PropertyTag[] = [...this.tags(), { name: tagName, type: this.tagType() }];
      this.tagsChange.emit(updatedTags);
      this.newTagInput.set('');
    }
  }

  removeTag(tagName: string) {
    const updatedTags = this.tags().filter((t) => t.name !== tagName);
    this.tagsChange.emit(updatedTags);
  }

  onTagInputKeydown(event: KeyboardEvent) {
    if (event.key === 'Enter') {
      event.preventDefault();
      this.addTag();
    }
  }
}
