import { Component, inject, OnInit } from '@angular/core';
import { IconComponent } from '../../shared/icon/icon.component';
import { EnvironmentService } from '../../deployment/environment.service';
import { Environment, EnvironmentTree } from '../../deployment/environment';

@Component({
  selector: 'app-environments-page',
  standalone: true,
  imports: [IconComponent],
  templateUrl: './environments-page.component.html',
})
export class EnvironmentsPageComponent implements OnInit {
  private environmentsService = inject(EnvironmentService);
  environmentTree: EnvironmentTree[];

  ngOnInit(): void {
    //this.getUserPermissions(); //TODO Handle permissions
    this.environmentsService.getContexts().subscribe((contexts) => {
      this.environmentTree = this.buildEnvironmentTree(contexts)[0].children;
    });
  }

  private buildEnvironmentTree(environments: Environment[], parentName: string | null = null): EnvironmentTree[] {
    return environments
      .filter((environment) => environment.parent === parentName)
      .map((environment) => {
        return {
          id: environment.id,
          name: environment.name,
          nameAlias: environment.nameAlias,
          children: this.buildEnvironmentTree(environments, environment.name),
          selected: environment.selected,
          disabled: environment.disabled,
        };
      });
  }
}
