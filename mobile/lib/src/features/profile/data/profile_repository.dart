import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/dio_provider.dart';
import '../domain/my_profile.dart';

/// Lee el perfil propio del colaborador (GET /me/profile). Solo lectura.
class ProfileRepository {
  ProfileRepository(this._dio);
  final Dio _dio;

  Future<MyProfile> me() async {
    final res = await _dio.get<Map<String, dynamic>>('/me/profile');
    return MyProfile.fromJson(res.data!);
  }
}

final profileRepositoryProvider =
    Provider<ProfileRepository>((ref) => ProfileRepository(ref.read(dioProvider)));

final myProfileProvider =
    FutureProvider<MyProfile>((ref) => ref.read(profileRepositoryProvider).me());
