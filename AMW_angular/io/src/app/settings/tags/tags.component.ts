import { Component, OnDestroy, OnInit, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { IconComponent } from '../../shared/icon/icon.component';
import { Subject } from 'rxjs';
import { AuthService } from '../../auth/auth.service';
import { takeUntil } from 'rxjs/operators';
import { ToastService } from '../../shared/elements/toast/toast.service';

type Tag = { id: number; name: string };

@Component({
  selector: 'app-tags',
  templateUrl: './tags.component.html',
  styleUrl: './tags.component.scss',
  standalone: true,
  imports: [FormsModule, IconComponent],
})
export class TagsComponent implements OnInit, OnDestroy {
  tagName = '';
  tags = signal<Tag[]>([]);
  canCreate = signal<boolean>(false);
  canDelete = signal<boolean>(false);
  private destroy$ = new Subject<void>();

  constructor(
    private http: HttpClient,
    private authService: AuthService,
    private toastService: ToastService,
  ) {}

  ngOnInit(): void {
    this.getUserPermissions();
    this.http.get<Tag[]>('/AMW_rest/resources/settings/tags').subscribe({
      next: (data) => {
        this.tags.set(data);
      },
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next(undefined);
  }

  private getUserPermissions() {
    if (this.authService.isLoaded()) {
      this.authService.getActionsForPermission('MANAGE_GLOBAL_TAGS').map((action) => {
        if (action.indexOf('ALL') > -1) {
          this.canCreate.set(true);
          this.canDelete.set(true);
        } else {
          this.canCreate.set(action.indexOf('CREATE') > -1);
          this.canDelete.set(action.indexOf('DELETE') > -1);
        }
      });
    }
  }

  addTag(): void {
    if (this.tagName.trim().length > 0) {
      this.http.post<Tag>('/AMW_rest/resources/settings/tags', { name: this.tagName }).subscribe((newTag) => {
        this.toastService.success('Tag added.');
        this.tags.update((prevTags) => [...prevTags, newTag]);
        this.tagName = '';
      });
    }
  }

  deleteTag(tagId: number): void {
    this.http.delete<Tag>(`/AMW_rest/resources/settings/tags/${tagId}`).subscribe(() => {
      this.tags.update((prevTags) => prevTags.filter((tag) => tag.id !== tagId));
      this.toastService.success('Tag deleted.');
    });
  }
}
