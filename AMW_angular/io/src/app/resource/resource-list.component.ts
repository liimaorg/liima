import { Component, OnInit } from '@angular/core';
import { Resource } from './resource';
import { ResourceService } from './resource.service';

import { Location } from '@angular/common';
import { ActivatedRoute, Router } from "@angular/router";
import {type} from "os";

@Component({
  selector: 'amw-resource-list',
  templateUrl: './resource-list.component.html',
  providers: [ResourceService]
})
export class ResourceListComponent implements OnInit {
  resource: Resource = null;
  resources: Resource[] = [];
  resourceInRelease: Resource = null;
  resourceName: string = '';
  releaseName: string = '';
  resourceType: string = '';
  titleLabel: string = '';
  errorMessage: string = '';
  isLoading: boolean = false;

  constructor(private resourceService: ResourceService,
              private activatedRoute: ActivatedRoute,
              private router: Router,
              private location: Location) {
  }

  ngOnInit(): void {

    this.activatedRoute.params.subscribe(
      (param: any) => {
        this.resourceName = param['resource'];
        this.releaseName = param['release'];
        this.resourceType = param['type'];
        console.log('resourceName: '+this.resourceName);
        console.log('releaseName: '+this.releaseName);
        console.log('resourceType: '+this.resourceType);
      });

    // this.activatedRoute.queryParams.subscribe(
    //   (param: any) => {
    //     this.resourceType = param['type'];
    //     console.log('resourceType: '+this.resourceType);
    //   });

    if (this.releaseName && this.resourceName) {
      this.getInRelease();
      //this.resourceService.getInRelease(this.resourceName, this.releaseName).subscribe(r => this.resourceInRelease = r);
    } else if (this.resourceName) {
      this.getResourceGroup();
      //this.resourceService.get(this.resourceName).subscribe(r => this.resource = r);
    } else if (this.resourceType) {
      this.byType(this.resourceType);
    } else {
      this.getAllResources();
    }

    console.log('hello `ResourceList` component');
  }

  getAllResources() {
    console.log('getAllResources()');
    this.isLoading = true;
    this.resource = null;
    this.resourceInRelease = null;
    this.titleLabel = 'Alle';
    this.resourceService
      .getAll()
      .subscribe(
        /* happy path */ r => this.resources = r,
        /* error path */ e => this.errorMessage = e,
        /* onComplete */ () => this.isLoading = false);
  }

  byType(type: string) {
    console.log('byType()');
    this.isLoading = true;
    this.resource = null;
    this.resourceInRelease = null;
    this.titleLabel = 'Alle vom Typ '+type;
    this.resourceService
      .getByType(type)
      .subscribe(
        /* happy path */ r => this.resources = r,
        /* error path */ e => this.errorMessage = e,
        /* onComplete */ () => this.isLoading = false);
  }

  getResourceGroup() {
    console.log('getResourceGroup()');
    this.isLoading = true;
    this.resources = [];
    this.resourceInRelease = null;
    this.titleLabel = 'Gruppe '+this.resourceName;
    this.resourceService
      .get(this.resourceName)
      .subscribe(
        /* happy path */ r => this.resource = r,
        /* error path */ e => this.errorMessage = e,
        /* onComplete */ () => this.isLoading = false);
    this.router.navigate(['/resource', this.resourceName]);
  }

  getInRelease() {
    console.log('getInRelease()');
    this.isLoading = true;
    this.resource = null;
    this.titleLabel = 'Gruppe ' +this.resourceName+ ' in Release ' +this.releaseName;
    this.resourceService
      .getInRelease(this.resourceName, this.releaseName)
      .subscribe(
        /* happy path */ r => this.resourceInRelease = r,
        /* error path */ e => this.errorMessage = e,
        /* onComplete */ () => this.isLoading = false);
  }

  goBack(): void {
    this.location.back();
  }
}
