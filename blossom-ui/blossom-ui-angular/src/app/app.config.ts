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
