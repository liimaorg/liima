import { Component } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';

type Tag = { id: number; name: string };

@Component({
  selector: 'amw-tags',
  templateUrl: './tags.component.html',
  styleUrl: './tags.component.scss',
  standalone: true,
  imports: [FormsModule],
})
export class TagsComponent {
  tagName: string = '';
  tags: Tag[] = [];
  private tagId = 0;

  constructor(private http: HttpClient) {
    http.get<Tag[]>('AMW_rest/resources/settings/tags').subscribe((data) => {
      this.tags = data;
    });
  }

  addTag(): void {
    if (this.tagName.trim().length > 0) {
      this.tags.push({ id: this.tagId++, name: this.tagName.trim() });
      this.tagName = '';
    }
  }

  deleteTag(tagId: number): void {
    this.tags = this.tags.filter((tag) => tag.id !== tagId);
  }
}
