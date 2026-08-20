import { Component, computed, input } from '@angular/core';

/**
 * POC: generic Bootstrap "placeholder" skeleton (https://getbootstrap.com/docs/5.3/components/placeholders/)
 * Renders a configurable number of glowing placeholder bars, used as a stand-in for content that is still loading.
 */
@Component({
  selector: 'app-placeholder-rows',
  standalone: true,
  template: `
    <div class="placeholder-glow" aria-hidden="true">
      @for (w of widths(); track $index) {
        <span class="placeholder d-block mb-2" [class]="'col-' + w"></span>
      }
    </div>
  `,
})
export class PlaceholderRowsComponent {
  rows = input<number>(3);
  // cycled so rows don't all look the same width
  private readonly pattern = [12, 8, 10, 6, 9];
  protected widths = computed(() => Array.from({ length: this.rows() }, (_, i) => this.pattern[i % this.pattern.length]));
}
