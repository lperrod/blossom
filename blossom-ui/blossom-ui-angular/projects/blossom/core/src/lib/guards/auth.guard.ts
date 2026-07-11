import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { ConfigurationService } from '../services/configuration.service';

export const authGuard: CanActivateFn = () => {
  const configService = inject(ConfigurationService);
  const router = inject(Router);

  if (configService.config()) {
    return true;
  }
  return router.createUrlTree(['/login']);
};
