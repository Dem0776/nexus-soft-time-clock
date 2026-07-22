import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, tap } from 'rxjs';

import { environment } from '../../../environments/environment';
import { LoginRequest, Me, TokenResponse } from '../models/auth.models';
import { AuthStore } from './auth.store';

/** Cliente HTTP de autenticación contra la API v1 (login, refresh, logout, me). */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly store = inject(AuthStore);
  private readonly base = environment.apiBaseUrl;

  login(request: LoginRequest): Observable<TokenResponse> {
    return this.http
      .post<TokenResponse>(`${this.base}/auth/login`, request)
      .pipe(tap((tokens) => this.store.setTokens(tokens)));
  }

  refresh(): Observable<TokenResponse> {
    const refreshToken = this.store.refreshTokenValue();
    return this.http
      .post<TokenResponse>(`${this.base}/auth/refresh`, { refreshToken })
      .pipe(tap((tokens) => this.store.setTokens(tokens)));
  }

  me(): Observable<Me> {
    return this.http.get<Me>(`${this.base}/auth/me`);
  }

  /** Carga la identidad actual y la cachea en el store (para el layout y RBAC de la UI). */
  loadCurrentUser(): Observable<Me> {
    return this.me().pipe(tap((user) => this.store.setUser(user)));
  }

  logout(): void {
    const refreshToken = this.store.refreshTokenValue();
    if (refreshToken) {
      this.http.post(`${this.base}/auth/logout`, { refreshToken }).subscribe({ error: () => void 0 });
    }
    this.store.clear();
  }
}
