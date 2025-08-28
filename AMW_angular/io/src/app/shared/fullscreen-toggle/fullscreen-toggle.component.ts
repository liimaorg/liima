import { Component, output } from '@angular/core';
import { ButtonComponent } from '../button/button.component';
import { IconComponent } from '../icon/icon.component';

@Component({
  selector: 'app-fullscreen-toggle',
  templateUrl: './fullscreen-toggle.component.html',
  standalone: true,
  imports: [ButtonComponent, IconComponent],
})
export class FullscreenToggleComponent {
  fullscreenChange = output<boolean>();
  public isFullscreen = false;
  public toggleFullscreenIcon = 'arrows-fullscreen';

  toggleFullscreen() {
    this.isFullscreen = !this.isFullscreen;
    this.toggleFullscreenIcon = this.isFullscreen ? 'fullscreen-exit' : 'arrows-fullscreen';
    this.fullscreenChange.emit(this.isFullscreen);
  }
}
