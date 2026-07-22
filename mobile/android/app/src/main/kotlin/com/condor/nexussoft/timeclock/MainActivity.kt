package com.condor.nexussoft.timeclock

import io.flutter.embedding.android.FlutterFragmentActivity

/**
 * Actividad principal. Se extiende de [FlutterFragmentActivity] (no de FlutterActivity)
 * porque el plugin local_auth requiere una FragmentActivity para mostrar el BiometricPrompt.
 */
class MainActivity : FlutterFragmentActivity()
