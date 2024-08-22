import { Component, inject, OnDestroy, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { IconComponent } from '../../shared/icon/icon.component';
import { Subject } from 'rxjs';
import { AuthService } from '../../auth/auth.service';
import { ToastService } from '../../shared/elements/toast/toast.service';
import { TagsService } from './tags.service';

@Component({
  selector: 'app-tags',
  templateUrl: './tags.component.html',
  styleUrl: './tags.component.scss',
  standalone: true,
  imports: [FormsModule, IconComponent],
})
export class TagsComponent implements OnInit, OnDestroy {
  private tagsService = inject(TagsService);
  private authService = inject(AuthService);
  private toastService = inject(ToastService);

  pageTitle = 'Tags';
  tagName = signal('');
  tags = this.tagsService.tags;
  canCreate = signal<boolean>(false);
  canDelete = signal<boolean>(false);
  private destroy$ = new Subject<void>();

  ngOnInit(): void {
    this.getUserPermissions();
  }

  ngOnDestroy(): void {
    this.destroy$.next(undefined);
  }

  private getUserPermissions() {
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

  addTag(): void {
    if (this.tagName().trim().length > 0) {
      this.tagsService.add(this.tagName());
      this.toastService.success('Tag added.');
      this.tagName.set('');
    }
  }

  deleteTag(tagId: number): void {
    this.tagsService.delete(tagId);
    this.toastService.success('Tag deleted.');
  }
}
