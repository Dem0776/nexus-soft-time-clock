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

export interface QrRequest {
  ttlMinutes?: number;
}

export interface QrToken {
  token: string;
  expiresAt: string;
}
