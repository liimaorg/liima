import {Component} from '@angular/core';

@Component({
  selector: 'amw-tags',
  templateUrl: './tags.component.html',
  styleUrl: './tags.component.scss'
})
export class TagsComponent {
  tagName: string = '';
  tags: { id: number, name: string }[] = [];
  private tagId = 0;

  addTag(): void {
    if (this.tagName.trim().length > 0) {
      console.log('Tag added:', this.tagName);
      this.tags.push({ id: this.tagId++, name: this.tagName.trim() });
      console.log(this.tags);
      this.tagName = '';
    }
  }

  deleteTag(index: number): void {
    this.tags.splice(index, 1);
  }
}
