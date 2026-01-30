import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TileComponent } from './tile.component';
import { ComponentRef } from '@angular/core';

describe('TileComponent', () => {
  let component: TileComponent;
  let componentRef: ComponentRef<TileComponent>;
  let fixture: ComponentFixture<TileComponent>;

  const tileData = [
    {
      title: 'Instance Templates',
      entries: [
        { name: 'startJob_0.sh', description: 'startJob_0.sh', id: 0 },
        { name: 'startJob_1.sh', description: 'job 2 again', id: 1 },
      ],
      canEdit: true,
      canDelete: true,
    },
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TileComponent],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TileComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
    componentRef.setInput('title', 'my title');
    componentRef.setInput('actionName', 'myAction');
    componentRef.setInput('canAction', true);
    componentRef.setInput('noContent', false);
    componentRef.setInput('notAllowed', false);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
