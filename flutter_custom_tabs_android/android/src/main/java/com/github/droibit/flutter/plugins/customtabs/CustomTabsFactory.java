package com.github.droibit.flutter.plugins.customtabs;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Browser;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.browser.customtabs.CustomTabColorSchemeParams;
import androidx.browser.customtabs.CustomTabsIntent;

import com.droibit.android.customtabs.launcher.CustomTabsPackageFallback;
import com.droibit.android.customtabs.launcher.NonChromeCustomTabs;

import java.util.List;
import java.util.Map;

import static com.droibit.android.customtabs.launcher.CustomTabsIntentHelper.ensureCustomTabsPackage;
import static com.github.droibit.flutter.plugins.customtabs.ResourceFactory.INVALID_RESOURCE_ID;
import static com.github.droibit.flutter.plugins.customtabs.ResourceFactory.resolveAnimationIdentifier;
import static com.github.droibit.flutter.plugins.customtabs.ResourceFactory.resolveDrawableIdentifier;

@SuppressWarnings({"ConstantConditions", "unchecked"})
@RestrictTo(RestrictTo.Scope.LIBRARY)
class CustomTabsFactory {
    private static final String KEY_OPTIONS_COLOR_SCHEMES = "colorSchemes";
    private static final String KEY_COLOR_SCHEMES_COLOR_SCHEME = "colorScheme";
    private static final String KEY_LIGHT_COLOR_SCHEME_PARAMS = "lightColorSchemeParams";
    private static final String KEY_DARK_COLOR_SCHEME_PARAMS = "darkColorSchemeParams";
    private static final String KEY_DEFAULT_COLOR_SCHEME_PARAMS = "defaultColorSchemeParams";
    private static final String KEY_COLOR_SCHEME_PARAMS_TOOLBAR_COLOR = "toolbarColor";
    private static final String KEY_COLOR_SCHEME_PARAMS_NAVIGATION_BAR_COLOR = "navigationBarColor";
    private static final String KEY_COLOR_SCHEME_PARAMS_NAVIGATION_BAR_DIVIDER_COLOR = "navigationBarDividerColor";
    private static final String KEY_OPTIONS_URL_BAR_HIDING_ENABLED = "urlBarHidingEnabled";
    private static final String KEY_OPTIONS_SHOW_TITLE = "showTitle";
    private static final String KEY_OPTIONS_SHARE_STATE = "shareState";
    private static final String KEY_INSTANT_APPS_ENABLED = "instantAppsEnabled";
    private static final String KEY_OPTIONS_ANIMATIONS = "animations";
    private static final String KEY_CLOSE_BUTTON_POSITION = "closeButtonPosition";
    private static final String KEY_CLOSE_BUTTON_ICON = "closeButtonIcon";
    private static final String KEY_HEADERS = "headers";
    private static final String KEY_ANIMATION_START_ENTER = "startEnter";
    private static final String KEY_ANIMATION_START_EXIT = "startExit";
    private static final String KEY_ANIMATION_END_ENTER = "endEnter";
    private static final String KEY_ANIMATION_END_EXIT = "endExit";
    private static final String KEY_OPTIONS_EXTRA_CUSTOM_TABS = "extraCustomTabs";
    private static final String KEY_OPTIONS_PARTIAL_CUSTOM_TABS = "partial";
    private static final String KEY_BOTTOM_SHEET_INITIAL_HEIGHT_DP = "initialHeightDp";
    private static final String KEY_BOTTOM_SHEET_ACTIVITY_HEIGHT_RESIZE_BEHAVIOR = "activityHeightResizeBehavior";
    private static final String KEY_BOTTOM_SHEET_CORNER_RADIUS_DP = "cornerRadiusDp";

    private final Context context;

    CustomTabsFactory(@NonNull Context context) {
        this.context = context;
    }

    @NonNull
    CustomTabsIntent createIntent(@NonNull Map<String, Object> options) {
        final CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        if (options.containsKey(KEY_OPTIONS_COLOR_SCHEMES)) {
            final Map<String, Object> colorSchemes =
                    (Map<String, Object>) options.get(KEY_OPTIONS_COLOR_SCHEMES);
            applyColorSchemes(builder, colorSchemes);
        }

        if (options.containsKey(KEY_CLOSE_BUTTON_ICON)) {
            final int closeButtonIconId =
                    resolveDrawableIdentifier(context, ((String) options.get(KEY_CLOSE_BUTTON_ICON)));
            if (closeButtonIconId != INVALID_RESOURCE_ID) {
                final Bitmap closeButtonIcon = ResourceFactory.getBitmap(context, closeButtonIconId);
                if (closeButtonIcon != null) {
                    builder.setCloseButtonIcon(closeButtonIcon);
                }
            }
        }
        if (options.containsKey(KEY_CLOSE_BUTTON_POSITION)) {
            final int position = (int) options.get(KEY_CLOSE_BUTTON_POSITION);
            builder.setCloseButtonPosition(position);
        }

        if (options.containsKey(KEY_OPTIONS_URL_BAR_HIDING_ENABLED)) {
            builder.setUrlBarHidingEnabled(((Boolean) options.get(KEY_OPTIONS_URL_BAR_HIDING_ENABLED)));
        }

        if (options.containsKey(KEY_OPTIONS_SHARE_STATE)) {
            final int shareState = ((int) options.get(KEY_OPTIONS_SHARE_STATE));
            builder.setShareState(shareState);
        }

        if (options.containsKey(KEY_OPTIONS_SHOW_TITLE)) {
            builder.setShowTitle(((Boolean) options.get(KEY_OPTIONS_SHOW_TITLE)));
        }

        if (options.containsKey(KEY_INSTANT_APPS_ENABLED)) {
            builder.setInstantAppsEnabled(((Boolean) options.get(KEY_INSTANT_APPS_ENABLED)));
        }

        if (options.containsKey(KEY_OPTIONS_ANIMATIONS)) {
            applyAnimations(builder, ((Map<String, String>) options.get(KEY_OPTIONS_ANIMATIONS)));
        }

        if (options.containsKey(KEY_OPTIONS_PARTIAL_CUSTOM_TABS)) {
            final Map<String, Object> partialCustomTabsConfig =
                    ((Map<String, Object>) options.get(KEY_OPTIONS_PARTIAL_CUSTOM_TABS));
            applyPartialCustomTabsConfiguration(builder, partialCustomTabsConfig);
        }

        final CustomTabsIntent customTabsIntent = builder.build();
        onPostBuild(customTabsIntent, options);
        return customTabsIntent;
    }

    private void onPostBuild(
            @NonNull CustomTabsIntent customTabsIntent,
            @NonNull Map<String, Object> options
    ) {
        if (options.containsKey(KEY_HEADERS)) {
            final Map<String, String> headers = (Map<String, String>) options.get(KEY_HEADERS);
            final Bundle bundleHeaders = new Bundle();
            for (Map.Entry<String, String> header : headers.entrySet()) {
                bundleHeaders.putString(header.getKey(), header.getValue());
            }
            customTabsIntent.intent.putExtra(Browser.EXTRA_HEADERS, bundleHeaders);
        }

        final List<String> extraCustomTabs;
        if (options.containsKey(KEY_OPTIONS_EXTRA_CUSTOM_TABS)) {
            extraCustomTabs = ((List<String>) options.get(KEY_OPTIONS_EXTRA_CUSTOM_TABS));
        } else {
            extraCustomTabs = null;
        }

        final CustomTabsPackageFallback fallback;
        if (extraCustomTabs != null && !extraCustomTabs.isEmpty()) {
            fallback = new NonChromeCustomTabs(extraCustomTabs);
        } else {
            fallback = new NonChromeCustomTabs(context);
        }
        ensureCustomTabsPackage(customTabsIntent, context, fallback);
    }

    void applyColorSchemes(
            @NonNull CustomTabsIntent.Builder builder,
            @NonNull Map<String, Object> colorSchemes
    ) {
        if (colorSchemes.containsKey(KEY_COLOR_SCHEMES_COLOR_SCHEME)) {
            final int colorScheme = (int) colorSchemes.get(KEY_COLOR_SCHEMES_COLOR_SCHEME);
            builder.setColorScheme(colorScheme);
        }

        if (colorSchemes.containsKey(KEY_LIGHT_COLOR_SCHEME_PARAMS)) {
            final Map<String, String> colorSchemeParams =
                    (Map<String, String>) colorSchemes.get(KEY_LIGHT_COLOR_SCHEME_PARAMS);
            builder.setColorSchemeParams(
                    CustomTabsIntent.COLOR_SCHEME_LIGHT,
                    buildColorSchemeParams(colorSchemeParams)
            );
        }
        if (colorSchemes.containsKey(KEY_DARK_COLOR_SCHEME_PARAMS)) {
            final Map<String, String> colorSchemeParams =
                    (Map<String, String>) colorSchemes.get(KEY_DARK_COLOR_SCHEME_PARAMS);
            builder.setColorSchemeParams(
                    CustomTabsIntent.COLOR_SCHEME_DARK,
                    buildColorSchemeParams(colorSchemeParams)
            );
        }
        if (colorSchemes.containsKey(KEY_DEFAULT_COLOR_SCHEME_PARAMS)) {
            final Map<String, String> colorSchemeParams =
                    (Map<String, String>) colorSchemes.get(KEY_DEFAULT_COLOR_SCHEME_PARAMS);
            builder.setDefaultColorSchemeParams(buildColorSchemeParams(colorSchemeParams));
        }
    }

    @NonNull
    CustomTabColorSchemeParams buildColorSchemeParams(@NonNull Map<String, String> source) {
        final CustomTabColorSchemeParams.Builder builder = new CustomTabColorSchemeParams.Builder();
        if (source.containsKey(KEY_COLOR_SCHEME_PARAMS_TOOLBAR_COLOR)) {
            final String colorString = source.get(KEY_COLOR_SCHEME_PARAMS_TOOLBAR_COLOR);
            builder.setToolbarColor(Color.parseColor(colorString));
        }
        if (source.containsKey(KEY_COLOR_SCHEME_PARAMS_NAVIGATION_BAR_COLOR)) {
            final String colorString = source.get(KEY_COLOR_SCHEME_PARAMS_NAVIGATION_BAR_COLOR);
            builder.setNavigationBarColor(Color.parseColor(colorString));
        }
        if (source.containsKey(KEY_COLOR_SCHEME_PARAMS_NAVIGATION_BAR_DIVIDER_COLOR)) {
            final String colorString = source.get(KEY_COLOR_SCHEME_PARAMS_NAVIGATION_BAR_DIVIDER_COLOR);
            builder.setNavigationBarDividerColor(Color.parseColor(colorString));
        }
        return builder.build();
    }

    private void applyAnimations(
            @NonNull CustomTabsIntent.Builder builder,
            @NonNull Map<String, String> animations
    ) {
        final int startEnterAnimationId =
                resolveAnimationIdentifier(context, animations.get(KEY_ANIMATION_START_ENTER));
        final int startExitAnimationId =
                resolveAnimationIdentifier(context, animations.get(KEY_ANIMATION_START_EXIT));
        final int endEnterAnimationId =
                resolveAnimationIdentifier(context, animations.get(KEY_ANIMATION_END_ENTER));
        final int endExitAnimationId =
                resolveAnimationIdentifier(context, animations.get(KEY_ANIMATION_END_EXIT));

        if (startEnterAnimationId != INVALID_RESOURCE_ID
                && startExitAnimationId != INVALID_RESOURCE_ID) {
            builder.setStartAnimations(context, startEnterAnimationId, startExitAnimationId);
        }

        if (endEnterAnimationId != INVALID_RESOURCE_ID
                && endExitAnimationId != INVALID_RESOURCE_ID) {
            builder.setExitAnimations(context, endEnterAnimationId, endExitAnimationId);
        }
    }

    private void applyPartialCustomTabsConfiguration(
            @NonNull CustomTabsIntent.Builder builder,
            @NonNull Map<String, Object> configuration
    ) {
        final double initialHeightDp = (double) configuration.get(KEY_BOTTOM_SHEET_INITIAL_HEIGHT_DP);
        final float scale = context.getResources().getDisplayMetrics().density;
        final int resizeBehavior = (int) configuration.get(KEY_BOTTOM_SHEET_ACTIVITY_HEIGHT_RESIZE_BEHAVIOR);
        builder.setInitialActivityHeightPx(
                (int) (initialHeightDp * scale + 0.5),
                resizeBehavior
        );
        if (configuration.containsKey(KEY_BOTTOM_SHEET_CORNER_RADIUS_DP)) {
            final int cornerRadius = ((int) configuration.get(KEY_BOTTOM_SHEET_CORNER_RADIUS_DP));
            builder.setToolbarCornerRadiusDp(cornerRadius);
        }
    }
}
