import { Component, inject, signal } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';

import { PageHeaderComponent } from '../../core/ui/page-header.component';
import { AppNotification } from './notification.models';
import { NotificationsService } from './notifications.service';

/** Centro de notificaciones del usuario (RF-27). Listado; las preferencias se abordan luego. */
@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [MatCardModule, MatListModule, MatIconModule, MatPaginatorModule, MatProgressBarModule, PageHeaderComponent],
  template: `
    <app-page-header title="Notificaciones" />
    <mat-card>
      <mat-card-content>
        @if (loading()) { <mat-progress-bar mode="indeterminate" /> }
        @if (error()) { <p class="error-text">{{ error() }}</p> }
        @if (!loading() && notifications().length === 0) {
          <p class="muted">No tenés notificaciones.</p>
        }

        <mat-list>
          @for (n of notifications(); track n.id) {
            <mat-list-item>
              <mat-icon matListItemIcon>{{ n.status === 'READ' ? 'mark_email_read' : 'mark_email_unread' }}</mat-icon>
              <span matListItemTitle>{{ n.title }}</span>
              <span matListItemLine>{{ n.body }}</span>
              <span matListItemMeta class="muted">{{ n.channel }}</span>
            </mat-list-item>
          }
        </mat-list>

        <mat-paginator
          [length]="total()"
          [pageSize]="size()"
          [pageIndex]="page()"
          [pageSizeOptions]="[10, 20, 50]"
          (page)="onPage($event)"
        />
      </mat-card-content>
    </mat-card>
  `,
})
export class NotificationsComponent {
  private readonly service = inject(NotificationsService);

  protected readonly notifications = signal<AppNotification[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly page = signal(0);
  protected readonly size = signal(20);
  protected readonly total = signal(0);

  constructor() {
    this.reload();
  }

  protected reload(): void {
    this.loading.set(true);
    this.error.set(null);
    this.service.mine(this.page(), this.size()).subscribe({
      next: (result) => {
        this.notifications.set(result.content);
        this.total.set(result.totalElements);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No se pudieron cargar las notificaciones.');
        this.loading.set(false);
      },
    });
  }

  protected onPage(event: PageEvent): void {
    this.page.set(event.pageIndex);
    this.size.set(event.pageSize);
    this.reload();
  }
}
