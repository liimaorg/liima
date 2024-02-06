import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReleasesComponent } from './releases.component';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HttpClient } from '@angular/common/http';

describe('ReleasesComponent', () => {
  let component: ReleasesComponent;
  let fixture: ComponentFixture<ReleasesComponent>;
  let httpTestingController: HttpTestingController;
  let httpClient: HttpClient;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, ReleasesComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ReleasesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    httpTestingController = TestBed.inject(HttpTestingController);
    httpClient = TestBed.inject(HttpClient);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
