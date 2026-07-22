/// Modelos de autenticación (plain Dart; se migrarán a Freezed con codegen).

class Credentials {
  const Credentials({required this.email, required this.password, this.companyCode});

  final String email;
  final String password;
  final String? companyCode;

  Map<String, dynamic> toJson() => {
        'email': email,
        'password': password,
        if (companyCode != null && companyCode!.isNotEmpty) 'companyCode': companyCode,
      };
}

class TokenResponse {
  const TokenResponse({
    required this.accessToken,
    required this.refreshToken,
    required this.tokenType,
    required this.expiresIn,
  });

  final String accessToken;
  final String refreshToken;
  final String tokenType;
  final int expiresIn;

  factory TokenResponse.fromJson(Map<String, dynamic> json) => TokenResponse(
        accessToken: json['accessToken'] as String,
        refreshToken: json['refreshToken'] as String,
        tokenType: json['tokenType'] as String? ?? 'Bearer',
        expiresIn: (json['expiresIn'] as num).toInt(),
      );
}

class Me {
  const Me({
    required this.userId,
    required this.tenantId,
    required this.platformAdmin,
    required this.roles,
    required this.permissions,
  });

  final String userId;
  final String? tenantId;
  final bool platformAdmin;
  final List<String> roles;
  final List<String> permissions;

  factory Me.fromJson(Map<String, dynamic> json) => Me(
        userId: json['userId'] as String,
        tenantId: json['tenantId'] as String?,
        platformAdmin: json['platformAdmin'] as bool? ?? false,
        roles: (json['roles'] as List<dynamic>? ?? []).cast<String>(),
        permissions: (json['permissions'] as List<dynamic>? ?? []).cast<String>(),
      );
}
