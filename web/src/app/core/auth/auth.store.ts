import { Injectable, computed, signal } from '@angular/core';
import { Me, TokenResponse } from '../models/auth.models';

const ACCESS_KEY = 'nexus.accessToken';
const REFRESH_KEY = 'nexus.refreshToken';

/**
 * Estado de sesión basado en Signals. Persiste los tokens en localStorage para
 * sobrevivir recargas (PWA). El access token se envía en cada petición vía interceptor;
 * la identidad del usuario (/me) se cachea en memoria para el layout y el control RBAC de la UI.
 */
@Injectable({ providedIn: 'root' })
export class AuthStore {
  private readonly _accessToken = signal<string | null>(localStorage.getItem(ACCESS_KEY));
  private readonly _refreshToken = signal<string | null>(localStorage.getItem(REFRESH_KEY));
  private readonly _user = signal<Me | null>(null);

  readonly accessToken = this._accessToken.asReadonly();
  readonly user = this._user.asReadonly();
  readonly isAuthenticated = computed(() => this._accessToken() !== null);

  refreshTokenValue(): string | null {
    return this._refreshToken();
  }

  setTokens(tokens: TokenResponse): void {
    this._accessToken.set(tokens.accessToken);
    this._refreshToken.set(tokens.refreshToken);
    localStorage.setItem(ACCESS_KEY, tokens.accessToken);
    localStorage.setItem(REFRESH_KEY, tokens.refreshToken);
  }

  setUser(user: Me | null): void {
    this._user.set(user);
  }

  /** Control de acceso fino en la UI (los endpoints ya lo imponen en el backend). */
  hasPermission(permission: string): boolean {
    return this._user()?.permissions.includes(permission) ?? false;
  }

  clear(): void {
    this._accessToken.set(null);
    this._refreshToken.set(null);
    this._user.set(null);
    localStorage.removeItem(ACCESS_KEY);
    localStorage.removeItem(REFRESH_KEY);
  }
}
