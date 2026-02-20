import { Component, computed, inject, signal, Signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { concatMap, finalize, map } from 'rxjs/operators';
import { of } from 'rxjs';
import { LoadingIndicatorComponent } from '../../shared/elements/loading-indicator.component';
import { PageComponent } from '../../layout/page/page.component';
import { ButtonComponent } from '../../shared/button/button.component';
import { IconComponent } from '../../shared/icon/icon.component';
import { CodeEditorComponent } from '../../shared/codemirror/code-editor.component';
import { DiffEditorComponent } from '../../shared/codemirror/diff-editor.component';
import { NgbDropdown, NgbDropdownItem, NgbDropdownMenu, NgbDropdownToggle } from '@ng-bootstrap/ng-bootstrap';
import { TestGenerationService } from '../services/test-generation.service';
import { ResourceService } from '../services/resource.service';
import { EnvironmentService } from '../../deployment/environment.service';
import {
  ComparedApplication,
  ComparedGenerationResult,
  ComparedNode,
  ComparedTemplate,
  EnvironmentGenerationResult,
  GeneratedTemplate,
  NodeGenerationResult,
} from '../models/test-generation-result';
import { NgClass, NgTemplateOutlet } from '@angular/common';
import { Release } from '../models/release';

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
    CodeEditorComponent,
    DiffEditorComponent,
    NgbDropdown,
    NgbDropdownMenu,
    NgbDropdownToggle,
    NgbDropdownItem,
    NgTemplateOutlet,
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
  releases: Signal<Release[]> = this.resourceService.releasesForResourceGroup;
  generating = signal(false);
  result = signal<EnvironmentGenerationResult | null>(null);
  compareResult = signal<ComparedGenerationResult | null>(null);
  compareReleaseId = signal<number | null>(null);
  errorMessage = signal<string | null>(null);
  expandedTemplates = signal<Set<string>>(new Set());

  isCompareMode = computed(() => this.compareReleaseId() !== null);

  compareReleaseName = computed(() => {
    const id = this.compareReleaseId();
    if (!id) return '';
    const release = this.releases().find((r) => r.id === id);
    return release?.release ?? release?.name ?? '';
  });

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
    this.compareResult.set(null);

    const compareRelease = this.compareReleaseName();

    if (compareRelease) {
      this.testGenerationService
        .generateTest(name, release, env)
        .pipe(
          concatMap((original) =>
            this.testGenerationService.generateTest(name, compareRelease, env).pipe(map((compared) => ({ original, compared }))),
          ),
          finalize(() => this.generating.set(false)),
        )
        .subscribe({
          next: ({ original, compared }) => {
            this.result.set(original);
            this.compareResult.set(this.mergeResults(original, compared));
          },
          error: (err) => {
            this.errorMessage.set(typeof err === 'string' ? err : 'Test generation failed.');
          },
        });
    } else {
      this.testGenerationService
        .generateTest(name, release, env)
        .pipe(finalize(() => this.generating.set(false))) // always remove the spinner...
        .subscribe({
          next: (res) => {
            this.result.set(res);
          },
          error: (err) => {
            this.errorMessage.set(typeof err === 'string' ? err : 'Test generation failed.');
          },
        });
    }
  }

  setCompareRelease(releaseId: number | null) {
    this.compareReleaseId.set(releaseId);
    this.result.set(null);
    this.compareResult.set(null);
  }

  private mergeResults(
    original: EnvironmentGenerationResult,
    compared: EnvironmentGenerationResult,
  ): ComparedGenerationResult {
    const nodeNames = new Set<string>();
    original.nodeGenerationResults?.forEach((n) => nodeNames.add(n.nodeName));
    compared.nodeGenerationResults?.forEach((n) => nodeNames.add(n.nodeName));

    const nodes: ComparedNode[] = [...nodeNames].sort().map((nodeName) => {
      const origNode = original.nodeGenerationResults?.find((n) => n.nodeName === nodeName);
      const compNode = compared.nodeGenerationResults?.find((n) => n.nodeName === nodeName);

      const asTemplates = this.mergeTemplates(origNode?.asTemplates ?? [], compNode?.asTemplates ?? []);

      const appNames = new Set<string>();
      origNode?.appResults?.forEach((a) => appNames.add(a.applicationName));
      compNode?.appResults?.forEach((a) => appNames.add(a.applicationName));

      const applications: ComparedApplication[] = [...appNames].sort().map((appName) => {
        const origApp = origNode?.appResults?.find((a) => a.applicationName === appName);
        const compApp = compNode?.appResults?.find((a) => a.applicationName === appName);
        return {
          applicationName: appName,
          templates: this.mergeTemplates(origApp?.templates ?? [], compApp?.templates ?? []),
          originalErrors: origApp?.errors ?? [],
          comparedErrors: compApp?.errors ?? [],
        };
      });

      return {
        nodeName,
        asTemplates,
        applications,
        originalErrors: origNode?.errors ?? [],
        comparedErrors: compNode?.errors ?? [],
      };
    });

    return {
      applicationServerName: original.applicationServerName,
      originalReleaseName: original.releaseName,
      comparedReleaseName: compared.releaseName,
      nodes,
      originalError: original.error,
      comparedError: compared.error,
    };
  }

  private mergeTemplates(original: GeneratedTemplate[], compared: GeneratedTemplate[]): ComparedTemplate[] {
    const lookup = new Map<string, ComparedTemplate>();
    const results: ComparedTemplate[] = [];

    for (const tmpl of original) {
      const ct: ComparedTemplate = { path: tmpl.path, original: tmpl, sameContent: false };
      lookup.set(tmpl.path, ct);
      results.push(ct);
    }
    for (const tmpl of compared) {
      const existing = lookup.get(tmpl.path);
      if (existing) {
        existing.compared = tmpl;
        existing.sameContent = (existing.original?.content ?? '') === (tmpl.content ?? '');
      } else {
        results.push({ path: tmpl.path, compared: tmpl, sameContent: false });
      }
    }
    return results.sort((a, b) => a.path.localeCompare(b.path));
  }

  comparedTemplateStatus(ct: ComparedTemplate): 'same' | 'diff' | 'added' | 'removed' {
    if (!ct.original) return 'added';
    if (!ct.compared) return 'removed';
    return ct.sameContent ? 'same' : 'diff';
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
