package com.swe.ux.view;

import com.swe.ux.theme.Theme;
import com.swe.ux.theme.ThemeManager;
import com.swe.ux.ui.CustomButton;
import com.swe.ux.ui.PlaceholderTextField;
import com.swe.ux.viewmodel.RegisterViewModel;
import com.swe.ux.App;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.swe.ux.binding.PropertyListeners;

/**
 * Registration page that allows new users to create an account.
 */
public class RegisterPage extends JPanel {
    private final RegisterViewModel viewModel;
    private JTextField usernameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JButton registerButton;
    private JButton backToLoginButton;
    private JLabel errorLabel;
    private JLabel successLabel;
    private JProgressBar loadingIndicator;
    private JLabel titleLabel;
    private JPanel formPanel;
    private JPanel buttonPanel;

    /**
     * Creates a new RegisterPage with the specified ViewModel.
     * @param viewModel The ViewModel to use for this view
     */
    public RegisterPage(RegisterViewModel viewModel) {
        this.viewModel = viewModel;
        initializeUI();
        setupBindings();
        applyTheme();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(0, 20));
        setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        
        // Initialize button panel
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setOpaque(false);

        // Title
        titleLabel = new JLabel("Create an Account", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        add(titleLabel, BorderLayout.NORTH);

        // Form Panel
        formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 15, 0);

        // Username
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 0, 5, 0);
        formPanel.add(new JLabel("Username"), gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 15, 0);
        usernameField = new PlaceholderTextField("Enter username");
        formPanel.add(usernameField, gbc);

        // Email
        gbc.gridy++;
        gbc.insets = new Insets(5, 0, 5, 0);
        formPanel.add(new JLabel("Email"), gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 15, 0);
        emailField = new PlaceholderTextField("Enter email");
        formPanel.add(emailField, gbc);

        // Password
        gbc.gridy++;
        gbc.insets = new Insets(5, 0, 5, 0);
        formPanel.add(new JLabel("Password"), gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 15, 0);
        passwordField = new JPasswordField();
        formPanel.add(passwordField, gbc);

        // Confirm Password
        gbc.gridy++;
        gbc.insets = new Insets(5, 0, 5, 0);
        formPanel.add(new JLabel("Confirm Password"), gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 20, 0);
        confirmPasswordField = new JPasswordField();
        formPanel.add(confirmPasswordField, gbc);

        // Error Label
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 10, 0);
        errorLabel = new JLabel("");
        errorLabel.setForeground(Color.RED);
        errorLabel.setVisible(false);
        formPanel.add(errorLabel, gbc);

        // Success Label
        gbc.gridy++;
        successLabel = new JLabel("Registration successful! Redirecting to login...");
        successLabel.setForeground(new Color(0, 128, 0));
        successLabel.setVisible(false);
        formPanel.add(successLabel, gbc);

        // Loading Indicator
        loadingIndicator = new JProgressBar();
        loadingIndicator.setIndeterminate(true);
        loadingIndicator.setVisible(false);
        gbc.gridy++;
        formPanel.add(loadingIndicator, gbc);

        // Register Button
        gbc.gridy++;
        registerButton = new CustomButton("Register", true);
        buttonPanel.add(registerButton);

        // Back to Login Button
        backToLoginButton = new JButton("Back to Login");
        backToLoginButton.setFont(backToLoginButton.getFont().deriveFont(Font.PLAIN));
        backToLoginButton.setBorderPainted(false);
        backToLoginButton.setContentAreaFilled(false);
        backToLoginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backToLoginButton.addActionListener(e -> onBackToLoginClicked());
        buttonPanel.add(backToLoginButton);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Add action listeners
        registerButton.addActionListener(this::onRegisterClicked);

        // Add key listeners for enter key
        KeyAdapter enterKeyAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    onRegisterClicked(null);
                }
            }
        };
        
        usernameField.addKeyListener(enterKeyAdapter);
        emailField.addKeyListener(enterKeyAdapter);
        passwordField.addKeyListener(enterKeyAdapter);
        confirmPasswordField.addKeyListener(enterKeyAdapter);
    }

    private void setupBindings() {
        // Bind view to ViewModel
        viewModel.username.addListener(PropertyListeners.onStringChanged(username -> 
            SwingUtilities.invokeLater(() -> 
                usernameField.setText(username != null ? username : "")
            )
        ));

        viewModel.email.addListener(PropertyListeners.onStringChanged(email -> 
            SwingUtilities.invokeLater(() -> 
                emailField.setText(email != null ? email : "")
            )
        ));

        viewModel.isLoading.addListener(PropertyListeners.onBooleanChanged(isLoading -> 
            SwingUtilities.invokeLater(() -> {
                loadingIndicator.setVisible(isLoading);
                registerButton.setEnabled(!isLoading);
                backToLoginButton.setEnabled(!isLoading);
                usernameField.setEnabled(!isLoading);
                emailField.setEnabled(!isLoading);
                passwordField.setEnabled(!isLoading);
                confirmPasswordField.setEnabled(!isLoading);
            })
        ));

        viewModel.errorMessage.addListener(PropertyListeners.onStringChanged(error -> 
            SwingUtilities.invokeLater(() -> {
                errorLabel.setText(error);
                errorLabel.setVisible(error != null && !error.isEmpty());
            })
        ));

        viewModel.registrationSuccess.addListener(PropertyListeners.onBooleanChanged(success -> {
            if (success) {
                SwingUtilities.invokeLater(() -> {
                    successLabel.setText("Registration successful! Redirecting to login...");
                    successLabel.setVisible(true);
                    // Clear sensitive data
                    passwordField.setText("");
                    confirmPasswordField.setText("");
                    // Redirect to login after a short delay
                    new Timer(2000, e -> onBackToLoginClicked()).start();
                });
            }
        }));

        // Bind ViewModel to view
        usernameField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateUsername(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateUsername(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateUsername(); }
            private void updateUsername() {
                viewModel.username.set(usernameField.getText().trim());
            }
        });

        emailField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateEmail(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateEmail(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateEmail(); }
            private void updateEmail() {
                viewModel.email.set(emailField.getText().trim());
            }
        });

        passwordField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updatePassword(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updatePassword(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updatePassword(); }
            private void updatePassword() {
                viewModel.password.set(new String(passwordField.getPassword()));
            }
        });

        confirmPasswordField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateConfirmPassword(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateConfirmPassword(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateConfirmPassword(); }
            private void updateConfirmPassword() {
                viewModel.confirmPassword.set(new String(confirmPasswordField.getPassword()));
            }
        });
    }

    private void onRegisterClicked(ActionEvent event) {
        viewModel.register();
    }

    private void onBackToLoginClicked() {
        // Reset form
        viewModel.username.set("");
        viewModel.email.set("");
        viewModel.password.set("");
        viewModel.confirmPassword.set("");
        viewModel.errorMessage.set("");
        viewModel.registrationSuccess.set(false);
        successLabel.setVisible(false);
        
        // Navigate back to login
        App.getInstance().showView(App.LOGIN_VIEW);
    }

    private void applyTheme() {
        // Apply theme colors
        ThemeManager themeManager = ThemeManager.getInstance();
        Theme theme = themeManager.getCurrentTheme();
        themeManager.applyTheme(this);
        
        // Apply theme to components
        if (backToLoginButton != null) {
            backToLoginButton.setForeground(theme.getPrimary());
            backToLoginButton.setFont(backToLoginButton.getFont().deriveFont(Font.BOLD));
        }
        
        Color bg = theme.getBackgroundColor();
        
        // Apply specific styles
        setBackground(theme.getBackgroundColor());
        titleLabel.setForeground(theme.getTextColor());
        
        // Style the back to login button
        backToLoginButton.setForeground(theme.getPrimaryColor());
        
        // Style the form panel
        if (formPanel != null) {
            formPanel.setBackground(theme.getBackgroundColor());
            for (Component comp : formPanel.getComponents()) {
                if (comp instanceof JLabel) {
                    ((JLabel) comp).setForeground(theme.getTextColor());
                }
            }
        }
    }
}
