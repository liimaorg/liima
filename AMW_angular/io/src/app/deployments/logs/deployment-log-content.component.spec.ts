import { EditorView } from '@codemirror/view';
import { logHighlightExtension } from './deployment-log-content.component';

describe('logHighlightExtension', () => {
  const buildView = (doc: string) => {
    const parent = document.createElement('div');
    const view = new EditorView({ doc, extensions: [logHighlightExtension], parent });
    return { view, parent };
  };

  it('highlights error lines', () => {
    const { view, parent } = buildView('info\nfatal server error\nall good');

    const errorLines = parent.querySelectorAll('.cm-log-error');
    const warningLines = parent.querySelectorAll('.cm-log-warning');

    expect(errorLines.length).toBe(1);
    expect(warningLines.length).toBe(0);

    view.destroy();
  });

  it('highlights warning lines', () => {
    const { view, parent } = buildView('info\nwarning: disk space low\nother info');

    const errorLines = parent.querySelectorAll('.cm-log-error');
    const warningLines = parent.querySelectorAll('.cm-log-warning');

    expect(errorLines.length).toBe(0);
    expect(warningLines.length).toBe(1);

    view.destroy();
  });
});
