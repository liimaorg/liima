import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RelationGroupComponent, RelationGroupItem } from './relation-group.component';

describe('RelationGroupComponent', () => {
  let fixture: ComponentFixture<RelationGroupComponent>;
  let element: HTMLElement;
  const items: RelationGroupItem[] = [
    { key: 1, name: 'Service A', type: 'Webservice' },
    { key: 2, name: 'Database A', type: 'Database' },
    { key: 3, name: 'Service B', type: 'Webservice', identifier: 'secondary', unresolved: true },
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [RelationGroupComponent] }).compileComponents();
    fixture = TestBed.createComponent(RelationGroupComponent);
    element = fixture.nativeElement;
    fixture.componentRef.setInput('title', 'Consumed Resources');
    fixture.componentRef.setInput('items', items);
    fixture.detectChanges();
  });

  it('groups non-adjacent resources under one heading per type in first-seen order', () => {
    expect(Array.from(element.querySelectorAll('.relations-list > *'), (node) => node.textContent.trim())).toEqual([
      'Webservice',
      'Service A',
      'Service B (secondary)',
      'Database',
      'Database A',
    ]);
  });

  it('preserves selection, unresolved styling and mouse and keyboard interaction', () => {
    fixture.componentRef.setInput('selectedKey', 3);
    fixture.detectChanges();
    const selected: RelationGroupItem[] = [];
    fixture.componentInstance.itemSelected.subscribe((item) => selected.push(item));
    const rows = element.querySelectorAll<HTMLElement>('.relation-item');

    expect(rows[1].classList.contains('relation-item--selected')).toBe(true);
    expect(rows[1].classList.contains('relation-item--unresolved')).toBe(true);
    rows[0].click();
    rows[1].dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter' }));

    expect(selected).toEqual([items[0], items[2]]);
  });

  it('updates groups when resources change and hides empty sections', () => {
    fixture.componentRef.setInput('items', [items[1]]);
    fixture.detectChanges();
    expect(element.querySelectorAll('.relation-type-heading')).toHaveLength(1);
    expect(element.querySelector('.relation-type-heading')?.textContent).toBe('Database');

    fixture.componentRef.setInput('items', []);
    fixture.detectChanges();
    expect(element.querySelector('.relations-section')).toBeNull();
  });
});
