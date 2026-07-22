import { Injectable, signal } from '@angular/core';

const THEME_KEY = 'nexus.theme';

export type Theme = 'light' | 'dark';

/**
 * Tema claro/oscuro (RNF-17). Persiste la preferencia en localStorage y la aplica
 * conmutando la clase `dark` en <body> (el tema oscuro de Material se activa bajo `body.dark`).
 */
@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly _theme = signal<Theme>((localStorage.getItem(THEME_KEY) as Theme | null) ?? 'light');
  readonly theme = this._theme.asReadonly();

  constructor() {
    this.apply(this._theme());
  }

  toggle(): void {
    this.set(this._theme() === 'dark' ? 'light' : 'dark');
  }

  set(theme: Theme): void {
    this._theme.set(theme);
    localStorage.setItem(THEME_KEY, theme);
    this.apply(theme);
  }

  private apply(theme: Theme): void {
    document.body.classList.toggle('dark', theme === 'dark');
  }
}
