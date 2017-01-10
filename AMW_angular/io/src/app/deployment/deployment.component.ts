import { Component, OnInit } from '@angular/core';
import { Resource } from '../resource/resource';
import { Release } from '../resource/release';
import { Relation } from '../resource/relation';
import { ResourceService } from '../resource/resource.service';
import { DeploymentService } from './deployment.service';
import { EnvironmentService } from './environment.service';
import { Location } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { Environment } from './environment';
import { Deployment } from './deployment';
import * as _ from 'lodash';

@Component({
  selector: 'amw-deployment',
  templateUrl: './deployment.component.html',
  providers: [DeploymentService]
})
export class DeploymentComponent implements OnInit {

  // from url
  appserverName: string = '';
  releaseName: string = '';

  selectedAppserver: Resource = null;
  appservers: Resource[] = [];
  selectedRelease: Release = null;
  relations: Relation[] = [];
  environments: Environment[] = [];
  environmentGroups: string[] = [];
  doSendEmail: boolean = false;
  doExecuteShakedownTest: boolean = false;
  // may only be enabled if above is true
  doNeighbourhoodTest: boolean = false;

  titleLabel: string = 'Create new deployment';
  errorMessage: string = '';
  isLoading: boolean = false;

  constructor(private resourceService: ResourceService,
              private environmentService: EnvironmentService,
              private activatedRoute: ActivatedRoute,
              private router: Router,
              private location: Location) {
  }

  ngOnInit(): void {

    this.activatedRoute.params.subscribe(
      (param: any) => {
        this.appserverName = param['appserverName'];
        console.log('appserverName: ' + this.appserverName);
        this.releaseName = param['releaseName'];
        console.log('releaseName: ' + this.releaseName);
      });

    if (this.selectedAppserver == null) {
      this.allAppserver();
      this.allEnvironments();
    }

    console.log('hello `Deployment` component');
  }

  onChangeAppserver(appServer) {
    console.log(appServer);
    this.selectedAppserver = appServer;
    this.appserverName = appServer.name;
    this.goTo([appServer.name]);
    this.resetVars();
  }

  onChangeRelease(release) {
    console.log(release);
    if (this.hasRelease(release.release)) {
      this.selectedRelease = release;
      this.goTo([this.selectedAppserver.name, release.release]);
      this.isLoading = true;
      this.resourceService.getRelated(this.selectedAppserver.name, release.release).subscribe(
        /* happy path */ r => this.relations = r,
        /* error path */ e => this.errorMessage = e,
        /* onComplete */ () => this.isLoading = false);
    }
  }

  resetVars() {
    this.selectedRelease = null;
    this.doSendEmail = false;
    this.doExecuteShakedownTest = false;
    this.doNeighbourhoodTest = false;
    this.environments.forEach(function (item) {
      item.selected = false
    });
  }

  isReadyForDeployment(): boolean {
    return (this.selectedRelease && this.environments.filter(item => item.selected).length > 0);
  }

  requestDeployment() {
    console.log(this.populateDeployment(true));
  }

  createDeployment() {
    console.log(this.populateDeployment(false));
  }

  populateDeployment(requestOnly: boolean): Deployment {
    if (this.isReadyForDeployment()) {
      let deployment: Deployment = <Deployment>{};
      deployment.appServerId = this.selectedAppserver.id;
      deployment.releaseId = this.selectedRelease.id;
      deployment.environmentIds = this.environments.filter(item => item.selected).map(val => val.id);
      deployment.doSendEmail = this.doSendEmail;
      deployment.doExecuteShakedownTest = this.doExecuteShakedownTest;
      deployment.doNeighbourhoodTest = this.doNeighbourhoodTest;
      deployment.requestOnly = requestOnly;
      return deployment;
    }
  }

  allEnvironments() {
    console.log('allEnvironments()');
    this.isLoading = true;
    this.environmentService
      .getAll().subscribe(
      /* happy path */ r => this.environments = r,
      /* error path */ e => this.errorMessage = e,
      /* onComplete */ () => this.extractEnvironmentGroups());
  }

  extractEnvironmentGroups() {
    this.environmentGroups = Array.from(new Set(this.environments.map(env => env.parent)));
    this.isLoading = false;
  }

  allAppserver() {
    console.log('allAppserver()');
    this.isLoading = true;
    this.resourceService
      .getByType('APPLICATIONSERVER').subscribe(
      /* happy path */ r => this.appservers = _.sortBy(_.uniqBy(r, 'id'), 'name'),
      /* error path */ e => this.errorMessage = e,
      /* onComplete */ () => this.setSelectedServer());
  }

  setSelectedServer() {
    console.log('setSelectedServer()');
    if (this.appserverName) {
      for (let i = 0; i < this.appservers.length; i++) {
        if (this.appservers[i].name === this.appserverName) {
          this.selectedAppserver = this.appservers[i];
          console.log('this.selectedAppserver: ' + this.appserverName);
          this.setSelectedRelease();
          break;
        }
      }
    }
    this.isLoading = false;
  }

  setSelectedRelease() {
    console.log('setSelectedRelease()');
    if (this.releaseName) {
      for (let i = 0; i < this.selectedAppserver.releases.length; i++) {
        if (this.selectedAppserver.releases[i].release === this.releaseName) {
          this.onChangeRelease(this.selectedAppserver.releases[i]);
          return;
        }
      }
    }
  }

  hasRelease(releaseName: string): boolean {
    if (this.selectedAppserver && this.selectedAppserver.releases) {
      for (let i = 0; i < this.selectedAppserver.releases.length; i++) {
        if (this.selectedAppserver.releases[i].release === releaseName) {
          return true;
        }
      }
    }
    return false;
  }

  goTo(destination: string[]) {
    destination.unshift('deployment');
    this.router.navigate(destination).then(
      function () {
        console.log('navigate success' + destination);
      },
      function () {
        console.log('navigate failure' + destination);
      }
    );
  }

  goBack(): void {
    this.location.back();
  }
}
