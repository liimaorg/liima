import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-property-types',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './property-types.component.html',
  styleUrl: './property-types.component.scss',
})
export class PropertyTypesComponent {
  properties = [
    {
      id: 1,
      propertyName: 'Example Property 1',
      encrypted: true,
      validation: 'Type: String, Length: 10',
      tags: ['tag1', 'tag2'],
    },
    {
      id: 2,
      propertyName: 'Example Property 2',
      encrypted: false,
      validation: 'Type: Number, Range: 1-100',
      tags: ['tag3'],
    },
  ];
}
