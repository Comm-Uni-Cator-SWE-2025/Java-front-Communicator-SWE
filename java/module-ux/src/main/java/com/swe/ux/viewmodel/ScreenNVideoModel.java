/**
 *  Contributed by Sandeep Kumar.
 */
package com.swe.ux.viewmodel;

import com.swe.screenNVideo.AbstractRPC;
import com.swe.screenNVideo.DummyRPC;
import com.swe.screenNVideo.RImage;
import com.swe.screenNVideo.Utils;
import com.swe.ux.binding.BindableProperty;
import com.swe.ux.model.UIImage;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class ScreenNVideoModel extends BaseViewModel {

    private Consumer<UIImage> onImageReceived;

    private AbstractRPC rpc;

    private static ScreenNVideoModel INSTANCE;

    // Property to track visible participants (IPs)
    public final BindableProperty<Set<String>> visibleParticipants = new BindableProperty<>(new HashSet<>(), "visibleParticipants");

    public static ScreenNVideoModel getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ScreenNVideoModel();
        }
        return INSTANCE;
    }

    private ScreenNVideoModel() {
        this.rpc = DummyRPC.getInstance();

        initComponents();
    }

    public void setOnImageReceived(Consumer<UIImage> onImageReceived) {
        this.onImageReceived = onImageReceived;
    }

    /**
     * Updates the list of currently visible participants.
     * Called by the View when layout or scroll changes.
     */
    public void updateVisibleParticipants(Set<String> visibleIps) {
        visibleParticipants.set(visibleIps);
    }

    private void initComponents() {
        rpc.subscribe(Utils.UPDATE_UI, (args) -> {
            final RImage rImage = RImage.deserialize(args);
            final int[][] image = rImage.getImage();
            int height = image.length;
            int width = image[0].length;

            BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

            for (int x = 0; x < height; x++) {
                for (int y = 0; y < width; y++) {
                    bufferedImage.setRGB(y, x, image[x][y]);
                }
            }
            UIImage uiImage = new UIImage(bufferedImage, rImage.getIp(), (byte) 1);
            onImageReceived.accept(uiImage);
            byte[] res = new byte[1];
            res[0] = uiImage.isSuccess();
            return res;
        });
    }
}
