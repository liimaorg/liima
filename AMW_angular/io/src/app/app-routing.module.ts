import { Routes, RouterModule } from '@angular/router';
import { HomeComponent } from './home';
import { NgModule } from '@angular/core';

export const routes: Routes = [
  {path: '', component: HomeComponent},
//  {path: 'about', component: AboutComponent},
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {useHash: true})],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
