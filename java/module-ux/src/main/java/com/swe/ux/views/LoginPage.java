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
    private boolean uiInitialized = false;

    private final LoginViewModel viewModel;
    private SoftCardPanel glassCard;
    private JLabel titleLabel;
    private JLabel helperLabel;
    private JLabel dateLabel;
    private FrostedToolbarButton googleLoginButton;

    public LoginPage(LoginViewModel viewModel) {
        this.viewModel = viewModel;
        initializeUI();
        uiInitialized = true;
        applyTheme();

        setupBindings();
        startClock();
        applyTheme();
    }

    private void initializeUI() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 32));

        glassCard = new SoftCardPanel();
        glassCard.setPreferredSize(new Dimension(560, 520));
        glassCard.setLayout(new BorderLayout());
        glassCard.setOpaque(false);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(20, 28, 28, 28));

        content.add(Box.createVerticalGlue());

        JPanel titleWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titleWrapper.setOpaque(false);
        titleLabel = new JLabel("IIT Palakkad Meet");
        titleLabel.setFont(FontUtil.getJetBrainsMono(32f, Font.BOLD));
        titleWrapper.add(titleLabel);
        content.add(titleWrapper);

        JPanel subtitleWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        subtitleWrapper.setOpaque(false);
        dateLabel = new JLabel("Secure video conferencing for education");
        dateLabel.setFont(FontUtil.getJetBrainsMono(14f, Font.PLAIN));
        subtitleWrapper.add(dateLabel);
        content.add(subtitleWrapper);

        content.add(Box.createVerticalStrut(40));

        googleLoginButton = new FrostedToolbarButton("Sign in with Google");
        googleLoginButton.setPreferredSize(new Dimension(320, 50));
        googleLoginButton.setFont(FontUtil.getJetBrainsMono(15f, Font.BOLD));
        googleLoginButton.addActionListener(e -> viewModel.loginWithGoogle());
        
        JPanel buttonWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonWrapper.setOpaque(false);
        buttonWrapper.add(googleLoginButton);
        content.add(buttonWrapper);

        content.add(Box.createVerticalStrut(40));

        helperLabel = new JLabel("<html><center>By signing in, you agree to use Google<br>authentication for secure access to IIT<br>Palakkad Meet.</center></html>");
        helperLabel.setFont(FontUtil.getJetBrainsMono(11f, Font.PLAIN));
        helperLabel.setHorizontalAlignment(JLabel.CENTER);
        
        JPanel helperWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        helperWrapper.setOpaque(false);
        helperWrapper.add(helperLabel);
        content.add(helperWrapper);

        content.add(Box.createVerticalGlue());

        glassCard.add(content, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
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
        ThemeManager themeManager = ThemeManager.getInstance();
        if (themeManager == null) return;
        Theme theme = themeManager.getCurrentTheme();
        if (theme == null) return;

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
    public void setVisible(boolean visible) {
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
