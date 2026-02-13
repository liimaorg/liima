import { Component, computed, inject, signal, Signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { map } from 'rxjs/operators';
import { LoadingIndicatorComponent } from '../../shared/elements/loading-indicator.component';
import { PageComponent } from '../../layout/page/page.component';
import { ButtonComponent } from '../../shared/button/button.component';
import { IconComponent } from '../../shared/icon/icon.component';
import { TestGenerationService } from '../services/test-generation.service';
import { ResourceService } from '../services/resource.service';
import { EnvironmentService } from '../../deployment/environment.service';
import { EnvironmentGenerationResult, GeneratedTemplate, NodeGenerationResult } from '../models/test-generation-result';
import { NgClass } from '@angular/common';

@Component({
  selector: 'app-test-generation',
  standalone: true,
  imports: [
    LoadingIndicatorComponent,
    PageComponent,
    ButtonComponent,
    IconComponent,
    RouterLink,
    NgClass,
  ],
  templateUrl: './test-generation.component.html',
  styleUrl: './test-generation.component.scss',
})
export class TestGenerationComponent {
  private testGenerationService = inject(TestGenerationService);
  private resourceService = inject(ResourceService);
  private environmentService = inject(EnvironmentService);
  private route = inject(ActivatedRoute);

  resourceId = toSignal(this.route.queryParamMap.pipe(map((params) => Number(params.get('id')))), { initialValue: 0 });
  contextId = toSignal(this.route.queryParamMap.pipe(map((params) => Number(params.get('ctx')))), { initialValue: 1 });

  resource: Signal<any> = this.resourceService.resource;
  generating = signal(false);
  result = signal<EnvironmentGenerationResult | null>(null);
  errorMessage = signal<string | null>(null);
  expandedTemplates = signal<Set<string>>(new Set());

  isLoading = computed(() => {
    if (this.resourceId()) {
      this.resourceService.setIdForResource(this.resourceId());
    }
    return this.generating();
  });

  resourceName = computed(() => this.resource()?.name ?? '');

  contextName = computed(() => {
    const tree = this.environmentService.environmentTree();
    const env = this.environmentService.findEnvironmentById(tree, this.contextId());
    return env?.name ?? '';
  });

  releaseName = computed(() => {
    const releases = this.resourceService.releasesForResourceGroup();
    const release = releases.find((r) => r.id === this.resourceId());
    return release?.release ?? release?.name ?? '';
  });

  backQueryParams = computed(() => ({
    id: this.resourceId(),
    ctx: this.contextId(),
  }));

  generate() {
    const name = this.resourceName();
    const release = this.releaseName();
    const env = this.contextName();

    if (!name || !release || !env) {
      this.errorMessage.set('Missing resource name, release, or environment information.');
      return;
    }

    this.generating.set(true);
    this.errorMessage.set(null);
    this.result.set(null);

    this.testGenerationService.generateTest(name, release, env).subscribe({
      next: (res) => {
        this.result.set(res);
        this.generating.set(false);
      },
      error: (err) => {
        this.errorMessage.set(typeof err === 'string' ? err : 'Test generation failed.');
        this.generating.set(false);
      },
    });
  }

  toggleTemplate(templateId: string) {
    const current = new Set(this.expandedTemplates());
    if (current.has(templateId)) {
      current.delete(templateId);
    } else {
      current.add(templateId);
    }
    this.expandedTemplates.set(current);
  }

  isExpanded(templateId: string): boolean {
    return this.expandedTemplates().has(templateId);
  }

  hasErrors(node: NodeGenerationResult): boolean {
    return node.errors && node.errors.length > 0;
  }

  templateHasErrors(template: GeneratedTemplate): boolean {
    return template.errors && template.errors.length > 0;
  }

  templateId(nodeIndex: number, prefix: string, templateIndex: number): string {
    return `${nodeIndex}-${prefix}-${templateIndex}`;
  }
}
