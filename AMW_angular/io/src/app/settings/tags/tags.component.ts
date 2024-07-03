import { Component, OnDestroy, OnInit } from '@angular/core';
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
  tags: Tag[] = [];
  canCreate = false;
  canDelete = false;
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
        this.tags = data;
      },
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next(undefined);
  }

  private getUserPermissions() {
    this.authService
      .getActionsForPermission('MANAGE_GLOBAL_TAGS')
      .pipe(takeUntil(this.destroy$))
      .subscribe((value) => {
        if (value.indexOf('ALL') > -1) {
          this.canCreate = this.canDelete = true;
        } else {
          this.canCreate = value.indexOf('CREATE') > -1;
          this.canDelete = value.indexOf('DELETE') > -1;
        }
      });
  }

  addTag(): void {
    if (this.tagName.trim().length > 0) {
      this.http.post<Tag>('/AMW_rest/resources/settings/tags', { name: this.tagName }).subscribe((newTag) => {
        this.toastService.success('Tag added.');
        this.tags.push(newTag);
        this.tagName = '';
      });
    }
  }

  deleteTag(tagId: number): void {
    this.http.delete<Tag>(`/AMW_rest/resources/settings/tags/${tagId}`).subscribe(() => {
      this.tags = this.tags.filter((tag) => tag.id !== tagId);
      this.toastService.success('Tag deleted.');
    });
  }
}
