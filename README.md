# SecureVault — Password & Credential Manager

SecureVault is a Java-based Password & Credential Manager designed to provide a centralized way to organize and protect account credentials. Built with **Java 25, Java Swing, SQLite, and Maven**, the project focuses on secure data storage, encryption, and credential organization through a modern dark-themed desktop application.

The project is currently under active development, with its core functionality and desktop interface being developed and expanded gradually.

## Implemented Features

### Core Application
- **Application Entry Point** — Initializes the application and its main components.
- **Maven Configuration** — Manages project dependencies and build configuration.
- **Database Integration** — Connects the application to a local SQLite database.
- **Credential Data Model** — Defines the structure used to represent stored credentials.
- **Secure Note Data Model** — Defines the structure used to represent stored secure notes.
- **Database Migration** — Adds support for new database fields while preserving existing credential records.
- **Database Snapshot Creation** — Uses SQLite's snapshot functionality to create a consistent copy of the vault database for backup purposes.

### Encryption & Security
- **AES-256-GCM Encryption** — Encrypts credential information before database storage.
- **PBKDF2-HMAC-SHA256** — Provides master-password-based encryption key derivation.
- **Secure Random Generation** — Generates cryptographic salts and encryption nonces.
- **Byte-Array Encryption** — Supports encrypting binary data for encrypted backup files.
- **Byte-Array Decryption** — Supports decrypting backup data using the corresponding backup password.
- **Authenticated Encryption** — Uses AES-GCM authentication to help detect unauthorized modifications to encrypted backup contents.
- **Secure Notes Encryption** — Encrypts note titles, contents, and categories before storing them in the database.

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

### Secure Notes
- **Secure Notes Panel** — Provides a dedicated interface for creating and managing private notes inside SecureVault.
- **Create Secure Notes** — Allows users to create notes with a title, content, and category.
- **Edit Secure Notes** — Supports modifying existing note titles, contents, and categories.
- **Delete Secure Notes** — Allows users to remove notes they no longer need.
- **Encrypted Note Storage** — Encrypts note titles, contents, and categories before they are stored in SQLite.
- **Custom Note Categories** — Supports assigning categories to notes, including custom category names.
- **Pin and Unpin Notes** — Allows users to pin important notes for easier access.
- **Pinned Note Organization** — Displays pinned notes before unpinned notes.
- **Secure Note Search** — Supports searching notes by title and category.
- **Note Timestamps** — Tracks note creation and last-updated timestamps.
- **Note Statistics** — Displays the total number of notes and pinned notes.
- **Activity Logging** — Records secure note creation, editing, deletion, and pinning actions.
- **Database Integration** — Stores secure notes in a dedicated `secure_notes` SQLite table.
- **Backup Compatibility** — Secure notes are included in database snapshots used by the existing backup system.

### Settings
- **Settings Panel** — Provides a dedicated interface for application preferences and configuration.
- **Theme Management** — Includes a centralized theme component for maintaining the application's visual appearance.
- **Manual Vault Locking** — Allows users to manually lock SecureVault through the Settings panel.
- **Automatic Vault Locking** — Automatically locks the vault after five minutes of inactivity within the application.
- **Application Information** — Displays application and security-related information.
- **Security Information** — Displays information about the application's password hashing, encryption, and database storage.
- **Backup Management** — Provides access to encrypted backup creation and restoration.
- **Backup Restoration Integration** — Connects the backup restoration functionality directly to the Settings panel.

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

### Encrypted Backup & Restoration
- **Encrypted Backup Creation** — Allows users to create encrypted backup files of their SecureVault database.
- **Separate Backup Password** — Uses a dedicated backup password instead of directly relying on the master password.
- **Backup Password Validation** — Requires a minimum backup password length of 12 characters.
- **SQLite Database Snapshot** — Creates a consistent database snapshot before encryption.
- **AES-256-GCM Backup Encryption** — Encrypts the database snapshot before saving it as a backup file.
- **PBKDF2-HMAC-SHA256 Key Derivation** — Derives the backup encryption key from the supplied backup password.
- **Random Salt and Nonce Generation** — Uses randomly generated cryptographic values during backup encryption.
- **`.svb` Backup Format** — Stores encrypted backup information in a dedicated SecureVault backup file format.
- **Backup File Validation** — Checks the backup file's structure, format, version, salt length, encrypted data length, and unexpected trailing data.
- **Backup Decryption** — Decrypts backup contents using the correct backup password.
- **Backup Password Verification** — Uses authenticated decryption to reject incorrect passwords or corrupted encrypted contents.
- **Database Snapshot Validation** — Checks the restored SQLite database before replacing the active database.
- **Database Integrity Checking** — Performs SQLite integrity checks and verifies required database tables and columns.
- **Safe Database Restoration** — Uses a staged database file and rollback protection when replacing the existing database.
- **Temporary File Cleanup** — Attempts to remove temporary snapshot and decrypted backup files after processing.
- **Backup Management Service** — Uses a dedicated `BackupService` class to handle backup creation, validation, and restoration.
- **Settings Integration** — Provides Create Encrypted Backup and Restore Encrypted Backup options directly from the Settings panel.
- **Restoration Confirmation** — Warns users that restoring a backup replaces the current database before proceeding.
- **Return to Login** — Returns the application to the login screen after successful database restoration so the restored database can be reopened.
- **Secure Notes Backup Support** — Includes the secure notes database table and its stored encrypted data in SQLite database snapshots.

### User Interface
- **Login Interface** — Provides the initial authentication screen for accessing the vault.
- **Dashboard Panel** — Establishes the main dashboard interface.
- **Credential Entry Dialog** — Provides a dedicated dialog for preparing credential entries.
- **Password Generator Panel** — Provides a dedicated interface for generating and evaluating passwords.
- **History Panel** — Displays recorded vault activities.
- **Category Panel** — Provides an interface for credential category organization.
- **Security Panel** — Provides the security analysis interface.
- **Password Health Panel** — Provides an interface for reviewing password health information.
- **Secure Notes Panel** — Provides the interface for creating, editing, searching, pinning, and deleting encrypted notes.
- **Settings Panel** — Provides an interface for application settings, encrypted backup creation, and backup restoration.
- **Main Frame** — Serves as the primary application window and navigation structure.
- **Theme Component** — Centralizes the application's visual theme.
- **Vault Panel** — Provides the main credential management interface.

## Planned Features

The following components are still under development or planned for future updates:

- **Password Expiration Tracking** — Track password age and identify credentials that may need updating.
- **Expanded Security Recommendations** — Introduce additional security checks and recommendations.
- **Dashboard Improvements** — Expand dashboard statistics and security-related information.
- **Recycle Bin and Credential Recovery** — Introduce a recovery system for accidentally deleted credentials.
- **Secure Clipboard Improvements** — Further improve clipboard handling and sensitive-data cleanup.
- **Additional Security Improvements** — Continue improving vault protection, password handling, backup management, and application security.

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

The complete desktop application is still under development, and additional testing and improvements are ongoing.

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
│                   │   ├── VaultEntry.java
│                   │   └── SecureNote.java
│                   ├── service/
│                   │   ├── PasswordStrengthAnalyzer.java
│                   │   ├── BackupService.java
│                   │   └── NoteService.java
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
│                       ├── VaultPanel.java
│                       ├── PasswordHealthPanel.java
│                       └── SecureNotesPanel.java
├── pom.xml
└── README.md
```

## Project Status

**Under Active Development**

SecureVault is being developed gradually. Its current implementation includes the core application structure, database integration, encryption services, master password authentication, dashboard, password generator, credential entry form, category management, activity history interface, password security analysis, encrypted backup and restoration, and secure note management.

The Password Generator uses a dedicated Password Strength Analyzer to evaluate generated passwords and provide improvement recommendations. The Security Center also uses the same analyzer to identify weak passwords, detect reused passwords, calculate individual password scores, and display security recommendations.

The Vault Management interface supports credential favorites, filtering between all, favorite, and non-favorite entries, expanded credential searching, and sorting by different credential fields. Users can also copy saved passwords to the clipboard, with an automatic clearing attempt after 30 seconds.

Database updates also support the new favorite field while preserving existing credential records. New credential activity descriptions use generic action labels instead of including credential titles.

The application includes automatic vault locking after five minutes of inactivity, as well as a manual locking option through the Settings panel.

The Encrypted Backup feature introduces a dedicated backup service that creates a SQLite database snapshot and encrypts it using AES-256-GCM. Users can provide a separate backup password, and the resulting encrypted backup is saved in the `.svb` file format.

The backup system has also been expanded to support restoration. Users can select an existing `.svb` file, validate its structure, enter its backup password, and decrypt its contents. Before installation, the restored database is checked for SQLite integrity and required database structures. The restoration process uses a staged database file and rollback protection to help protect the existing database if installation fails.

The Settings panel includes both Create Encrypted Backup and Restore Encrypted Backup options. Successful restoration closes the current database connection and returns the application to the login screen so that the restored database can be reopened.

The Secure Notes feature introduces a dedicated note management system integrated into the main application. Users can create, edit, search, pin, unpin, and delete private notes. Each note supports a title, content, and category, along with creation and last-updated timestamps.

Secure note titles, contents, and categories are encrypted before being stored in the dedicated `secure_notes` SQLite table. The Notes interface also displays total and pinned note statistics, supports searching by title and category, and places pinned notes before unpinned notes. Note-related actions are recorded in the activity history.

Secure Notes are stored in the same SQLite database used by the vault. As a result, the existing database snapshot mechanism includes the secure notes table and its encrypted data when creating database backups.

The UI structure includes dedicated components for the Dashboard, Vault, Categories, Password Generator, Activity History, Security, Password Health, Secure Notes, Settings, Login, and Main Application Frame, along with a centralized Theme component.

Further development will focus on improving the application's security, expanding its functionality, and testing existing features.

## Security Notice

SecureVault is an educational project and has not undergone a professional security audit. Its encryption implementation and password strength analysis are intended for learning and development purposes. The password strength analyzer uses local heuristic checks and does not verify whether passwords have appeared in data breaches.

Clipboard auto-clear is best-effort and cannot guarantee removal from operating system clipboard history or third-party clipboard managers.

Encrypted backup creation and restoration have been implemented in the project code. However, backup recovery, database replacement, rollback behavior, and application login after restoration should be thoroughly tested before relying on the feature for important data.

Secure Notes encrypts note titles, contents, and categories before database storage. However, the overall application and its security mechanisms still require thorough testing and review.

Avoid storing real or sensitive credentials or private information until the application has been thoroughly tested and reviewed.

## Purpose

This project is part of my journey as an Information Technology student. It serves as an opportunity to improve my Java programming skills while exploring database management, application security, encryption, backup and recovery systems, secure data storage, and desktop software development.
