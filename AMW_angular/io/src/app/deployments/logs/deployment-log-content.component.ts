import { Component, Input } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Extension, RangeSetBuilder } from '@codemirror/state';
import { Decoration, DecorationSet, EditorView, ViewPlugin, ViewUpdate } from '@codemirror/view';
import { DeploymentLog } from './deployment-log';
import { CodeEditorComponent } from '../../shared/codemirror/code-editor.component';

type Failed = 'failed';

const errorRegex = /\b(error|failure|failed|fatal|not found)\b/i;
const warningRegex = /\bwarn\w*\b/i;

export const logHighlightExtension: Extension = [
  ViewPlugin.fromClass(
    class {
      decorations: DecorationSet;

      constructor(view: EditorView) {
        this.decorations = this.buildDecorations(view);
      }

      update(update: ViewUpdate) {
        if (update.docChanged || update.viewportChanged) {
          this.decorations = this.buildDecorations(update.view);
        }
      }

      private buildDecorations(view: EditorView): DecorationSet {
        const builder = new RangeSetBuilder<Decoration>();

        for (const { from, to } of view.visibleRanges) {
          let position = from;

          while (position <= to) {
            const line = view.state.doc.lineAt(position);
            const text = line.text;

            if (errorRegex.test(text)) {
              builder.add(line.from, line.from, Decoration.line({ class: 'cm-log-error' }));
            } else if (warningRegex.test(text)) {
              builder.add(line.from, line.from, Decoration.line({ class: 'cm-log-warning' }));
            }

            if (line.to + 1 > view.state.doc.length) {
              break;
            }

            position = line.to + 1;
          }
        }

        return builder.finish();
      }
    },
    { decorations: (v) => v.decorations },
  ),
  EditorView.baseTheme({
    '.cm-log-error': {
      color: 'var(--bs-danger, #dc3545)',
      fontWeight: 600,
    },
    '.cm-log-warning': {
      color: 'var(--bs-warning, #ffc107)',
      fontWeight: 600,
    },
  }),
];

@Component({
  selector: 'app-deployment-log-content',
  template: `
    <div class="m-2 h-100">
      @if (content !== null && content !== 'failed') {
        <div class="editor border border-primary-subtle rounded">
          <app-code-editor
            [theme]="'light'"
            [setup]="'minimal'"
            [readonly]="true"
            [extensions]="logHighlightExtensions"
            [(ngModel)]="content.content"
          ></app-code-editor>
        </div>
      } @else {
        ...
      }
    </div>
  `,
  imports: [FormsModule, CodeEditorComponent],
})
export class DeploymentLogContentComponent {
  readonly logHighlightExtensions: Extension[] = [logHighlightExtension];
  @Input() content: DeploymentLog | Failed;
}
