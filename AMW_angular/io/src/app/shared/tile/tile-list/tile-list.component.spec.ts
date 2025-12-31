import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TileListComponent } from './tile-list.component';
import { ComponentRef } from '@angular/core';

describe('TileListComponent', () => {
  let component: TileListComponent;
  let componentRef: ComponentRef<TileListComponent>;
  let fixture: ComponentFixture<TileListComponent>;

  const tileListData = [
    { name: 'startJob_0.sh', description: 'startJob_0.sh', id: 0 },
    { name: 'startJob_1.sh', description: 'job 2 again', id: 1 },
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TileListComponent],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TileListComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('title', 'my title');
    componentRef.setInput('data', tileListData);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
