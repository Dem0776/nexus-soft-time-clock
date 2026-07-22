import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';

import { AuthService } from '../../core/auth/auth.service';

/** Pantalla de inicio de sesión (RF-01): layout partido con panel de marca y formulario. */
@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
  ],
  styles: [
    `
      .auth { display: flex; min-height: 100vh; }
      .hero {
        flex: 1 1 0;
        background: linear-gradient(150deg, #3949ab 0%, #283593 55%, #1a237e 100%);
        color: #fff;
        padding: 56px;
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        position: relative;
        overflow: hidden;
      }
      .hero::after {
        content: '';
        position: absolute;
        right: -120px;
        bottom: -120px;
        width: 360px;
        height: 360px;
        border-radius: 50%;
        background: rgba(255, 255, 255, 0.08);
      }
      .hero .brand { display: flex; align-items: center; gap: 12px; }
      .hero .logo { width: 42px; height: 42px; }
      .hero h1 { font-size: 2.4rem; margin: 0 0 12px; line-height: 1.1; }
      .hero p { font-size: 1.05rem; opacity: 0.85; max-width: 460px; }
      .hero .foot { font-size: 0.8rem; opacity: 0.7; }
      .features { display: flex; flex-direction: column; gap: 10px; margin-top: 20px; }
      .features div { display: flex; align-items: center; gap: 10px; opacity: 0.92; }

      .panel {
        flex: 0 0 clamp(360px, 40%, 520px);
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 32px;
        background: var(--surface);
      }
      .card { width: 100%; max-width: 360px; }
      .card h2 { margin: 0 0 4px; font-size: 1.5rem; font-weight: 700; }
      .card .sub { color: var(--text-muted); margin: 0 0 24px; }

      @media (max-width: 900px) {
        .hero { display: none; }
        .panel { flex: 1 1 auto; }
      }
    `,
  ],
  template: `
    <div class="auth">
      <div class="hero">
        <div class="brand">
          <svg class="logo" viewBox="0 0 64 64" aria-hidden="true">
            <rect width="64" height="64" rx="16" fill="rgba(255,255,255,.12)" />
            <circle cx="32" cy="32" r="18" fill="none" stroke="#fff" stroke-width="3.5" />
            <path d="M32 22v11l7 5" fill="none" stroke="#fff" stroke-width="3.5" stroke-linecap="round" stroke-linejoin="round" />
          </svg>
          <strong style="font-size:1.2rem">Nexus Soft Time Clock</strong>
        </div>
        <div>
          <h1>Control de asistencia<br />sin fricción.</h1>
          <p>Registro por QR + GPS con geocercas y antifraude, visibilidad en tiempo real y reportería lista para tu operación.</p>
          <div class="features">
            <div><mat-icon>verified_user</mat-icon> Multi-empresa y seguro por diseño</div>
            <div><mat-icon>my_location</mat-icon> Geocercas y validación GPS</div>
            <div><mat-icon>insights</mat-icon> Dashboards y reportes exportables</div>
          </div>
        </div>
        <div class="foot">© Nexus Soft — Plataforma de asistencia empresarial</div>
      </div>

      <div class="panel">
        <div class="card">
          @if (loading()) { <mat-progress-bar mode="indeterminate" style="margin-bottom:16px" /> }
          <h2>Iniciar sesión</h2>
          <p class="sub">Ingresá con tus credenciales corporativas.</p>
          <form [formGroup]="form" (ngSubmit)="submit()">
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Correo</mat-label>
              <input matInput type="email" formControlName="email" autocomplete="username" />
              <mat-icon matSuffix>mail</mat-icon>
            </mat-form-field>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Contraseña</mat-label>
              <input matInput type="password" formControlName="password" autocomplete="current-password" />
              <mat-icon matSuffix>lock</mat-icon>
            </mat-form-field>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Empresa (opcional)</mat-label>
              <input matInput formControlName="companyCode" />
              <mat-icon matSuffix>business</mat-icon>
            </mat-form-field>

            @if (error()) { <p class="error-text">{{ error() }}</p> }

            <button mat-flat-button color="primary" type="submit" class="full-width"
                    [disabled]="form.invalid || loading()" style="height:44px">
              Entrar
            </button>
          </form>
        </div>
      </div>
    </div>
  `,
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]],
    companyCode: [''],
  });

  protected submit(): void {
    if (this.form.invalid) {
      return;
    }
    this.loading.set(true);
    this.error.set(null);
    const { email, password, companyCode } = this.form.getRawValue();
    this.authService.login({ email, password, companyCode: companyCode || undefined }).subscribe({
      next: () => {
        this.loading.set(false);
        void this.router.navigate(['/dashboard']);
      },
      error: () => {
        this.loading.set(false);
        this.error.set('Usuario o contraseña incorrectos.');
      },
    });
  }
}
