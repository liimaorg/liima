import { EditorView } from '@codemirror/view';
import { logHighlightExtension, logUrlLinkExtension } from './deployment-log-content.component';

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

describe('logUrlLinkExtension', () => {
  const buildView = (doc: string) => {
    const parent = document.createElement('div');
    const view = new EditorView({ doc, extensions: [logUrlLinkExtension], parent });
    return { view, parent };
  };

  it('should detect and style HTTP URLs', () => {
    const { view, parent } = buildView('Visit http://example.com for more info');

    const urlElements = parent.querySelectorAll('.cm-log-url');
    expect(urlElements.length).toBe(1);
    expect(urlElements[0].textContent).toBe('http://example.com');

    view.destroy();
  });

  it('should detect and style HTTPS URLs', () => {
    const { view, parent } = buildView('Visit https://example.com for more info');

    const urlElements = parent.querySelectorAll('.cm-log-url');
    expect(urlElements.length).toBe(1);
    expect(urlElements[0].textContent).toBe('https://example.com');

    view.destroy();
  });

  it('should detect multiple URLs on the same line', () => {
    const { view, parent } = buildView('Check http://example.com and https://github.com');

    const urlElements = parent.querySelectorAll('.cm-log-url');
    expect(urlElements.length).toBe(2);
    expect(urlElements[0].textContent).toBe('http://example.com');
    expect(urlElements[1].textContent).toBe('https://github.com');

    view.destroy();
  });

  it('should detect URLs on multiple lines', () => {
    const { view, parent } = buildView('Line 1: http://example.com\nLine 2: https://github.com');

    const urlElements = parent.querySelectorAll('.cm-log-url');
    expect(urlElements.length).toBe(2);

    view.destroy();
  });

  it('should handle URLs with paths and query parameters', () => {
    const { view, parent } = buildView('API: https://api.example.com/v1/data?key=value&id=123');

    const urlElements = parent.querySelectorAll('.cm-log-url');
    expect(urlElements.length).toBe(1);
    expect(urlElements[0].textContent).toBe('https://api.example.com/v1/data?key=value&id=123');

    view.destroy();
  });

  it('should not detect plain text', () => {
    const { view, parent } = buildView('This is just plain text without any links');

    const urlElements = parent.querySelectorAll('.cm-log-url');
    expect(urlElements.length).toBe(0);

    view.destroy();
  });

  it('should open URL in new tab when clicked', () => {
    const { view, parent } = buildView('Visit https://example.com');

    const urlElement = parent.querySelector('.cm-log-url') as HTMLAnchorElement;
    expect(urlElement).toBeTruthy();
    expect(urlElement.tagName).toBe('A');
    expect(urlElement.href).toBe('https://example.com/');
    expect(urlElement.target).toBe('_blank');
    expect(urlElement.rel).toBe('noopener noreferrer');

    view.destroy();
  });

  it('should set title attribute with URL', () => {
    const { view, parent } = buildView('Visit https://example.com');

    const urlElement = parent.querySelector('.cm-log-url') as HTMLAnchorElement;
    expect(urlElement?.textContent).toBe('https://example.com');

    view.destroy();
  });
});
