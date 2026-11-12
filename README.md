# SecureVault — Password & Credential Manager

SecureVault is a Java-based Password & Credential Manager designed to provide a centralized way to organize and protect account credentials. Built with **Java 25, Java Swing, SQLite, and Maven**, the project focuses on secure data storage, encryption, and credential organization through a modern dark-themed desktop application.

The project is currently under active development, with its core functionality and desktop interface being developed and expanded gradually.

## Implemented Features

### Core Application
- **Application Entry Point** — Initializes the application and its main components.
- **Maven Configuration** — Manages project dependencies and build configuration.
- **Database Integration** — Connects the application to a local SQLite database.
- **Credential Data Model** — Defines the structure used to represent stored credentials.

### Encryption & Security
- **AES-256-GCM Encryption** — Encrypts credential information before database storage.
- **PBKDF2-HMAC-SHA256** — Provides master-password-based encryption key derivation.
- **Secure Random Generation** — Generates cryptographic salts and encryption nonces.

### Master Password Authentication
- **Master Password Setup** — Provides the initial master password setup process.
- **Vault Unlocking** — Allows users to authenticate before accessing the vault.
- **Password Validation** — Checks master password requirements during setup.

### Dashboard
- **Credential Statistics** — Displays an overview of saved credentials.
- **Category Statistics** — Shows the number of credential categories.
- **Activity Overview** — Displays activity record statistics.
- **Recent Credentials** — Shows recently added credential entries.

### Credential Entry Form
- **Credential Input Form** — Provides fields for account titles, usernames, passwords, URLs, categories, and notes.
- **Add and Edit Support** — Supports preparing new credential entries and editing existing ones.

### Password Generator
- **Custom Password Length** — Generates passwords with configurable lengths.
- **Character Options** — Supports uppercase letters, lowercase letters, numbers, and symbols.
- **Password Strength Estimate** — Displays an estimated password strength.
- **Copy to Clipboard** — Allows generated passwords to be copied conveniently.

### Category Management
- **Credential Categories** — Supports organizing credentials using category information.
- **Category Structure** — Establishes the foundation for grouping and managing saved credentials.

### Activity History
- **Activity Records** — Displays recorded vault activities.
- **Activity Descriptions** — Shows information about logged actions.
- **History Interface** — Provides a dedicated page for reviewing activity records.

### Security Management
- **Security Panel** — Provides a dedicated interface for security-related features and vault protection.
- **Security Analysis Foundation** — Establishes the interface for future password security monitoring and recommendations.

### Settings
- **Settings Panel** — Provides a dedicated interface for application preferences and configuration.
- **Theme Management** — Includes a centralized theme component for maintaining the application's visual appearance.

### Vault Management
- **Vault Panel** — Provides a dedicated interface for accessing and organizing stored credentials.
- **Main Application Navigation** — Uses a centralized main frame to organize the application's different panels.

### User Interface
- **Login Interface** — Provides the initial authentication screen for accessing the vault.
- **Dashboard Panel** — Establishes the main dashboard interface.
- **Credential Entry Dialog** — Provides a dedicated dialog for preparing credential entries.
- **Password Generator Panel** — Provides a dedicated interface for generating passwords.
- **History Panel** — Displays recorded vault activities.
- **Category Panel** — Provides an interface for credential category organization.
- **Security Panel** — Establishes the security management interface.
- **Settings Panel** — Provides an interface for application settings.
- **Main Frame** — Serves as the primary application window and navigation structure.
- **Theme Component** — Centralizes the application's visual theme.
- **Vault Panel** — Provides the main credential management interface.

## Planned Features

The following components are still under development or planned for future updates:

- **Complete Credential Management** — Fully integrate adding, viewing, editing, deleting, and searching credentials through the vault interface.
- **Security Analysis** — Identify weak and reused passwords and provide security recommendations.
- **Settings** — Fully integrate application preferences and vault controls.
- **Main Dashboard Integration** — Connect all completed panels through the main application navigation.
- **Additional Security Improvements** — Improve vault protection, password handling, and application security.

## Technologies Used

- Java 25
- Java Swing
- SQLite
- SQLite JDBC
- Java Cryptography Architecture (JCA)
- AES-256-GCM
- PBKDF2-HMAC-SHA256
- SecureRandom
- Maven

## Requirements

- JDK 25 or later
- Apache Maven
- Visual Studio Code or another Java IDE

## Running the Project

1. Clone or download this repository.
2. Open the project folder in Visual Studio Code.
3. Make sure Java 25 and Maven are installed.
4. Open a terminal in the project directory.
5. Compile the project:

```bash
mvn clean compile
```

The complete desktop application is still under development, and not all planned features are currently integrated.

## Current Project Structure

```text
SecureVault/
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── securevault/
│                   ├── Main.java
│                   ├── crypto/
│                   │   └── CryptoService.java
│                   ├── db/
│                   │   └── Database.java
│                   ├── model/
│                   │   └── VaultEntry.java
│                   └── ui/
│                       ├── CategoriesPanel.java
│                       ├── DashboardPanel.java
│                       ├── EntryDialog.java
│                       ├── GeneratorPanel.java
│                       ├── HistoryPanel.java
│                       ├── LoginFrame.java
│                       ├── MainFrame.java
│                       ├── SecurityPanel.java
│                       ├── SettingsPanel.java
│                       ├── Theme.java
│                       └── VaultPanel.java
├── pom.xml
└── README.md
```

## Project Status

**Under Active Development**

SecureVault is being developed gradually. Its current implementation includes the core application structure, database integration, encryption services, master password authentication, dashboard, password generator, credential entry form, category management, and activity history interface.

The UI structure now includes dedicated components for the Dashboard, Vault, Categories, Password Generator, Activity History, Security, Settings, Login, and Main Application Frame, along with a centralized Theme component.

Additional components and improvements will be introduced in future updates as development continues.

## Security Notice

SecureVault is an educational project and has not undergone a professional security audit. Its encryption implementation is intended for learning and development purposes. Avoid storing real or sensitive credentials until the application has been thoroughly tested and reviewed.

## Purpose

This project is part of my journey as an Information Technology student. It serves as an opportunity to improve my Java programming skills while exploring database management, application security, encryption, and desktop software development.
