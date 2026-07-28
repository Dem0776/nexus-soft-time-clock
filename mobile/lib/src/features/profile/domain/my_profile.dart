/// Perfil propio del colaborador (solo lectura en la app). Espejo de MeProfileResponse.
class MyProfile {
  const MyProfile({
    required this.userId,
    this.fullName,
    this.email,
    this.employeeCode,
    this.birthDate,
    this.hireDate,
    this.gender,
    this.phone,
    this.address,
    this.emergencyContactName,
    this.emergencyContactPhone,
  });

  final String userId;
  final String? fullName;
  final String? email;
  final String? employeeCode;
  final String? birthDate;
  final String? hireDate;
  final String? gender;
  final String? phone;
  final String? address;
  final String? emergencyContactName;
  final String? emergencyContactPhone;

  factory MyProfile.fromJson(Map<String, dynamic> j) => MyProfile(
        userId: j['userId'] as String? ?? '',
        fullName: j['fullName'] as String?,
        email: j['email'] as String?,
        employeeCode: j['employeeCode'] as String?,
        birthDate: j['birthDate'] as String?,
        hireDate: j['hireDate'] as String?,
        gender: j['gender'] as String?,
        phone: j['phone'] as String?,
        address: j['address'] as String?,
        emergencyContactName: j['emergencyContactName'] as String?,
        emergencyContactPhone: j['emergencyContactPhone'] as String?,
      );
}
