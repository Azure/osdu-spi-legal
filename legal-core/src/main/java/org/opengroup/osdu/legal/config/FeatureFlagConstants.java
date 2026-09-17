package org.opengroup.osdu.legal.config;

/**
 * Constants for feature flag configuration properties.
 */
public final class FeatureFlagConstants {

  /**
   * Configuration property key for controlling feature flag exposure in version info API.
   * When set to true (default), feature flag states are included in the /info endpoint response.
   * When set to false, feature flag states are omitted from the response.
   * When it is missing, default will be true.
   */
  public static final String EXPOSE_FEATUREFLAG_ENABLED_PROPERTY = "expose_featureflag.enabled";

  /**
   * Private constructor to prevent instantiation of this utility class.
   */
  private FeatureFlagConstants() {
  }
}
