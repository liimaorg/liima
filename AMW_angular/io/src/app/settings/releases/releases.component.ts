import { Component, OnInit } from '@angular/core';
import { AsyncPipe, DatePipe, NgFor, NgIf } from '@angular/common';
import { LoadingIndicatorComponent } from '../../shared/elements/loading-indicator.component';
import { BehaviorSubject, Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { IconComponent } from '../../shared/icon/icon.component';
import { PaginationComponent } from '../../shared/pagination/pagination.component';
import { DATE_FORMAT } from '../../core/amw-constants';

type Release = {
  name: string;
  mainRelease: string;
  description: string;
  installationInProductionAt: string;
  id: string;
};

@Component({
  selector: 'amw-releases',
  standalone: true,
  imports: [AsyncPipe, DatePipe, IconComponent, LoadingIndicatorComponent, NgIf, NgFor, PaginationComponent],
  templateUrl: './releases.component.html',
})
export class ReleasesComponent implements OnInit {
  releases$: Observable<Release[]>;
  isLoading$: Observable<boolean> = new BehaviorSubject<boolean>(true);

  dateFormat = DATE_FORMAT;

  // pagination with default values
  maxResults: number = 10;
  offset: number = 0;
  allResults: number;
  currentPage: number;
  lastPage: number;

  isLoading: boolean = true;

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.getReleases();
  }

  private getReleases() {
    this.isLoading = true;
    this.releases$ = this.http.get<Release[]>(
      '/AMW_rest/resources/releases?start=' + this.offset + '&limit=' + this.maxResults,
    );
    this.releases$.subscribe({
      next: (r) => {
        this.allResults = r.length;
        this.currentPage = Math.floor(this.offset / this.maxResults) + 1;
        this.lastPage = Math.ceil(this.allResults / this.maxResults);
      },
      complete: () => {
        this.isLoading = false;
      },
    });
  }

  addRelease() {}

  editRelease(id: string) {}

  deleteRelease(id: string) {}

  setMaxResultsPerPage(max: number) {
    this.maxResults = max;
    this.offset = 0;
    this.getReleases();
  }

  setNewOffset(offset: number) {
    this.offset = offset;
    this.getReleases();
  }
}
