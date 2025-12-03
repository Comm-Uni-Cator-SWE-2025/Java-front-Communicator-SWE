package com.swe.ux.views;

import com.swe.ux.theme.Theme;
import com.swe.ux.theme.ThemeManager;
import com.swe.ux.ui.FrostedBackgroundPanel;
import com.swe.ux.ui.SoftCardPanel;
import com.swe.ux.ui.FrostedToolbarButton;
import com.swe.ux.ui.FontUtil;
import com.swe.ux.viewmodels.LoginViewModel;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

/**
 * Login view reimagined with a floating frosted-glass card inspired by macOS.
 */
public class LoginPage extends FrostedBackgroundPanel {
    /** UI initialization flag. */
    private boolean uiInitialized = false;

    /** View model for login functionality. */
    private final LoginViewModel viewModel;
    /** Glass card panel for the login UI. */
    private SoftCardPanel glassCard;
    /** Title label for the application. */
    private JLabel titleLabel;
    /** Helper text label. */
    private JLabel helperLabel;
    /** Date/subtitle label. */
    private JLabel dateLabel;
    /** Google login button. */
    private FrostedToolbarButton googleLoginButton;

    /** Border padding constant. */
    private static final int BORDER_PADDING = 32;
    /** Card width constant. */
    private static final int CARD_WIDTH = 560;
    /** Card height constant. */
    private static final int CARD_HEIGHT = 520;
    /** Content border top constant. */
    private static final int CONTENT_BORDER_TOP = 20;
    /** Content border sides constant. */
    private static final int CONTENT_BORDER_SIDES = 28;
    /** Title font size constant. */
    private static final float TITLE_FONT_SIZE = 32f;
    /** Subtitle font size constant. */
    private static final float SUBTITLE_FONT_SIZE = 14f;
    /** Vertical spacing constant. */
    private static final int VERTICAL_SPACING = 40;
    /** Button width constant. */
    private static final int BUTTON_WIDTH = 320;
    /** Button height constant. */
    private static final int BUTTON_HEIGHT = 50;
    /** Button font size constant. */
    private static final float BUTTON_FONT_SIZE = 15f;
    /** Helper font size constant. */
    private static final float HELPER_FONT_SIZE = 11f;

    /**
     * Constructs a LoginPage with the given view model.
     *
     * @param loginViewModel the view model for login functionality
     */
    public LoginPage(final LoginViewModel loginViewModel) {
        this.viewModel = loginViewModel;
        initializeUI();
        uiInitialized = true;
        applyTheme();

        setupBindings();
        startClock();
        applyTheme();
    }

    private void initializeUI() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(BORDER_PADDING, BORDER_PADDING,
                BORDER_PADDING, BORDER_PADDING));

        glassCard = new SoftCardPanel();
        glassCard.setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        glassCard.setLayout(new BorderLayout());
        glassCard.setOpaque(false);

        final JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(CONTENT_BORDER_TOP, CONTENT_BORDER_SIDES,
                CONTENT_BORDER_SIDES, CONTENT_BORDER_SIDES));

        content.add(Box.createVerticalGlue());

        final JPanel titleWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titleWrapper.setOpaque(false);
        titleLabel = new JLabel("VARTΛŁΛpp");
        titleLabel.setFont(FontUtil.getJetBrainsMono(TITLE_FONT_SIZE, Font.BOLD));
        titleWrapper.add(titleLabel);
        content.add(titleWrapper);

        final JPanel subtitleWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        subtitleWrapper.setOpaque(false);
        dateLabel = new JLabel("Built with Hope and Tears");
        dateLabel.setFont(FontUtil.getJetBrainsMono(SUBTITLE_FONT_SIZE, Font.PLAIN));
        subtitleWrapper.add(dateLabel);
        content.add(subtitleWrapper);

        content.add(Box.createVerticalStrut(VERTICAL_SPACING));

        googleLoginButton = new FrostedToolbarButton("Sign in with Google");
        googleLoginButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        googleLoginButton.setFont(FontUtil.getJetBrainsMono(BUTTON_FONT_SIZE, Font.BOLD));
        googleLoginButton.addActionListener(e -> viewModel.loginWithGoogle());
        
        final JPanel buttonWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonWrapper.setOpaque(false);
        buttonWrapper.add(googleLoginButton);
        content.add(buttonWrapper);

        content.add(Box.createVerticalStrut(VERTICAL_SPACING));

        helperLabel = new JLabel("<html><center>By signing in, you agree to use Google<br>"
                + "authentication for secure access to IIT<br>Palakkad Meet.</center></html>");
        helperLabel.setFont(FontUtil.getJetBrainsMono(HELPER_FONT_SIZE, Font.PLAIN));
        helperLabel.setHorizontalAlignment(JLabel.CENTER);
        
        final JPanel helperWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        helperWrapper.setOpaque(false);
        helperWrapper.add(helperLabel);
        content.add(helperWrapper);

        content.add(Box.createVerticalGlue());

        glassCard.add(content, BorderLayout.CENTER);

        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        add(glassCard, gbc);
    }

    private void setupBindings() {
        // No data binding required at present
    }

    private void startClock() {
        // Clock disabled
    }


    private void applyTheme() {
        final ThemeManager themeManager = ThemeManager.getInstance();
        if (themeManager == null) {
            return;
        }
        final Theme theme = themeManager.getCurrentTheme();
        if (theme == null) {
            return;
        }

        setBackground(theme.getBackgroundColor());
        titleLabel.setForeground(theme.getTextColor());
        helperLabel.setForeground(theme.getTextColor().darker());
        dateLabel.setForeground(theme.getTextColor());

        SwingUtilities.invokeLater(() -> {
            if (glassCard != null) {
                glassCard.repaint();
            }
        });
    }

    @Override
    public void setVisible(final boolean visible) {
        super.setVisible(visible);
        if (visible) {
            viewModel.reset();
            applyTheme();
            startClock();
        }
    }

    @Override
    public void updateUI() {
        super.updateUI();
        if (uiInitialized) {
            applyTheme();
        }
    }

}
