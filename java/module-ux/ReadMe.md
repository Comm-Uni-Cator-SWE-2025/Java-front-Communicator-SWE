# UX Module

## Overview
The UX module provides a modern, themeable user interface for the Java-front-Communicator application. Built with Java Swing, it offers a responsive and intuitive interface for users to interact with the communication system.

## Features

### 🎨 Theming System
- Light and Dark theme support
- System theme detection
- Smooth theme transitions
- Customizable color schemes

### 🖥️ UI Components
- **CustomButton**: Enhanced button with hover and press effects
- **PlaceholderTextField/PasswordField**: Input fields with placeholder text support
- **TitledPanel**: Custom panel with title bar
- **ThemeToggleButton**: Switch between light and dark themes

### 🏗️ Project Structure
```
module-ux/
├── pom.xml                 # Maven configuration
└── src/
    └── main/
        └── java/
            └── com/
                └── swe/
                    └── ux/
                        ├── App.java                # Application entry point
                        ├── theme/                  # Theming system
                        │   ├── Theme.java          # Theme interface
                        │   ├── LightTheme.java     # Light theme implementation
                        │   ├── DarkTheme.java      # Dark theme implementation
                        │   └── ThemeManager.java   # Theme management
                        ├── ui/                     # Custom UI components
                        │   ├── CustomButton.java
                        │   ├── PlaceholderTextField.java
                        │   └── TitledPanel.java
                        └── views/                  # Application screens
                            ├── LoginPage.java
                            ├── MainPage.java
                            └── MeetingInterface.java
```

## 🚀 Getting Started

### Prerequisites
- Java 17 or later
- Maven 3.6.0 or later

### Building the Module
```bash
# Build the module
mvn clean install

# Run the application
mvn exec:java -Dexec.mainClass="com.swe.ux.App"
```

## 🎯 Key Classes

### App.java
Main application class that initializes the UI and sets up the main window.

### Theme System
- **Theme Interface**: Defines the contract for all themes
- **ThemeManager**: Singleton that handles theme switching and management
- **Built-in Themes**: Light and Dark theme implementations

### UI Components
- **CustomButton**: Enhanced JButton with theme support
- **PlaceholderTextField**: Text field with placeholder support
- **TitledPanel**: Panel with a title bar and border

## 🔧 Configuration

### Theme Configuration
Theme colors and styles can be customized by modifying the respective theme classes in the `theme` package.

### Window Settings
Default window size and position can be configured in `App.java`.

## 🤝 Integration
This module integrates with:
- `module-controller` for business logic
- `module-networking` for communication services

## 📝 Best Practices
1. Use theme colors from `ThemeManager` instead of hardcoded colors
2. Make components responsive to window resizing
3. Follow the existing component patterns for consistency
4. Keep UI logic separate from business logic

## 📄 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
                        └── MeetingInterface.java