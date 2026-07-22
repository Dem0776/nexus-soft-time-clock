import 'dart:convert';

/// Extrae el `workSiteId` del QR firmado del centro (ADR-006). El token tiene el
/// formato `base64url(body).base64url(sig)` donde `body = tenantId|workSiteId|nonce|exp`.
/// Aquí solo se decodifica el cuerpo para conocer el centro; la firma y la vigencia las
/// valida el servidor. Devuelve `null` si el formato no es reconocible.
String? workSiteIdFromQrToken(String rawToken) {
  final dot = rawToken.indexOf('.');
  if (dot <= 0) {
    return null;
  }
  try {
    final body = utf8.decode(base64Url.decode(base64Url.normalize(rawToken.substring(0, dot))));
    final parts = body.split('|');
    if (parts.length != 4 || parts[1].isEmpty) {
      return null;
    }
    return parts[1];
  } catch (_) {
    return null;
  }
}
