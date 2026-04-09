import { Routes } from '@angular/router';
import { ArticlesListComponent } from './articles-list.component';
import { ArticleDetailComponent } from './article-detail.component';
export const ARTICLES_ROUTES: Routes = [{ path: '', component: ArticlesListComponent }, { path: ':id', component: ArticleDetailComponent }];
