/**
 *  Contributed by Sandeep Kumar.
 */
package com.swe.ux.model;

import java.awt.image.BufferedImage;

public record UIImage(
    BufferedImage image,
    String ip
) {}
