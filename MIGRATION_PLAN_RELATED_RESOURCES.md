# Migration Plan: Related Resources from JSF to Angular

## Overview

This document outlines the step-by-step migration of the related resources functionality from the JSF frontend (`editRelatedProperties.xhtml`) to the new Angular frontend.

### Current JSF Implementation

Location: `AMW_web/src/main/webapp/resources/mobi/editRelatedProperties.xhtml`

The JSF implementation displays:
- **Runtime relations** - relations to runtime resources
- **Unresolved relations** - resource type relations without concrete resources  
- **Consumed resources** - resources this resource consumes
- **Provided resources** - resources this resource provides
- **Resource type relations** - type-level relations

Each relation can have:
- Properties (editable per context)
- Templates
- Relation identifier/name
- Release selection dropdown
- Navigation to related resource

### Architecture Principles

1. **Clean Architecture**: AMW_rest depends on AMW_business, but AMW_business CANNOT depend on AMW_rest
2. **Reuse Existing Components**: Angular already has property components, tile structure, and service patterns
3. **Incremental Approach**: Each phase adds independent value and can be tested separately
4. **Backend Simplification**: Create new use cases/commands in AMW_business that wrap existing services

---

## Implementation Phases

### Phase 1: Add Related Resources Tile to Resource Edit View

**Goal**: Add empty tile/section to the Angular resource edit view

**Frontend Changes**:
- File: `AMW_angular/io/src/app/resources/resource-edit/resource-edit.component.html`
- Add `<app-resource-relations>` component after resource-functions-list
- Create skeleton component with basic tile structure, use `<app-tile-component>`

**Tasks**:
- [x] Create `resource-relations` folder under `AMW_angular/io/src/app/resources/resource-edit/`
- [x] Create `resource-relations.component.ts` (standalone component)
- [x] Create `resource-relations.component.html` with tile structure
- [x] Create `resource-relations.component.scss`
- [x] Add component to resource-edit template
- [x] Import component in resource-edit.component.ts

**Acceptance Criteria**:
- Empty "Related Resources" tile visible on resource edit page
- No errors in console
- Tile follows existing design patterns

---

### Phase 2: Create Backend Endpoint to Fetch Related Resources by Resource ID

**Goal**: Add ID-based endpoint to fetch relations (reuse existing logic)

**Analysis**: 
- `ResourceRelationsRest` already has `GET /resources/{resourceGroupName}/{releaseName}/relations` (lines 73-96)
- Returns `List<ResourceRelationDTO>` with consumed + provided relations + templates
- **Missing**: ID-based access, relation entity ID in DTO

**Backend Changes - AMW_rest**:
- Extend existing `ResourceRelationsRest.java` 
- Add new endpoint: `GET /resources/{resourceId}/relations`
- Reuse existing `getResourceRelations()` logic
- Enhance `ResourceRelationDTO` to include relation ID field

**Tasks**:
- [x] Add `id` field to `ResourceRelationDTO` (relation entity ID)
- [x] Create new REST class or add to existing: `GET /resources/{resourceId}/relations`
- [x] Fetch resource by ID, then call existing `getResourceRelations()` logic
- [x] No new use cases needed - reuse existing `ResourceLocator` methods
- [ ] Test endpoint manually

**Acceptance Criteria**:
- Endpoint returns all relations for a resource by ID
- Each relation includes: id, name, type, release, identifier, relationType
- Returns 404 if resource not found

**Note**: Grouping by type (consumed/provided/runtime) will be done on frontend for now

**Update (revised implementation)**:
- Switched endpoint to use `PropertyEditor.getRelationsForResource()` (native SQL, no lazy-init issues)
- Added `ResourceRelationDTO(ResourceEditRelation)` constructor
- Consumed-relations filter mirrors JSF `ResourceRelationModel`:
  - Excludes slave types `APPLICATION` and `RUNTIME` (these belong to the Runtime / Apps sections)
  - Groups by `(slaveGroupId, qualifiedIdentifier)` and picks best-matching release via `ResourceRelationService.getBestMatchingRelationRelease`
- Provided relations and templates deferred to later phases

---

### Phase 3: Create Angular Service to Fetch Related Resources

**Goal**: Create service to communicate with backend endpoint

**Frontend Changes**:
- File: `AMW_angular/io/src/app/resources/services/resource-relations.service.ts`
- Create service with methods to fetch relations
- Use reactive patterns (Signals/Observables)

**Tasks**:
- [x] Create `resource-relations.service.ts` in services folder
- [x] Create TypeScript interfaces/models for relation DTOs
- [x] Implement `getResourceRelations(resourceId, contextId)` method
- [x] Add loading state signals
- [x] Add error handling
- [x] Follow existing service patterns (see `resource-properties.service.ts`)

**Acceptance Criteria**:
- Service successfully fetches relations from backend
- Loading states properly managed
- Errors handled gracefully
- Service is injectable and follows Angular best practices

---

### Phase 4: Create Angular Component to Display Related Resources List

**Goal**: Display list of related resources grouped by category

**Frontend Changes**:
- Enhance `resource-relations.component.ts`
- Display grouped relations in collapsible sections or tabs
- Show resource name, type, release for each relation

**Tasks**:
- [x] Inject `resource-relations.service` into component
- [x] Create computed signals for grouped relations
- [x] Design UI layout (tabs vs accordion vs list)
- [x] Implement template to display:
  - Runtime relations section
  - Consumed resources section
  - Provided resources section
  - Unresolved relations section (if applicable)
- [x] Add loading indicator
- [x] Add empty state messages
- [x] Style according to existing design system

**Acceptance Criteria**:
- Relations displayed in organized groups
- Each relation shows: name, type, release
- Loading state shown while fetching
- Empty state shown when no relations exist
- Follows existing UI patterns

**Update**: Backend now returns a grouped response `GroupedResourceRelationsDTO` with four
lists (`runtime`, `consumed`, `provided`, `unresolved`). Unresolved relations use
`UnresolvedRelationDTO` (type + name) since they have no concrete resource instance.
Frontend consumes the grouped structure directly — no more frontend grouping.

---

### Phase 5: Add Navigation/Selection to View Individual Related Resource Details

**Goal**: Allow user to select a relation and view its details

**Frontend Changes**:
- Add selection state to component
- Display selected relation in detail panel
- Show release dropdown for relations with multiple releases

**Tasks**:
- [x] Add selection state signal to component
- [x] Add click handlers to relation list items
- [x] Create detail panel section in template
- [x] Display selected relation information
- [x] Add release dropdown (if multiple releases available)
- [x] Handle release switching
- [x] Add visual indicator for selected relation
- [x] Add "Remove Relation" button (disabled for now)

**Acceptance Criteria**:
- Clicking a relation selects it
- Detail panel shows selected relation info
- Release dropdown allows switching between releases
- Visual feedback for selected state
- Navigation works smoothly

---

### Phase 6: Create Backend Endpoint to Fetch Properties for a Specific Resource Relation

**Goal**: Fetch properties for a selected relation in a specific context

**Backend Changes - AMW_business**:
- Create `GetRelationPropertiesUseCase`
- Use existing `PropertyEditor.getPropertiesForRelatedResource()`
- Return properties for specific relation + context

**Backend Changes - AMW_rest**:
- Option A: Enhance existing `ResourceRelationPropertiesRest`
- Option B: Add new endpoint to resources REST
- Endpoint: `GET /resources/{resourceId}/relations/{relationId}/properties?contextId={contextId}`
- Return list of PropertyDTO

**Tasks**:
- [x] Create `GetRelationPropertiesUseCase` in AMW_business
- [x] Create REST endpoint method
- [x] Ensure proper permission checks
- [x] Write unit tests
- [x] Test endpoint manually

**Acceptance Criteria**:
- Endpoint returns properties for specific relation
- Properties include all metadata (value, defaultValue, context, etc.)
- Returns 404 if relation not found
- Proper permission checks in place

---

### Phase 7: Display Relation Properties Using Existing Property Components

**Goal**: Show relation properties using existing Angular property components

**Frontend Changes**:
- Enhance service to fetch relation properties
- Display properties in detail panel
- Reuse `<app-properties-list>` component

**Tasks**:
- [x] Add `getRelationProperties(resourceId, relationId, contextId)` to service
- [x] Add properties signal to component
- [x] Include `<app-properties-list>` in detail panel template
- [x] Pass relation properties to component
- [x] Handle loading state for properties
- [x] Display in read-only mode initially

**Acceptance Criteria**:
- Properties displayed when relation selected
- Uses existing property list component
- Properties shown in read-only mode
- Loading indicator while fetching
- Empty state if no properties

---

### Phase 8: Add Relation Identifier Editing Capability

**Goal**: Allow editing the relation identifier/name

**Frontend Changes**:
- Add input field for relation identifier
- Track changes separately from properties
- Add validation

**Tasks**:
- [x] Add identifier input field to detail panel
- [x] Add signal for identifier changes
- [x] Add validation (if required)
- [x] Add visual indicator for unsaved changes
- [x] Disable if user lacks permission
- [x] Add to save operation (Phase 9)

**Acceptance Criteria**:
- Identifier editable in input field
- Changes tracked separately
- Validation works correctly
- Disabled when no permission
- Visual feedback for changes

---

### Phase 9: Add Property Editing/Saving for Relation Properties

**Goal**: Enable editing and saving relation properties

**Backend Changes - AMW_business**:
- Create `UpdateRelationPropertiesUseCase`
- Invoke existing `PropertyEditor` methods for relation properties
- Handle bulk updates and resets

**Backend Changes - AMW_rest**:
- Create endpoint for bulk property updates
- Endpoint: `PUT /resources/{resourceId}/relations/{relationId}/properties?contextId={contextId}`
- Accept updates and resets arrays

**Frontend Changes**:
- Enable property editing in properties list
- Add save button
- Track changes using properties editor pattern
- Handle save operation

**Tasks**:
- [x] Create `UpdateRelationPropertiesUseCase` in AMW_business
- [x] Create REST endpoint for bulk update
- [x] Add `updateRelationProperties()` method to service
- [x] Extend component to track property changes
- [x] Use `createPropertiesEditor()` pattern from base-properties
- [x] Add save button to detail panel
- [x] Implement save logic
- [x] Add success/error toast messages
- [x] Reload properties after save
- [x] Handle validation errors

**Acceptance Criteria**:
- Properties editable when permission granted
- Changes tracked correctly
- Save button enabled when changes exist
- Save operation updates backend
- Success/error feedback shown
- Properties reloaded after save
- Validation errors displayed

---

### Phase 10: Add Relation Management (Add/Remove Relations)

**Goal**: Allow adding and removing resource relations

**Backend Changes - AMW_business**:
- Create `AddResourceRelationCommand`
- Create `RemoveResourceRelationCommand`
- Wrap existing `RelationEditor` methods

**Backend Changes - AMW_rest**:
- Enhance existing endpoints or create new ones
- `POST /resources/{resourceId}/relations`
- `DELETE /resources/{resourceId}/relations/{relationId}`

**Frontend Changes**:
- Add "Add Relation" button
- Add modal/dialog for selecting resource to add
- Enable "Remove Relation" button
- Add confirmation dialog for removal

**Tasks**:
- [x] Create `AddResourceRelationCommand` in AMW_business
- [x] Create `RemoveResourceRelationCommand` in AMW_business
- [x] Create/enhance REST endpoints
- [x] Add service methods for add/remove
- [x] Create add relation modal component
- [x] Implement resource selection in modal
- [x] Add relation type selection (consumed/provided)
- [x] Implement add logic
- [x] Enable remove button
- [x] Add confirmation dialog for removal
- [x] Implement remove logic
- [x] Reload relations after add/remove
- [x] Add permission checks

**Acceptance Criteria**:
- "Add Relation" button opens modal
- Modal allows selecting resource and type
- Add operation creates relation
- Remove button shows confirmation
- Remove operation deletes relation
- Relations list refreshes after operations
- Proper permission checks in place
- Error handling for conflicts

---

### Phase 11: Add Templates Display for Relations

**Goal**: Display templates associated with relations

**Frontend Changes**:
- Fetch templates for selected relation
- Display templates section in detail panel
- Reuse existing template components if available

**Tasks**:
- [ ] Check if relation DTOs include templates
- [ ] If not, enhance backend to include templates
- [ ] Add templates section to detail panel
- [ ] Display template names and metadata
- [ ] Add navigation to template details (if applicable)
- [ ] Style according to existing patterns

**Acceptance Criteria**:
- Templates displayed for selected relation
- Template information is accurate
- Follows existing template display patterns
- Empty state if no templates

---

### Phase 12: Handle Unresolved Relations Display

**Goal**: Display and manage unresolved relations (type-level relations without instances)

**Frontend Changes**:
- Display unresolved relations in separate section
- Show resource type information
- Allow resolving by creating concrete relation (if applicable)

**Tasks**:
- [ ] Ensure backend returns unresolved relations
- [ ] Create separate section for unresolved relations
- [ ] Display resource type name and identifier
- [ ] Add ability to resolve (create concrete relation)
- [ ] Style differently from resolved relations
- [ ] Add tooltips/help text explaining unresolved relations

**Acceptance Criteria**:
- Unresolved relations shown separately
- Clear indication they are unresolved
- User can understand what unresolved means
- Option to resolve (if permissions allow)

---

## Testing Strategy

### Unit Tests
- [ ] Test all use cases in AMW_business
- [ ] Test REST endpoints in AMW_rest
- [ ] Test Angular services
- [ ] Test Angular components

### Integration Tests
- [ ] Test full flow: fetch relations → display → edit properties → save
- [ ] Test add/remove relation flows
- [ ] Test permission scenarios

### Manual Testing
- [ ] Test with different resource types
- [ ] Test with different contexts
- [ ] Test with various permission levels
- [ ] Test error scenarios
- [ ] Test with large numbers of relations

---

## Migration Checklist

- [ ] All phases completed
- [ ] All tests passing
- [ ] Documentation updated
- [ ] Code reviewed
- [ ] Performance verified
- [ ] Accessibility checked
- [ ] JSF implementation can be deprecated

---

## Notes

### Existing Backend Services to Leverage
- `ResourceRelationLocator` - fetching relations
- `ResourceRelationService` - relation operations
- `PropertyEditor` - property operations on relations
- `RelationEditor` - adding/removing relations
- `ContextLocator` - context information

### Existing Angular Patterns to Follow
- `BasePropertiesDirective` - property editing pattern
- `createPropertiesEditor()` - change tracking
- `ResourcePropertiesService` - service pattern
- `TileComponent` - UI tile structure
- `PropertiesListComponent` - property display

### Key Differences: Properties vs Relation Properties
- Regular properties: attached to resource
- Relation properties: attached to resource relation
- Backend handles them differently via `PropertyEditor`
- Frontend should abstract this difference

### Permission Considerations
- Check permissions for viewing relations
- Check permissions for editing relation properties
- Check permissions for adding/removing relations
- Use existing `AuthService` patterns
