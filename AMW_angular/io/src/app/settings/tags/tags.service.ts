import { inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Tag } from './tag';
import { toSignal } from '@angular/core/rxjs-interop';
import { tap } from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class TagsService {
  private http = inject(HttpClient);
  tagsUrl = '/AMW_rest/resources/settings/tags';

  tags = signal<Tag[]>([]);
  private tags$ = this.http.get<Tag[]>(this.tagsUrl).pipe(tap((tags) => this.tags.set(tags)));
  // used to automatically un-/subscribe to observable
  readOnlyTags = toSignal(this.tags$, { initialValue: [] as Tag[] });

  add(tagName: string) {
    this.http.post<Tag>(this.tagsUrl, { name: tagName }).subscribe((newTag) => {
      this.tags.update((tags) => [...tags, newTag]);
    });
  }

  delete(tagId: number) {
    this.http.delete<Tag>(this.tagsUrl + `/${tagId}`).subscribe(() => {
      this.tags.update((tags) => tags.filter((tag) => tag.id !== tagId));
    });
  }
}
