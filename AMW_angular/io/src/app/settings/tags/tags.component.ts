import { Component, ViewChild } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ToastComponent } from 'src/app/shared/elements/toast/toast.component';

type Tag = { id: number; name: string };

@Component({
  selector: 'amw-tags',
  templateUrl: './tags.component.html',
  styleUrl: './tags.component.scss',
})
export class TagsComponent {
  tagName: string = '';
  tags: Tag[] = [];

  @ViewChild(ToastComponent) toast: ToastComponent;

  constructor(private http: HttpClient) {
    http.get<Tag[]>('AMW_rest/resources/settings/tags').subscribe({
      next: (data) => {
        this.tags = data;
      },
    });
  }

  addTag(): void {
    if (this.tagName.trim().length > 0) {
      this.http.post<Tag>('AMW_rest/resources/settings/tags', { name: this.tagName }).subscribe({
        next: (newTag) => {
          this.tags.push(newTag);
          this.toast.display('Tag added.');
        },
        error: (error) => {
          console.log(error);
          this.toast.display('Failed to add tag.', 'error');
        },
      });
    }
  }

  deleteTag(tagId: number): void {
    this.http.delete<Tag>(`AMW_rest/resources/settings/tags/${tagId}`).subscribe({
      next: (response) => {
        this.tags = this.tags.filter((tag) => tag.id !== tagId);
        this.toast.display('Tag deleted.');
      },
      error: (error) => {
        console.log(error);
        this.toast.display('Failed to delete tag.', 'error');
      },
    });
  }
}
