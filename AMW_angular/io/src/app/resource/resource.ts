import {Release} from "./release";

export interface Resource {
  name: string;
  type: string;
  releases: Release[];
}
