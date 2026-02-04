export type PropertiesAction =
  | { type: 'valueChange'; name: string; value: string }
  | { type: 'resetToggle'; name: string; checked: boolean };

export type PropertiesValueChangeAction = Extract<PropertiesAction, { type: 'valueChange' }>;
export type PropertiesResetToggleAction = Extract<PropertiesAction, { type: 'resetToggle' }>;
