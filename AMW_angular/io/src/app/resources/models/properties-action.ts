export type PropertiesAction =
  | { type: 'valueChange'; name: string; value: string }
  | { type: 'resetToggle'; name: string; checked: boolean }
  | { type: 'validationChange'; name: string; invalid: boolean }
  | { type: 'editPropertyDescriptor'; id: number }
  | { type: 'deletePropertyDescriptor'; id: number };

export type PropertiesValueChangeAction = Extract<PropertiesAction, { type: 'valueChange' }>;
export type PropertiesResetToggleAction = Extract<PropertiesAction, { type: 'resetToggle' }>;
export type PropertiesValidationChangeAction = Extract<PropertiesAction, { type: 'validationChange' }>;
export type PropertiesEditAction = Extract<PropertiesAction, { type: 'editPropertyDescriptor' }>;
export type PropertiesDeleteAction = Extract<PropertiesAction, { type: 'deletePropertyDescriptor' }>;
