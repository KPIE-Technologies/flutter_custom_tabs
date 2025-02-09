import 'package:flutter/foundation.dart';
import 'package:flutter_custom_tabs_platform_interface/flutter_custom_tabs_platform_interface.dart';

extension BrightnessToColorScheme on Brightness {
  /// Converts the [Brightness] enum to the corresponding [CustomTabsColorScheme].
  CustomTabsColorScheme toColorScheme() {
    switch (this) {
      case Brightness.dark:
        return CustomTabsColorScheme.dark;
      case Brightness.light:
        return CustomTabsColorScheme.light;
    }
  }
}
