/**
 *  Contributed by Sandeep Kumar.
 */

package com.swe.ux.viewmodels;

import com.swe.controller.RPCinterface.AbstractRPC;
import com.swe.screenNVideo.RImage;
import com.swe.screenNVideo.SubscriberPacket;
import com.swe.screenNVideo.Utils;
import com.swe.ux.binding.BindableProperty;
import com.swe.ux.model.UIImage;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * ViewModel for screen and video sharing.
 */
public class ScreenNVideoModel extends BaseViewModel {

    /** Image received callback. */
    private Consumer<UIImage> onImageReceived;

    /** RPC instance. */
    private final AbstractRPC rpc;

    /** Singleton instance. */
    private static ScreenNVideoModel instance;

    /** Property to track visible participants (IPs). */
    private final BindableProperty<Set<String>> visibleParticipants = 
        new BindableProperty<>(new HashSet<>(), "visibleParticipants");

    /**
     * Gets the singleton instance.
     * @param rpcParam The RPC instance
     * @return The singleton instance
     */
    public static ScreenNVideoModel getInstance(final AbstractRPC rpcParam) {
        if (instance == null) {
            instance = new ScreenNVideoModel(rpcParam);
        }
        return instance;
    }
    
    private ScreenNVideoModel(final AbstractRPC rpcParam) {
        this.rpc = rpcParam;
        
        initComponents();
    }

    /**
     * Sets the image received callback.
     * @param onImageReceivedParam The callback to set
     */
    public void setOnImageReceived(final Consumer<UIImage> onImageReceivedParam) {
        this.onImageReceived = onImageReceivedParam;
    }

    /**
     * Requests uncompressed data from the specified IP.
     * @param ip The IP address
     */
    public void requestUncompressedData(final String ip) {
        final SubscriberPacket subscriberPacket = new SubscriberPacket(ip, false);
        rpc.call(Utils.SUBSCRIBE_AS_VIEWER, subscriberPacket.serialize());
    }

    /**
     * Requests compressed data from the specified IP.
     * @param ip The IP address
     */
    public void requestCompressedData(final String ip) {
        final SubscriberPacket subscriberPacket = new SubscriberPacket(ip, true);
        rpc.call(Utils.SUBSCRIBE_AS_VIEWER, subscriberPacket.serialize());
    }

    /**
     * Updates the list of currently visible participants.
     * Called by the View when layout or scroll changes.
     * @param visibleEmails The set of visible participant emails
     */
    public void updateVisibleParticipants(final Set<String> visibleEmails) {
        System.out.println("Participants " + Arrays.toString(visibleEmails.toArray()));
        // get new ips
        for (final String emails : visibleEmails) {
            if (!visibleParticipants.get().contains(emails)) {
                final SubscriberPacket subscriberPacket = new SubscriberPacket(emails, true);
                try {
                    rpc.call(Utils.SUBSCRIBE_AS_VIEWER, subscriberPacket.serialize());
                } catch (final NumberFormatException ignored) {
                    // Ignore format exceptions
                }
            }
        }
        // get ips to remove
        for (final String ip : visibleParticipants.get()) {
            if (!visibleEmails.contains(ip)) {
                final SubscriberPacket subscriberPacket = new SubscriberPacket(ip, true);
                try {
                    rpc.call(Utils.UNSUBSCRIBE_AS_VIEWER, subscriberPacket.serialize());
                } catch (final NumberFormatException ignored) {
                    // Ignore format exceptions
                }
            }
        }
        visibleParticipants.set(visibleEmails);
    }

    private void initComponents() {
        rpc.subscribe(Utils.UPDATE_UI, args -> {
            final RImage rImage = RImage.deserialize(args);
            final int[][] image = rImage.getImage();
            final int height = image.length;
            final int width = image[0].length;

            final BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

            for (int x = 0; x < height; x++) {
                for (int y = 0; y < width; y++) {
                    bufferedImage.setRGB(y, x, image[x][y]);
                }
            }
            final UIImage uiImage = new UIImage(bufferedImage, rImage.getIp(), rImage.getDataRate(), (byte) 1);
            onImageReceived.accept(uiImage);
            final byte[] res = new byte[1];
            res[0] = uiImage.isSuccess();
            return res;
        });
    }

    /**
     * Gets the visible participants property.
     * @return The visible participants property
     */
    public BindableProperty<Set<String>> getVisibleParticipants() {
        return visibleParticipants;
    }
}
