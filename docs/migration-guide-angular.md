# Migrating from Blossom FreeMarker UI to Angular

This guide walks through migrating a project that uses `blossom-starter-ui-web` (FreeMarker/Inspinia) to the new `blossom-starter-ui-angular` (Angular/Material).

---

## Table of Contents

1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [Step 1: Switch the Maven Starter](#step-1-switch-the-maven-starter)
4. [Step 2: Create the Angular Workspace](#step-2-create-the-angular-workspace)
5. [Step 3: Wire Up the Angular App](#step-3-wire-up-the-angular-app)
6. [Step 4: Migrate Custom Pages](#step-4-migrate-custom-pages)
7. [Step 5: Migrate Custom Menu Items](#step-5-migrate-custom-menu-items)
8. [Step 6: Remove Legacy Artifacts](#step-6-remove-legacy-artifacts)
9. [Step 7: Build and Test](#step-7-build-and-test)
10. [Reference: Angular Project Structure](#reference-angular-project-structure)
11. [Reference: API Endpoints](#reference-api-endpoints)
12. [Reference: Shared Components](#reference-shared-components)
13. [Running Both UIs During Migration](#running-both-uis-during-migration)
14. [Troubleshooting](#troubleshooting)

---

## Overview

### What changed

| Before | After |
|--------|-------|
| `blossom-starter-ui-web` | `blossom-starter-ui-angular` |
| FreeMarker templates (`.ftl`) | Angular standalone components |
| Bootstrap 3 / Inspinia / jQuery | Angular Material |
| Server-side rendering | Single Page Application |
| `@BlossomController` + `ModelAndView` | `@BlossomApiController` + JSON |
| Session + AJAX with `X-Requested-With` | Session + CSRF cookie + Angular `HttpClient` |
| FTL views at `/blossom/` | Angular app at `/blossom/ng/` |

### What stayed the same

- **Server-side code**: Entities, DTOs, services, DAOs, repositories are unchanged.
- **Menu system**: `MenuItem` beans still drive the navigation. The Angular app reads the menu from `GET /blossom/api/configuration`.
- **Security model**: Session-based authentication, `@PreAuthorize` privilege checks, CSRF protection.
- **Module auto-discovery**: Add a Maven dependency, register beans in a `@Configuration` class, and everything wires up automatically.
- **API controllers**: If your project already had `@BlossomApiController` endpoints, they work as-is.

---

## Prerequisites

- **Blossom 4.x** (the version with Angular support)
- **Node.js 20+** and **npm 10+** (for building the Angular app)
- **Java 21+**
- Basic familiarity with Angular (standalone components, services, routing)

---

## Step 1: Switch the Maven Starter

### 1.1 Replace the starter dependency

In your project's `pom.xml`, replace:

```xml
<dependency>
  <groupId>com.blossom-project</groupId>
  <artifactId>blossom-starter-ui-web</artifactId>
  <version>4.0.0-SNAPSHOT</version>
</dependency>
```

With:

```xml
<dependency>
  <groupId>com.blossom-project</groupId>
  <artifactId>blossom-starter-ui-angular</artifactId>
  <version>4.0.0-SNAPSHOT</version>
</dependency>
```

This pulls in `blossom-starter-ui-api` (all REST endpoints) and `blossom-ui-angular` (the prebuilt Angular app with all standard pages).

### 1.2 If your project has custom pages

If your project only uses the standard Blossom pages (users, groups, roles, dashboard, etc.) with no custom controllers or FTL templates, **you are done** -- skip to [Step 7](#step-7-build-and-test).

If your project has custom `@BlossomController` classes and FTL templates, continue to Step 2.

---

## Step 2: Create the Angular Workspace

Your project needs its own Angular workspace to add custom pages alongside the Blossom defaults.

### 2.1 Create the module structure

```
my-project/
  my-project-server/          # Your existing Spring Boot app
    pom.xml
    src/
  my-project-angular/         # NEW: Angular workspace
    pom.xml
    angular.json
    package.json
    tsconfig.json
    src/
      app/
        app.config.ts
        app.routes.ts
        app.component.ts
        features/              # Your custom pages go here
          my-feature/
    projects/
      my-feature-lib/          # Optional: reusable Angular libraries
```

### 2.2 Create the Angular module pom.xml

```xml
<!-- my-project-angular/pom.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <modelVersion>4.0.0</modelVersion>

  <parent>
    <groupId>com.mycompany</groupId>
    <artifactId>my-project</artifactId>
    <version>1.0.0-SNAPSHOT</version>
  </parent>

  <artifactId>my-project-angular</artifactId>
  <packaging>jar</packaging>

  <properties>
    <node.version>v20.11.1</node.version>
    <npm.version>10.2.4</npm.version>
    <frontend-maven-plugin.version>1.15.1</frontend-maven-plugin.version>
  </properties>

  <build>
    <plugins>
      <plugin>
        <groupId>com.github.eirslett</groupId>
        <artifactId>frontend-maven-plugin</artifactId>
        <version>${frontend-maven-plugin.version}</version>
        <configuration>
          <workingDirectory>${project.basedir}</workingDirectory>
          <installDirectory>${project.build.directory}/node</installDirectory>
        </configuration>
        <executions>
          <execution>
            <id>install-node-and-npm</id>
            <goals><goal>install-node-and-npm</goal></goals>
            <configuration>
              <nodeVersion>${node.version}</nodeVersion>
              <npmVersion>${npm.version}</npmVersion>
            </configuration>
          </execution>
          <execution>
            <id>npm-install</id>
            <goals><goal>npm</goal></goals>
            <configuration><arguments>install</arguments></configuration>
          </execution>
          <execution>
            <id>npm-build</id>
            <goals><goal>npm</goal></goals>
            <phase>generate-resources</phase>
            <configuration><arguments>run build</arguments></configuration>
          </execution>
        </executions>
      </plugin>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-resources-plugin</artifactId>
        <executions>
          <execution>
            <id>copy-angular-dist</id>
            <phase>generate-resources</phase>
            <goals><goal>copy-resources</goal></goals>
            <configuration>
              <outputDirectory>${project.build.outputDirectory}/static/blossom</outputDirectory>
              <resources>
                <resource>
                  <directory>${project.basedir}/dist/my-app/browser</directory>
                </resource>
              </resources>
            </configuration>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
</project>
```

### 2.3 Initialize the Angular workspace

```bash
cd my-project-angular
npx @angular/cli@19 new my-app --directory=. --routing --style=scss --skip-git --ssr=false
npm install @angular/material @angular/cdk @angular/animations
```

### 2.4 Add an `.npmrc` if needed

If your environment has a private npm registry that does not mirror public packages, create `.npmrc`:

```
registry=https://registry.npmjs.org/
```

---

## Step 3: Wire Up the Angular App

### 3.1 Configure `tsconfig.json` paths

The Blossom Angular libraries (`@blossom/core`, `@blossom/ui`, `@blossom/shell`) are published as part of the `blossom-ui-angular` JAR. For development, point to them in your `node_modules` or install them as npm packages.

Since they ship inside the blossom JAR at build time, the simplest approach during development is to symlink or copy them. Alternatively, if blossom publishes them to a registry:

```json
// tsconfig.json
{
  "compilerOptions": {
    "paths": {
      "@blossom/core": ["./node_modules/@blossom/core"],
      "@blossom/ui": ["./node_modules/@blossom/ui"],
      "@blossom/shell": ["./node_modules/@blossom/shell"]
    }
  }
}
```

For the initial setup, copy the library sources from `blossom-ui-angular/projects/blossom/` into your workspace's `projects/` directory and reference them directly:

```json
{
  "compilerOptions": {
    "paths": {
      "@blossom/core": ["./projects/blossom/core/src/public-api.ts"],
      "@blossom/ui": ["./projects/blossom/ui/src/public-api.ts"],
      "@blossom/shell": ["./projects/blossom/shell/src/public-api.ts"]
    }
  }
}
```

### 3.2 Configure `app.config.ts`

```typescript
import { ApplicationConfig, provideZoneChangeDetection, APP_INITIALIZER } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { ConfigurationService, csrfInterceptor, authErrorInterceptor } from '@blossom/core';
import { routes } from './app.routes';
import { catchError, of } from 'rxjs';

function initializeApp(configService: ConfigurationService) {
  return () => configService.load().pipe(catchError(() => of(null)));
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(withInterceptors([csrfInterceptor, authErrorInterceptor])),
    provideAnimationsAsync(),
    {
      provide: APP_INITIALIZER,
      useFactory: initializeApp,
      deps: [ConfigurationService],
      multi: true
    }
  ]
};
```

Key elements:
- `csrfInterceptor` reads the `XSRF-TOKEN` cookie and attaches the `X-XSRF-TOKEN` header on mutating requests.
- `authErrorInterceptor` redirects to `/login` on 401 responses.
- `APP_INITIALIZER` loads the configuration (menu, user, privileges) before the app renders.

### 3.3 Configure `app.routes.ts`

```typescript
import { Routes } from '@angular/router';
import { LayoutComponent, LoginComponent, ErrorPageComponent } from '@blossom/shell';
import { authGuard, privilegeGuard } from '@blossom/core';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  {
    path: '',
    component: LayoutComponent,
    canActivate: [authGuard],
    children: [
      // Blossom standard routes (all lazy-loaded)
      { path: '', loadChildren: () => import('./features/home/routes').then(m => m.HOME_ROUTES) },
      { path: 'profile', loadChildren: () => import('./features/profile/routes').then(m => m.PROFILE_ROUTES) },
      { path: 'administration/users', loadChildren: () => import('./features/admin-users/routes').then(m => m.USERS_ROUTES), canActivate: [privilegeGuard], data: { privilege: 'administration:users:read' } },
      // ... other standard routes ...

      // YOUR CUSTOM ROUTES
      {
        path: 'my-feature',
        loadChildren: () => import('./features/my-feature/routes').then(m => m.MY_FEATURE_ROUTES),
        canActivate: [privilegeGuard],
        data: { privilege: 'my:feature:read' }
      },

      // Error pages (must be last)
      { path: '403', component: ErrorPageComponent, data: { code: '403', message: 'Access Denied', icon: 'lock' } },
      { path: '**', component: ErrorPageComponent, data: { code: '404', message: 'Page not found', icon: 'error_outline' } }
    ]
  }
];
```

### 3.4 Configure `index.html`

```html
<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <title>My Application</title>
  <base href="/blossom/ng/">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@300;400;500&display=swap" rel="stylesheet">
  <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
  <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css" rel="stylesheet">
</head>
<body>
  <app-root></app-root>
</body>
</html>
```

### 3.5 Configure `app.component.ts`

```typescript
import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  template: '<router-outlet></router-outlet>'
})
export class AppComponent {}
```

---

## Step 4: Migrate Custom Pages

For each custom `@BlossomController` + FTL template in your project, create an equivalent Angular feature module.

### 4.1 Before (FTL)

**Server side:**
```java
@BlossomController
@RequestMapping("/my-feature")
@OpenedMenu("myFeature")
public class MyFeatureController {

  @GetMapping
  @PreAuthorize("hasAuthority('my:feature:read')")
  public ModelAndView list(Model model, Pageable pageable) {
    model.addAttribute("items", myService.getAll(pageable));
    return new ModelAndView("blossom/my-feature/list", model.asMap());
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAuthority('my:feature:read')")
  public ModelAndView detail(@PathVariable Long id, Model model) {
    model.addAttribute("item", myService.getOne(id));
    return new ModelAndView("blossom/my-feature/detail", model.asMap());
  }

  @PostMapping("/_create")
  @PreAuthorize("hasAuthority('my:feature:create')")
  public ModelAndView create(@Valid MyCreateForm form, BindingResult result) {
    // ...
  }
}
```

**FTL template (`my-feature/list.ftl`):**
```html
<#import "/spring.ftl" as spring>
<#import "/blossom/master/master.ftl" as master>
<#import "/blossom/utils/table.ftl" as table>
<@master.default currentUser=currentUser>
  <@table.pagetable page=items columns=["name","status"] ...>
  </@table.pagetable>
</@master.default>
```

### 4.2 After (Angular)

**Server side -- replace the web controller with an API controller:**

```java
@BlossomApiController
@RequestMapping("/my-feature")
public class MyFeatureApiController {

  private final MyFeatureService myService;

  public MyFeatureApiController(MyFeatureService myService) {
    this.myService = myService;
  }

  @GetMapping
  @PreAuthorize("hasAuthority('my:feature:read')")
  public Page<MyFeatureDTO> list(
    @RequestParam(value = "q", required = false) String q,
    @PageableDefault(size = 25) Pageable pageable) {
    return myService.getAll(pageable);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAuthority('my:feature:read')")
  public ResponseEntity<MyFeatureDTO> get(@PathVariable Long id) {
    MyFeatureDTO item = myService.getOne(id);
    if (item == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    return new ResponseEntity<>(item, HttpStatus.OK);
  }

  @PostMapping
  @PreAuthorize("hasAuthority('my:feature:create')")
  public ResponseEntity<MyFeatureDTO> create(@Valid @RequestBody MyCreateForm form) {
    return new ResponseEntity<>(myService.create(form), HttpStatus.CREATED);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAuthority('my:feature:write')")
  public ResponseEntity<MyFeatureDTO> update(@PathVariable Long id, @Valid @RequestBody MyUpdateForm form) {
    return new ResponseEntity<>(myService.update(id, form), HttpStatus.OK);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAuthority('my:feature:delete')")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    myService.delete(myService.getOne(id), false);
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
```

Key differences:
- `@BlossomApiController` instead of `@BlossomController` (mapped to `/blossom/api/` prefix instead of `/blossom/`)
- Returns DTOs directly (JSON) instead of `ModelAndView`
- `@RequestBody` for JSON input instead of `@ModelAttribute` form binding
- DELETE/PUT HTTP methods instead of POST-based form actions

**Angular service (`features/my-feature/my-feature.service.ts`):**

```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Page } from '@blossom/core';

export interface MyFeatureDTO {
  id: number;
  name: string;
  status: string;
  // ... your fields
}

@Injectable({ providedIn: 'root' })
export class MyFeatureService {
  private readonly API = '/blossom/api/my-feature';

  constructor(private http: HttpClient) {}

  list(q = '', page = 0, size = 25): Observable<Page<MyFeatureDTO>> {
    return this.http.get<Page<MyFeatureDTO>>(this.API, {
      params: { page: page.toString(), size: size.toString(), ...(q ? { q } : {}) }
    });
  }

  get(id: number): Observable<MyFeatureDTO> {
    return this.http.get<MyFeatureDTO>(`${this.API}/${id}`);
  }

  create(form: any): Observable<MyFeatureDTO> {
    return this.http.post<MyFeatureDTO>(this.API, form);
  }

  update(id: number, form: any): Observable<MyFeatureDTO> {
    return this.http.put<MyFeatureDTO>(`${this.API}/${id}`, form);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API}/${id}`);
  }
}
```

**Angular list component (`features/my-feature/my-feature-list.component.ts`):**

```typescript
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { SearchBarComponent } from '@blossom/ui';
import { MyFeatureService, MyFeatureDTO } from './my-feature.service';

@Component({
  selector: 'app-my-feature-list',
  standalone: true,
  imports: [CommonModule, MatTableModule, MatPaginatorModule, MatButtonModule, MatIconModule, SearchBarComponent],
  template: `
    <div class="page-header">
      <h2>My Features</h2>
      <button mat-flat-button color="primary" (click)="create()">
        <mat-icon>add</mat-icon> Create
      </button>
    </div>

    <blossom-search-bar (search)="onSearch($event)"></blossom-search-bar>

    <table mat-table [dataSource]="items" class="full-width">
      <ng-container matColumnDef="name">
        <th mat-header-cell *matHeaderCellDef>Name</th>
        <td mat-cell *matCellDef="let item">{{ item.name }}</td>
      </ng-container>
      <ng-container matColumnDef="status">
        <th mat-header-cell *matHeaderCellDef>Status</th>
        <td mat-cell *matCellDef="let item">{{ item.status }}</td>
      </ng-container>
      <tr mat-header-row *matHeaderRowDef="['name', 'status']"></tr>
      <tr mat-row *matRowDef="let row; columns: ['name', 'status'];"
          (click)="router.navigate(['/my-feature', row.id])"
          style="cursor: pointer"></tr>
    </table>

    <mat-paginator [length]="total" [pageSize]="25"
      (page)="onPage($event)" showFirstLastButtons></mat-paginator>
  `,
  styles: [`
    .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
    .full-width { width: 100%; }
  `]
})
export class MyFeatureListComponent implements OnInit {
  items: MyFeatureDTO[] = [];
  total = 0;
  private query = '';
  private page = 0;

  constructor(private svc: MyFeatureService, public router: Router) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.svc.list(this.query, this.page).subscribe(p => {
      this.items = p.content;
      this.total = p.totalElements;
    });
  }

  onSearch(q: string): void { this.query = q; this.page = 0; this.load(); }
  onPage(e: PageEvent): void { this.page = e.pageIndex; this.load(); }
  create(): void { /* open dialog or navigate */ }
}
```

**Angular routes (`features/my-feature/routes.ts`):**

```typescript
import { Routes } from '@angular/router';
import { MyFeatureListComponent } from './my-feature-list.component';
import { MyFeatureDetailComponent } from './my-feature-detail.component';

export const MY_FEATURE_ROUTES: Routes = [
  { path: '', component: MyFeatureListComponent },
  { path: ':id', component: MyFeatureDetailComponent }
];
```

### 4.3 Conversion cheat sheet

| FTL Concept | Angular Equivalent |
|---|---|
| `<@table.pagetable>` macro | `<table mat-table>` + `<mat-paginator>` or `<blossom-table>` |
| `<@master.default>` layout | `LayoutComponent` (automatic via routing) |
| `<@spring.formInput>` | `<mat-form-field>` + `<input matInput [(ngModel)]="...">` |
| `SweetAlert` confirm | `ConfirmDialogComponent` from `@blossom/ui` |
| `toastr` notification | `NotificationService` from `@blossom/core` |
| `<@tabulation>` | `<mat-tab-group>` |
| jsTree privilege display | `<blossom-privilege-tree>` from `@blossom/ui` |
| Summernote rich text | `<textarea matInput>` (or add `ngx-quill`) |
| Dropzone file upload | `<input type="file">` + `HttpClient` multipart |
| `<@privilege.check>` | `privilegeGuard` on route or `*ngIf` with `ConfigurationService.config.authorities` |
| `${currentUser.user.firstname}` | `ConfigurationService.config.user.firstname` |
| `<#list items as item>` | `*ngFor="let item of items"` |
| `<#if condition>` | `*ngIf="condition"` |

---

## Step 5: Migrate Custom Menu Items

Menu item registration is **unchanged** on the server side. Your existing `MenuItem` beans continue to work:

```java
@Bean
public MenuItem myFeatureMenuItem(MenuItemBuilder builder,
    @Qualifier("administrationMenuItem") MenuItem parent) {
  return builder
    .key("myFeature")
    .label("menu.my.feature")
    .link("/my-feature")        // The Angular route path
    .icon("fa fa-star")
    .privilege(myReadPrivilege())
    .parent(parent)
    .build();
}
```

The Angular app reads this from `GET /blossom/api/configuration` and renders it in the sidebar automatically. The `link` field is matched to Angular routes.

**The `link` value must match your Angular route path.** For example, if your route is:
```typescript
{ path: 'my-feature', loadChildren: () => ... }
```
Then the `MenuItem.link` should be `/my-feature`.

---

## Step 6: Remove Legacy Artifacts

Once all custom pages are migrated to Angular:

### 6.1 Delete FTL templates
```bash
rm -rf src/main/resources/templates/
```

### 6.2 Delete legacy static assets
```bash
rm -rf src/main/resources/public/
rm -rf src/main/resources/static/css/
rm -rf src/main/resources/static/js/
```

### 6.3 Remove legacy web controllers

Delete any `@BlossomController` classes that returned `ModelAndView`. Keep the `@BlossomApiController` replacements.

### 6.4 Remove the old starter dependency

Make sure `blossom-starter-ui-web` is no longer in your `pom.xml`. Only `blossom-starter-ui-angular` should be present.

---

## Step 7: Build and Test

### 7.1 Build the Angular app

```bash
cd my-project-angular
npm install
npm run build
```

### 7.2 Build the Maven project

```bash
cd my-project
mvn clean package
```

The `frontend-maven-plugin` will install Node, run `npm install`, run `ng build`, and copy the dist into the JAR.

### 7.3 Run and verify

```bash
java -jar my-project-server/target/my-project-server.jar
```

- Angular UI: `http://localhost:8080/blossom/ng/`
- API endpoints: `http://localhost:8080/blossom/api/`
- Login with your existing credentials

### 7.4 Verify checklist

- [ ] Login page renders and authentication works
- [ ] Sidebar shows all menu items (standard + custom)
- [ ] Each page loads data correctly from the API
- [ ] Create/update/delete operations work
- [ ] Privilege-based access control works (unauthorized routes redirect to 403)
- [ ] Session expiry redirects to login page
- [ ] CSRF token is sent on POST/PUT/DELETE requests

---

## Reference: Angular Project Structure

```
my-project-angular/
  .npmrc                          # npm registry config (if needed)
  angular.json                    # Angular CLI workspace config
  package.json                    # npm dependencies
  tsconfig.json                   # TypeScript config with @blossom/* paths
  pom.xml                         # Maven build with frontend-maven-plugin
  src/
    index.html                    # SPA entry point (base href="/blossom/ng/")
    styles.scss                   # Global styles (Angular Material theme)
    main.ts                       # Bootstrap
    app/
      app.config.ts               # Providers: router, http, interceptors, APP_INITIALIZER
      app.routes.ts               # All routes (standard + custom)
      app.component.ts            # Root component (<router-outlet>)
      features/
        home/                     # Home page
        profile/                  # User profile
        search/                   # Omnisearch
        admin-users/              # User CRUD
        admin-groups/             # Group CRUD
        admin-roles/              # Role CRUD + privilege tree
        admin-memberships/        # User-group associations
        admin-responsibilities/   # User-role associations
        content-articles/         # Article CRUD
        content-filemanager/      # File upload/list
        system-dashboard/         # Health/memory/JVM metrics
        system-caches/            # Cache management
        system-sessions/          # Session management
        system-loggers/           # Logger tree + level config
        system-scheduler/         # Job scheduler
        system-liquibase/         # DB migration history
        system-bpmn/              # BPMN process viewer
        my-feature/               # YOUR CUSTOM PAGES
  projects/
    blossom/
      core/                       # @blossom/core - services, guards, interceptors, models
      ui/                         # @blossom/ui - shared Material components
      shell/                      # @blossom/shell - layout, sidebar, topbar, login
```

---

## Reference: API Endpoints

All endpoints are under `/blossom/api/` and require authentication (session or HTTP Basic).

### Configuration & Auth

| Method | Path | Description |
|--------|------|-------------|
| GET | `/configuration` | App bootstrap: menu tree, current user, authorities, locales |
| POST | `/auth/login` | JSON login (`{ "username": "...", "password": "..." }`) |
| POST | `/auth/logout` | Invalidate session |
| GET | `/auth/current-user` | Current user info |
| GET | `/profile` | Current user profile |
| PUT | `/profile/password` | Change password |

### Administration

| Method | Path | Description |
|--------|------|-------------|
| GET/POST | `/administration/users` | List / Create users |
| GET/PUT/DELETE | `/administration/users/{id}` | Read / Update / Delete user |
| GET/POST | `/administration/groups` | List / Create groups |
| GET/PUT/DELETE | `/administration/groups/{id}` | Read / Update / Delete group |
| GET/POST | `/administration/roles` | List / Create roles |
| GET/PUT/DELETE | `/administration/roles/{id}` | Read / Update / Delete role |
| GET/POST/DELETE | `/administration/memberships` | User-group associations |
| GET/POST/DELETE | `/administration/responsabilities` | User-role associations |
| GET | `/administration/privileges` | Privilege tree (for role editing) |

### Content

| Method | Path | Description |
|--------|------|-------------|
| GET/POST | `/content/articles` | List / Create articles |
| GET/PUT/DELETE | `/content/articles/{id}` | Read / Update / Delete article |
| GET/POST | `/content/filemanager` | List files / Upload file |
| GET | `/content/filemanager/{id}` | Get file metadata |

### System

| Method | Path | Description |
|--------|------|-------------|
| GET | `/system/dashboard/status` | Health + uptime |
| GET | `/system/dashboard/memory` | Heap/non-heap memory |
| GET | `/system/dashboard/jvm` | Classes, threads, processors |
| GET | `/system/caches` | List caches with stats |
| POST | `/system/caches/{name}/_empty` | Clear cache |
| POST | `/system/caches/{name}/_enable` | Enable cache |
| POST | `/system/caches/{name}/_disable` | Disable cache |
| POST | `/system/caches/_empty` | Clear all caches |
| GET | `/system/sessions` | Active sessions + login attempts |
| POST | `/system/sessions/{id}/_invalidate` | Expire session |
| GET | `/system/loggers` | All loggers |
| GET | `/system/loggers/tree` | Logger hierarchy |
| POST | `/system/loggers/{name}/{level}` | Set log level |
| GET | `/system/scheduler` | Scheduler info + groups |
| GET | `/system/scheduler/{group}` | Jobs in group |
| POST | `/system/scheduler/{group}/{name}/_execute` | Execute job |
| POST | `/system/scheduler/_changeState` | Enable/disable scheduler |
| GET | `/system/liquibase` | Migration history |
| GET | `/system/bpmn` | Process definitions |

### Search

| Method | Path | Description |
|--------|------|-------------|
| GET | `/search?q=...&page=0&size=25` | Multi-entity search |

---

## Reference: Shared Components

### From `@blossom/core`

| Export | Type | Purpose |
|--------|------|---------|
| `ConfigurationService` | Service | Loads and caches app config (menu, user, authorities) |
| `AuthService` | Service | Login, logout, current user |
| `MenuService` | Service | Reactive menu tree from config |
| `NotificationService` | Service | Snackbar notifications (success/error/info) |
| `csrfInterceptor` | HttpInterceptorFn | CSRF token handling |
| `authErrorInterceptor` | HttpInterceptorFn | 401 -> redirect to login |
| `authGuard` | CanActivateFn | Requires loaded config (authenticated) |
| `privilegeGuard` | CanActivateFn | Checks `route.data.privilege` against user authorities |
| `AppConfiguration` | Interface | Menu, user, authorities, locales |
| `MenuItem` | Interface | Menu item shape |
| `UserInfo` | Interface | User profile shape |
| `Page<T>` | Interface | Spring Data page shape |

### From `@blossom/ui`

| Export | Type | Purpose |
|--------|------|---------|
| `BlossomTableComponent` | Component | Table with built-in search, pagination, sorting |
| `ConfirmDialogComponent` | Component | Confirm dialog (replaces SweetAlert) |
| `DeleteButtonComponent` | Component | Delete icon button with confirm dialog |
| `SearchBarComponent` | Component | Debounced search input |
| `BreadcrumbComponent` | Component | Breadcrumb navigation |
| `PrivilegeTreeComponent` | Component | Checkbox tree for privilege selection |

### From `@blossom/shell`

| Export | Type | Purpose |
|--------|------|---------|
| `LayoutComponent` | Component | Sidebar + topbar + router-outlet |
| `SidebarComponent` | Component | Menu-driven sidebar navigation |
| `TopbarComponent` | Component | User menu, logout |
| `LoginComponent` | Component | Login page |
| `ErrorPageComponent` | Component | Configurable error page (403, 404, 500) |

---

## Running Both UIs During Migration

During migration, you can run both UIs simultaneously:

- FreeMarker UI: `http://localhost:8080/blossom/` (if `blossom-starter-ui-web` is still in dependencies)
- Angular UI: `http://localhost:8080/blossom/ng/`

Both share the same session and API endpoints. This lets you migrate one page at a time and verify side-by-side.

To have both starters at the same time:

```xml
<dependencies>
  <!-- Keep during migration -->
  <dependency>
    <groupId>com.blossom-project</groupId>
    <artifactId>blossom-starter-ui-web</artifactId>
    <version>4.0.0-SNAPSHOT</version>
  </dependency>
  <!-- Add for Angular -->
  <dependency>
    <groupId>com.blossom-project</groupId>
    <artifactId>blossom-starter-ui-angular</artifactId>
    <version>4.0.0-SNAPSHOT</version>
  </dependency>
</dependencies>
```

Remove `blossom-starter-ui-web` once migration is complete.

---

## Troubleshooting

### CSRF errors on POST/PUT/DELETE

Make sure both interceptors are registered in `app.config.ts`:
```typescript
provideHttpClient(withInterceptors([csrfInterceptor, authErrorInterceptor]))
```

The server must be configured with `CookieCsrfTokenRepository.withHttpOnlyFalse()` -- this is the default in Blossom 4.x with `blossom-starter-ui-angular`.

### 401 on API calls after login

Session-based auth requires the browser to send cookies. Angular `HttpClient` does this automatically for same-origin requests. If your Angular dev server runs on a different port, configure a proxy:

```json
// proxy.conf.json
{
  "/blossom/api": {
    "target": "http://localhost:8080",
    "secure": false
  }
}
```

Run with: `ng serve --proxy-config proxy.conf.json`

### Menu items not appearing

1. Verify the `MenuItem` bean is registered (check Spring Boot logs for bean creation).
2. Verify the user has the required privilege for the menu item.
3. Check `GET /blossom/api/configuration` returns your menu item in the response.
4. Ensure the `link` value matches your Angular route path.

### Angular route not loading

1. Verify the route is registered in `app.routes.ts`.
2. Check the `loadChildren` import path matches your file location.
3. Check the browser console for lazy-loading errors.
4. Verify the `privilegeGuard` is not blocking access (check `data.privilege` matches a privilege the user has).

### Build fails with "Cannot find module '@blossom/core'"

Ensure your `tsconfig.json` has the correct `paths` entries pointing to either:
- The library source files (`projects/blossom/core/src/public-api.ts`), or
- The built library dist (`dist/blossom/core`)

### npm install fails with authentication error

Create an `.npmrc` file in your Angular workspace root:
```
registry=https://registry.npmjs.org/
```
