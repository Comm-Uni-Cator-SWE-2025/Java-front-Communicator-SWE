/**
 *  Contributed by Sandeep Kumar.
 */
package com.swe.ux.model;

import java.awt.image.BufferedImage;
import java.util.Objects;

public final class UIImage {
    private final BufferedImage image;
    private final String ip;
    private byte isSuccess;

    public UIImage(
        BufferedImage image,
        String ip,
        byte isSuccess
    ) {
        this.image = image;
        this.ip = ip;
        this.isSuccess = isSuccess;
    }

    public void setIsSuccess(boolean val) {
        isSuccess = (byte) (val ? 1 : 0);
    }

    public BufferedImage image() {
        return image;
    }

    public String ip() {
        return ip;
    }

    public byte isSuccess() {
        return isSuccess;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (UIImage) obj;
        return Objects.equals(this.image, that.image) &&
            Objects.equals(this.ip, that.ip) &&
            this.isSuccess == that.isSuccess;
    }

    @Override
    public int hashCode() {
        return Objects.hash(image, ip, isSuccess);
    }

    @Override
    public String toString() {
        return "UIImage[" +
            "image=" + image + ", " +
            "ip=" + ip + ", " +
            "isSuccess=" + isSuccess + ']';
    }

}
