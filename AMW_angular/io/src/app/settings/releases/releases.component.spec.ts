import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReleasesComponent } from './releases.component';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ReleaseEditComponent } from './release-edit.component';
import { Release } from './release';

describe('ReleasesComponent', () => {
  let component: ReleasesComponent;
  let fixture: ComponentFixture<ReleasesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReleasesComponent],
      providers: [provideHttpClient(withInterceptorsFromDi()), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(ReleasesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should open release-edit modal and set date from existing release', () => {
    // Arrange
    const modalService = TestBed.inject(NgbModal);
    const openSpy = vi.spyOn(modalService, 'open');
    const testRelease: Release = {
      id: 123,
      name: 'Test Release',
      mainRelease: false,
      description: 'desc',
      installationInProductionAt: 1641600000, // 1.8. 2022 00:00:00 UTC

      default: false,
      v: 1,
    };
    // Patch results() to return the test release
    vi.spyOn(component, 'results').mockReturnValue([testRelease]);

    // Act
    component.editRelease(testRelease.id);

    // Assert
    expect(openSpy).toHaveBeenCalledWith(ReleaseEditComponent);
    // The modalRef.componentInstance.release should be set to testRelease
    const modalRef = openSpy.mock.results[openSpy.mock.results.length - 1].value;
    expect(modalRef.componentInstance.release).toEqual(testRelease);

    // The installationDate should be set from installationInProductionAt
    modalRef.componentInstance.ngOnInit();
    expect(modalRef.componentInstance.installationDate.toEpoch()).toEqual(testRelease.installationInProductionAt);
  });
});
