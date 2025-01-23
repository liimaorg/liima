import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TableHeader, TableComponent } from './table.component';
import { ComponentRef } from '@angular/core';

describe('TableComponent', () => {
  let component: TableComponent;
  let componentRef: ComponentRef<TableComponent>;
  let fixture: ComponentFixture<TableComponent>;

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
    componentRef.setInput('headers', [header]);
    componentRef.setInput('data', []);
    componentRef.setInput('hasAction', false);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
