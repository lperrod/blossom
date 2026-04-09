import { inject } from '@angular/core';
import { CanActivateFn, Router, ActivatedRouteSnapshot } from '@angular/router';
import { ConfigurationService } from '../services/configuration.service';

export const privilegeGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const configService = inject(ConfigurationService);
  const router = inject(Router);
  const requiredPrivilege = route.data['privilege'] as string;

  if (!configService.config) {
    return router.createUrlTree(['/login']);
  }

  if (!requiredPrivilege || configService.config.authorities.includes(requiredPrivilege)) {
    return true;
  }

  return router.createUrlTree(['/403']);
};
