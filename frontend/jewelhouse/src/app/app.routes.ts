import { Routes } from '@angular/router';
import { HomeComponent } from './home.component';
import { ItemManagementComponent } from './item-management.component';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'items', component: ItemManagementComponent },
  { path: '**', redirectTo: '' }
];
