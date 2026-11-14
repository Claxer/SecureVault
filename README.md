# SecureVault — Password & Credential Manager

SecureVault is a Java-based Password & Credential Manager designed to provide a centralized way to organize and protect account credentials. Built with **Java 25, Java Swing, SQLite, and Maven**, the project focuses on secure data storage, encryption, and credential organization through a modern dark-themed desktop application.

The project is currently under active development, with its core functionality and desktop interface being developed and expanded gradually.

## Implemented Features

### Core Application
- **Application Entry Point** — Initializes the application and its main components.
- **Maven Configuration** — Manages project dependencies and build configuration.
- **Database Integration** — Connects the application to a local SQLite database.
- **Credential Data Model** — Defines the structure used to represent stored credentials.
- **Database Migration** — Adds support for new database fields while preserving existing credential records.

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
- **Favorite Option** — Allows users to mark credential entries as favorites directly from the entry form.

### Password Generator
- **Custom Password Length** — Generates passwords with configurable lengths from 8 to 64 characters.
- **Character Options** — Supports uppercase letters, lowercase letters, numbers, and symbols.
- **Secure Random Generation** — Uses Java's `SecureRandom` to generate random passwords.
- **Character Group Inclusion** — Ensures at least one character from each selected character group is included in the generated password.
- **Password Strength Analysis** — Uses the Password Strength Analyzer to calculate a score from 0 to 100.
- **Strength Classification** — Displays password ratings ranging from Very Weak to Very Strong.
- **Password Recommendations** — Provides suggestions based on the generated password's characteristics.
- **Copy to Clipboard** — Allows generated passwords to be copied conveniently.

### Password Strength Analyzer
- **Password Scoring System** — Evaluates passwords using a score ranging from 0 to 100.
- **Strength Classification** — Categorizes passwords as Very Weak, Weak, Fair, Strong, or Very Strong.
- **Password Length Analysis** — Evaluates password length and rewards longer passwords.
- **Character Variety Detection** — Checks for uppercase letters, lowercase letters, numbers, and symbols.
- **Common Password Detection** — Identifies selected commonly used passwords.
- **Repeated Character Detection** — Identifies consecutive repeated characters.
- **Sequential Character Detection** — Detects predictable sequences such as ascending or descending characters.
- **Security Recommendations** — Provides suggestions to improve password quality.

### Category Management
- **Credential Categories** — Supports organizing credentials using category information.
- **Category Structure** — Establishes the foundation for grouping and managing saved credentials.

### Activity History
- **Activity Records** — Displays recorded vault activities.
- **Activity Descriptions** — Shows information about logged actions.
- **History Interface** — Provides a dedicated page for reviewing activity records.
- **Generic Activity Logging** — Records credential-related actions without including credential titles in new activity descriptions.

### Security Management
- **Security Panel** — Provides a dedicated interface for reviewing password security.
- **Weak Password Detection** — Identifies credentials with low password strength scores.
- **Reused Password Detection** — Identifies passwords shared across multiple saved accounts.
- **Individual Password Analysis** — Displays the strength classification and numerical score of each credential.
- **Security Status Overview** — Displays total credentials, weak passwords, reused passwords, and strong passwords.
- **Security Recommendations** — Provides improvement suggestions for individual credentials.
- **Refreshable Security Analysis** — Allows users to refresh the security report using their saved credentials.

### Settings
- **Settings Panel** — Provides a dedicated interface for application preferences and configuration.
- **Theme Management** — Includes a centralized theme component for maintaining the application's visual appearance.

### Vault Management
- **Vault Panel** — Provides a dedicated interface for accessing and organizing stored credentials.
- **Credential Search** — Supports searching saved credentials by title, username, category, and notes.
- **Favorites Management** — Allows users to mark and unmark credentials as favorites.
- **Favorites Filtering** — Provides options to display all credentials, favorites only, or non-favorite credentials.
- **Credential Sorting** — Supports sorting saved credentials by newest, oldest, title, username, and category.
- **Favorite Indicators** — Displays favorite status directly in the credential table.
- **View Credentials** — Allows users to view saved credential details.
- **Edit Credentials** — Supports modifying existing credential entries.
- **Delete Credentials** — Allows users to remove saved credentials.
- **Copy Password** — Allows users to copy a saved credential's password to the clipboard.
- **Clipboard Auto-Clear** — Attempts to clear copied passwords from the clipboard after 30 seconds, provided the clipboard still contains the copied password.
- **Main Application Navigation** — Uses a centralized main frame to organize the application's different panels.

### User Interface
- **Login Interface** — Provides the initial authentication screen for accessing the vault.
- **Dashboard Panel** — Establishes the main dashboard interface.
- **Credential Entry Dialog** — Provides a dedicated dialog for preparing credential entries.
- **Password Generator Panel** — Provides a dedicated interface for generating and evaluating passwords.
- **History Panel** — Displays recorded vault activities.
- **Category Panel** — Provides an interface for credential category organization.
- **Security Panel** — Provides the security analysis interface.
- **Settings Panel** — Provides an interface for application settings.
- **Main Frame** — Serves as the primary application window and navigation structure.
- **Theme Component** — Centralizes the application's visual theme.
- **Vault Panel** — Provides the main credential management interface.

## Planned Features

The following components are still under development or planned for future updates:

- **Encrypted Backup** — Implement encrypted backups of stored credentials.
- **Backup Restoration** — Provide a way to restore credentials from encrypted backup files.
- **Automatic Vault Locking** — Lock the vault after a period of inactivity.
- **Advanced Search and Filtering** — Expand credential search with additional filtering options.
- **Password Expiration Tracking** — Track password age and identify credentials that may need updating.
- **Security Recommendations Expansion** — Introduce additional security checks and recommendations.
- **Dashboard Improvements** — Expand dashboard statistics and security-related information.
- **Additional Security Improvements** — Continue improving vault protection, password handling, and application security.

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

6. Run the application:

```bash
mvn exec:java
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
│                   ├── service/
│                   │   └── PasswordStrengthAnalyzer.java
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

SecureVault is being developed gradually. Its current implementation includes the core application structure, database integration, encryption services, master password authentication, dashboard, password generator, credential entry form, category management, activity history interface, and password security analysis.

The Password Generator uses a dedicated Password Strength Analyzer to evaluate generated passwords and provide improvement recommendations. The Security Center also uses the same analyzer to identify weak passwords, detect reused passwords, calculate individual password scores, and display security recommendations.

The Vault Management interface now supports credential favorites, filtering between all, favorite, and non-favorite entries, expanded credential searching, and sorting by different credential fields. Users can also copy saved passwords to the clipboard, with an automatic clearing attempt after 30 seconds.

Database updates also support the new favorite field while preserving existing credential records. New credential activity descriptions use generic action labels instead of including credential titles.

The UI structure includes dedicated components for the Dashboard, Vault, Categories, Password Generator, Activity History, Security, Settings, Login, and Main Application Frame, along with a centralized Theme component.

Additional components and improvements will be introduced in future updates as development continues.

## Security Notice

SecureVault is an educational project and has not undergone a professional security audit. Its encryption implementation and password strength analysis are intended for learning and development purposes. The password strength analyzer uses local heuristic checks and does not verify whether passwords have appeared in data breaches. Clipboard auto-clear is best-effort and cannot guarantee removal from operating system clipboard history or third-party clipboard managers. Avoid storing real or sensitive credentials until the application has been thoroughly tested and reviewed.

## Purpose

This project is part of my journey as an Information Technology student. It serves as an opportunity to improve my Java programming skills while exploring database management, application security, encryption, and desktop software development.
