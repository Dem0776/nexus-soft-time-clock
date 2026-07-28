export interface Geofence {
  workSiteId: string;
  latitude: number;
  longitude: number;
  radiusM: number;
  active: boolean;
}

export interface GeofenceRequest {
  latitude: number;
  longitude: number;
  radiusM: number;
}

/** Si se envía `expiresAt`, tiene prioridad sobre `ttlMinutes` (vigencia por fecha exacta). */
export interface QrRequest {
  ttlMinutes?: number;
  expiresAt?: string;
}

export interface QrToken {
  token: string;
  expiresAt: string;
}
