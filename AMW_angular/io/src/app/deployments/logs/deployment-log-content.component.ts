import { Component, Input } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Extension, RangeSetBuilder } from '@codemirror/state';
import { Decoration, DecorationSet, EditorView, ViewPlugin, ViewUpdate, WidgetType } from '@codemirror/view';
import { DeploymentLog } from './deployment-log';
import { CodeEditorComponent } from '../../shared/codemirror/code-editor.component';

type Failed = 'failed';

const errorRegex = /\b(error|failure|failed|fatal|not found)\b/i;
const warningRegex = /\bwarn\w*\b/i;
const urlRegex = /https?:\/\/[^\s<>"{}|\\^`\[\]]+/gi;

class UrlWidget extends WidgetType {
  constructor(readonly url: string) {
    super();
  }

  toDOM() {
    const link = document.createElement('a');
    link.href = this.url;
    link.textContent = this.url;
    link.target = '_blank';
    link.rel = 'noopener noreferrer';
    link.className = 'cm-log-url';
    return link;
  }
}

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

export const logUrlLinkExtension: Extension = [
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

            // Reset regex lastIndex for each line
            urlRegex.lastIndex = 0;
            let match;

            while ((match = urlRegex.exec(text)) !== null) {
              const url = match[0];
              const start = line.from + match.index;
              const end = start + url.length;

              builder.add(
                start,
                end,
                Decoration.replace({
                  widget: new UrlWidget(url),
                }),
              );
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
    '.cm-log-url': {
      color: 'var(--bs-link-color, #0d6efd)',
      textDecoration: 'underline',
      cursor: 'pointer',
      '&:hover': {
        color: 'var(--bs-link-hover-color, #0a58ca)',
      },
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
  readonly logHighlightExtensions: Extension[] = [logHighlightExtension, logUrlLinkExtension];
  @Input() content: DeploymentLog | Failed;
}
