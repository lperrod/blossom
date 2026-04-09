import { HttpInterceptorFn } from '@angular/common/http';

export const csrfInterceptor: HttpInterceptorFn = (req, next) => {
  const csrfToken = getCookie('XSRF-TOKEN');
  if (csrfToken && !['GET', 'HEAD', 'OPTIONS'].includes(req.method)) {
    req = req.clone({
      setHeaders: { 'X-XSRF-TOKEN': csrfToken }
    });
  }
  return next(req);
};

function getCookie(name: string): string | null {
  const match = document.cookie.match(new RegExp('(^| )' + name + '=([^;]+)'));
  return match ? decodeURIComponent(match[2]) : null;
}
