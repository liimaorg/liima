import { Component, computed, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { DomSanitizer } from '@angular/platform-browser';
import { toSignal } from '@angular/core/rxjs-interop';
import { catchError, of, shareReplay } from 'rxjs';

/**
 * Fetches the bootstrap-icons sprite once and inlines it, so <app-icon> can reference
 * it via a local "#name" fragment instead of an external file, avoiding one HTTP
 * request per rendered <use> element.
 */
@Component({
  selector: 'app-icon-sprite',
  standalone: true,
  template: `<div [innerHTML]="spriteHtml()" style="display: none" aria-hidden="true"></div>`,
})
export class IconSpriteComponent {
  private http = inject(HttpClient);
  private sanitizer = inject(DomSanitizer);

  private sprite$ = this.http.get('bootstrap-icons.svg', { responseType: 'text' }).pipe(
    catchError(() => of('')),
    shareReplay(1),
  );

  private rawSprite = toSignal(this.sprite$, { initialValue: '' });
  protected spriteHtml = computed(() => this.sanitizer.bypassSecurityTrustHtml(this.rawSprite()));
}
