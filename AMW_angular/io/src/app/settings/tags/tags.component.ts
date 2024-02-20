import { Component, OnInit, ViewChild } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ToastComponent } from 'src/app/shared/elements/toast/toast.component';
import { ToastComponent as ToastComponent_1 } from '../../shared/elements/toast/toast.component';
import { FormsModule } from '@angular/forms';
import { IconComponent } from '../../shared/icon/icon.component';
import { Subject } from 'rxjs';
import { AuthService } from '../../auth/auth.service';
import { takeUntil } from 'rxjs/operators';

type Tag = { id: number; name: string };

@Component({
  selector: 'amw-tags',
  templateUrl: './tags.component.html',
  styleUrl: './tags.component.scss',
  standalone: true,
  imports: [FormsModule, ToastComponent_1, IconComponent],
})
export class TagsComponent implements OnInit {
  tagName = '';
  tags: Tag[] = [];
  canCreate: boolean = false;
  canDelete: boolean = false;
  private destroy$ = new Subject<void>();

  @ViewChild(ToastComponent) toast: ToastComponent;

  constructor(
    private http: HttpClient,
    private authService: AuthService,
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
      this.http.post<Tag>('/AMW_rest/resources/settings/tags', { name: this.tagName }).subscribe({
        next: (newTag) => {
          this.tags.push(newTag);
          this.toast.display('Tag added.');
          this.tagName = '';
        },
        error: (error) => {
          this.toast.display(error.error.message, 'error', 5000);
        },
      });
    }
  }

  deleteTag(tagId: number): void {
    this.http.delete<Tag>(`/AMW_rest/resources/settings/tags/${tagId}`).subscribe({
      next: (response) => {
        this.tags = this.tags.filter((tag) => tag.id !== tagId);
        this.toast.display('Tag deleted.');
      },
      error: (error) => {
        this.toast.display(error.error.message, 'error', 5000);
      },
    });
  }
}
