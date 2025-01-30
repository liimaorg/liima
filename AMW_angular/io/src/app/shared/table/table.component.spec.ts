import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TableHeader, TableComponent } from './table.component';
import { ComponentRef } from '@angular/core';

describe('TableComponent', () => {
  let component: TableComponent<any>;
  let componentRef: ComponentRef<TableComponent<any>>;
  let fixture: ComponentFixture<TableComponent<any>>;

  const header: TableHeader = {
    key: 'name',
    title: 'Name',
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TableComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(TableComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('entityName', 'name');
    componentRef.setInput('headers', [header]);
    componentRef.setInput('data', []);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
