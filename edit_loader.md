Please refactor the angular edit resource and resource type screen to used bootstrap placeholders (https://getbootstrap.com/docs/5.3/components/placeholders/) instead of a global loader with a spinner:
* No full screen spinners/loaders should be used anymore on the edit resource and resource type pages.
* All components needing loading treatment should have a placeholder until it is loaded
  * For components like Releases where data arrives already-resolved from the parent, no placeholder is needed.
* The placeholder should match the general form of the component.
  * The property component placeholder should be a two-column grid (matching the real properties-list layout), each field rendered as a short label-width bar stacked above a full-width input-height bar.
  * Env should be an indented list
  * For components whose data is genuinely fetched independently (Templates, Functions, Properties, Relations), show the placeholder inside the collapsed tile body.
* Skeleton shown only until first successful load; subsequent reloads keep the real fields visible but inert/dimmed and show a small spinner in the title.
  * Should use same spinner animation as the full screen spinner/loader.
  * Relationship Templates should have its own spinner, as it load separately from the relation properties.
  * Same for clicking save. Spinner should be show immediately when clicking save.
  * While dimmed/inert after a save, fields must keep showing the just-saved values, not revert to the pre-save values until the fresh data has actually loaded.
* The save banners should be moved to a standard toast component using the ToastService.
* Add/update all unit tests and e2e tests.