/*
 * Contributed by Sandeep Kumar.
 */

package com.swe.ux.model;

import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * Represents an image with metadata for UI display.
 */
public final class UIImage {

    private static final byte SUCCESS_TRUE = 1;
    private static final byte SUCCESS_FALSE = 0;

    /** The buffered image. */
    private final BufferedImage image;

    /** The IP address. */
    private final String ip;

    /** The data rate. */
    private final long dataRate;

    /** Success flag as byte. */
    private byte isSuccess;

    /**
     * Creates a new UI image.
     *
     * @param img the buffered image
     * @param ipAddress the IP address
     * @param rate the data rate
     * @param success success flag
     */
    public UIImage(
        final BufferedImage img,
        final String ipAddress,
        final long rate,
        final byte success
    ) {
        this.image = img;
        this.ip = ipAddress;
        this.dataRate = rate;
        this.isSuccess = success;
    }

    /**
     * Sets the success flag.
     *
     * @param val true for success, false otherwise
     */
    public void setIsSuccess(final boolean val) {
        if (val) {
            isSuccess = SUCCESS_TRUE;
        } else {
            isSuccess = SUCCESS_FALSE;
        }
    }

    /**
     * Gets the buffered image.
     *
     * @return the image
     */
    public BufferedImage image() {
        return image;
    }

    /**
     * Gets the IP address.
     *
     * @return the IP address
     */
    public String ip() {
        return ip;
    }

    /**
     * Gets the data rate.
     *
     * @return the data rate
     */
    public long dataRate() {
        return dataRate;
    }

    /**
     * Gets the success flag.
     *
     * @return the success flag
     */
    public byte isSuccess() {
        return isSuccess;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        final UIImage that = (UIImage) obj;
        return Objects.equals(this.image, that.image)
            && Objects.equals(this.ip, that.ip)
            && this.isSuccess == that.isSuccess;
    }

    @Override
    public int hashCode() {
        return Objects.hash(image, ip, isSuccess);
    }

    @Override
    public String toString() {
        return "UIImage["
            + "image=" + image + ", "
            + "ip=" + ip + ", "
            + "isSuccess=" + isSuccess + ']';
    }

}
