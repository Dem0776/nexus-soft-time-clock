import 'dart:io';

import 'package:crypto/crypto.dart';
import 'package:flutter_image_compress/flutter_image_compress.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:image_picker/image_picker.dart';
import 'package:path/path.dart' as p;
import 'package:path_provider/path_provider.dart';

/// Foto capturada y lista para subir, ya comprimida.
class CapturedEvidence {
  const CapturedEvidence({
    required this.file,
    required this.sha256,
    required this.sizeBytes,
    required this.contentType,
  });

  final File file;
  final String sha256;
  final int sizeBytes;
  final String contentType;
}

/// Captura la evidencia fotográfica de una marcación (RF-18, HU-13).
///
/// La foto se guarda en el directorio de documentos, no en temporales: una marcación creada sin
/// conexión puede tardar horas en sincronizarse y el sistema limpia los temporales cuando quiere.
class EvidenceCaptureService {
  EvidenceCaptureService(this._picker);

  final ImagePicker _picker;

  static const int _maxDimension = 1280;
  static const int _quality = 70;
  static const String contentType = 'image/jpeg';

  /// Abre la cámara y devuelve la foto comprimida, o `null` si el usuario cancela.
  Future<CapturedEvidence?> capture(String operationUuid) async {
    final shot = await _picker.pickImage(
      source: ImageSource.camera,
      preferredCameraDevice: CameraDevice.front,
      imageQuality: 90, // primera reducción; la compresión fina va después
    );
    if (shot == null) {
      return null;
    }

    final dir = Directory(p.join((await getApplicationDocumentsDirectory()).path, 'evidence'));
    if (!dir.existsSync()) {
      await dir.create(recursive: true);
    }
    final target = p.join(dir.path, '$operationUuid.jpg');

    final compressed = await FlutterImageCompress.compressAndGetFile(
      shot.path,
      target,
      quality: _quality,
      minWidth: _maxDimension,
      minHeight: _maxDimension,
      format: CompressFormat.jpeg,
    );

    // Si la compresión falla (formato inesperado del fabricante), se usa el original: es
    // preferible subir una foto grande a dejar al colaborador sin poder registrar.
    final file = compressed == null ? await File(shot.path).copy(target) : File(compressed.path);
    final bytes = await file.readAsBytes();

    return CapturedEvidence(
      file: file,
      sha256: sha256.convert(bytes).toString(),
      sizeBytes: bytes.length,
      contentType: contentType,
    );
  }
}

final evidenceCaptureServiceProvider =
    Provider<EvidenceCaptureService>((ref) => EvidenceCaptureService(ImagePicker()));
