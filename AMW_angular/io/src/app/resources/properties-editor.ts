import { computed, signal } from '@angular/core';
import { Property } from './models/property';

type CreatePropertiesEditorOptions = {
  includeResetsInHasChanges?: boolean;
  unmarkResetOnChange?: boolean;
};

export function createPropertiesEditor(
  getProperties: () => Property[],
  options: CreatePropertiesEditorOptions = {},
) {
  const { includeResetsInHasChanges = true, unmarkResetOnChange = true } = options;

  const changedProperties = signal<Map<string, string>>(new Map());
  const resetProperties = signal<Set<string>>(new Set());

  const hasChanges = computed(() => {
    if (includeResetsInHasChanges) {
      return changedProperties().size > 0 || resetProperties().size > 0;
    }
    return changedProperties().size > 0;
  });

  function onPropertyChange(propertyName: string, newValue: string) {
    if (unmarkResetOnChange) {
      resetProperties.update((set) => {
        if (!set.has(propertyName)) return set;
        const next = new Set(set);
        next.delete(propertyName);
        return next;
      });
    }

    const props = getProperties() || [];
    const originalProperty = props.find((p) => p.name === propertyName);

    if (originalProperty && originalProperty.value !== newValue) {
      changedProperties.update((map) => {
        const newMap = new Map(map);
        newMap.set(propertyName, newValue);
        return newMap;
      });
    } else {
      changedProperties.update((map) => {
        const newMap = new Map(map);
        newMap.delete(propertyName);
        return newMap;
      });
    }
  }

  function onPropertyReset(propertyName: string, checked: boolean) {
    if (checked) {
      resetProperties.update((set) => {
        const next = new Set(set);
        next.add(propertyName);
        return next;
      });

      changedProperties.update((map) => {
        if (!map.has(propertyName)) return map;
        const next = new Map(map);
        next.delete(propertyName);
        return next;
      });
    } else {
      resetProperties.update((set) => {
        if (!set.has(propertyName)) return set;
        const next = new Set(set);
        next.delete(propertyName);
        return next;
      });
    }
  }

  function resetChanges() {
    changedProperties.set(new Map());
    resetProperties.set(new Set());
  }

  return {
    changedProperties,
    resetProperties,
    hasChanges,
    onPropertyChange,
    onPropertyReset,
    resetChanges,
  };
}
