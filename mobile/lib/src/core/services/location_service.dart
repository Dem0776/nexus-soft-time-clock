import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:geolocator/geolocator.dart';

/// Captura la posición GPS y expone señales antifraude locales (mock location, GPS off).
class LocationService {
  Future<bool> isGpsEnabled() => Geolocator.isLocationServiceEnabled();

  Future<Position?> current() async {
    if (!await Geolocator.isLocationServiceEnabled()) {
      return null;
    }
    var permission = await Geolocator.checkPermission();
    if (permission == LocationPermission.denied) {
      permission = await Geolocator.requestPermission();
    }
    if (permission == LocationPermission.denied || permission == LocationPermission.deniedForever) {
      return null;
    }
    return Geolocator.getCurrentPosition(
      locationSettings: const LocationSettings(accuracy: LocationAccuracy.best),
    );
  }
}

final locationServiceProvider = Provider<LocationService>((ref) => LocationService());
