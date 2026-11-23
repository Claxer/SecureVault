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
- **Database Migration** — Adds support for new database fields, including favorites and soft deletion, while preserving existing credential records.
- **Automatic Schema Updates** — Checks whether database columns exist before adding them, helping maintain compatibility with existing databases.
- **Database Snapshot Creation** — Uses SQLite snapshot functionality to create a consistent copy of the vault database for backup purposes.
- **Database Connection Management** — Supports closing and reopening the database connection during database restoration.
- **Database Restoration Protection** — Uses staged database files and rollback handling when replacing the active database.
- **Login Security Storage** — Uses a dedicated SQLite table to store failed login attempts and lockout information.
- **Persistent Login Lockout** — Maintains login lockout information in the database so that restarting the application does not immediately bypass an active lockout.

### Encryption & Security
- **AES-256-GCM Encryption** — Encrypts credential information before database storage.
- **PBKDF2-HMAC-SHA256** — Provides master-password-based encryption key derivation.
- **Secure Random Generation** — Generates cryptographic salts and encryption nonces.
- **Byte-Array Encryption** — Supports encrypting binary data for encrypted backup files.
- **Byte-Array Decryption** — Supports decrypting backup data using the corresponding backup password.
- **Authenticated Encryption** — Uses AES-GCM authentication to help detect unauthorized modifications to encrypted backup contents.
- **Secure Notes Encryption** — Encrypts note titles, contents, and categories before storing them in the database.
- **Encrypted Credential Fields** — Encrypts credential titles, usernames, passwords, URLs, categories, and notes before database storage.
- **Sensitive Memory Cleanup** — Clears temporary password copies and sensitive byte arrays after backup processing.
- **Temporary File Cleanup** — Attempts to remove temporary database snapshots, decrypted files, and intermediate backup files after processing.
- **Login Attempt Tracking** — Tracks unsuccessful master password authentication attempts.
- **Brute-Force Protection** — Temporarily blocks further login attempts after five consecutive failed attempts.
- **Timed Login Lockout** — Applies a five-minute lockout after the maximum number of failed attempts is reached.
- **Persistent Lockout State** — Stores the failed attempt count and lockout expiration time in SQLite.
- **Lockout Expiration** — Allows authentication attempts again after the lockout period expires.

### Master Password Authentication
- **Master Password Setup** — Provides the initial master password setup process.
- **Vault Unlocking** — Allows users to authenticate before accessing the vault.
- **Password Validation** — Checks master password requirements during setup.
- **Failed Login Tracking** — Records unsuccessful attempts to unlock the vault.
- **Maximum Login Attempts** — Allows up to five consecutive failed login attempts before temporarily blocking access.
- **Temporary Account Lockout** — Locks the login interface for five minutes after reaching the failed attempt limit.
- **Lockout Countdown** — Displays the remaining lockout time in the login interface.
- **Persistent Lockout Protection** — Retains the lockout state in SQLite, including when the application is restarted.
- **Automatic Lockout Expiration** — Re-enables login after the five-minute lockout period ends.
- **Failed Attempt Reset** — Resets the failed login counter after successful authentication.

### Dashboard
- **Credential Statistics** — Displays an overview of saved credentials.
- **Category Statistics** — Shows the number of credential categories.
- **Activity Overview** — Displays activity record statistics.
- **Recent Credentials** — Shows recently added credential entries.
- **Recent Activity** — Displays recent vault activities directly on the dashboard.
- **Activity Timestamps** — Shows formatted dates and times for recorded activity.
- **Favorites Statistics** — Adds a dashboard overview of favorited credentials.
- **Favorite Credentials Section** — Provides quick access to selected favorite credentials.
- **Quick Access Actions** — Allows users to view favorite credential details and copy passwords through the dashboard.
- **Dashboard Refresh** — Allows dashboard information to be refreshed.

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

### Advanced Password Health Checker
- **Password Health Analysis Engine** — Introduces a dedicated `PasswordHealthChecker` service for analyzing saved vault credentials.
- **Individual Password Evaluation** — Analyzes each saved credential and generates an individual security score ranging from 0 to 100.
- **Password Strength Classification** — Classifies analyzed passwords as Strong, Medium, or Weak.
- **Password Length Detection** — Identifies passwords shorter than the recommended minimum length of 12 characters.
- **Character Variety Detection** — Checks for uppercase letters, lowercase letters, numbers, and special characters.
- **Common Password Detection** — Identifies selected commonly used and predictable passwords through a built-in local password list.
- **Reused Password Detection** — Compares saved passwords and identifies exact password matches across multiple accounts.
- **Security Score Calculation** — Calculates an overall vault security score using the average scores of analyzed credentials.
- **Password Security Penalties** — Applies score reductions for reused and short passwords and limits scores for detected common passwords.
- **Password Issue Identification** — Generates individual issue descriptions for passwords that do not meet the current security checks.
- **Personalized Security Recommendations** — Provides suggestions based on the identified issues of individual passwords.
- **Vault Security Statistics** — Calculates the total number of analyzed passwords, strong passwords, medium passwords, weak passwords, reused passwords, and common passwords.
- **Local Password Analysis** — Performs password health checks locally without sending passwords to external websites or services.
- **Password Analysis Results** — Returns structured analysis results without including plaintext passwords in the result objects.
- **Health Report Generation** — Produces a consolidated report containing the overall score, password statistics, individual results, and recommendations.
- **Immutable Analysis Results** — Uses Java records and immutable result lists for organizing password analysis information.
- **Refreshable Analysis** — Allows password health information to be recalculated using the currently saved vault credentials.

### Category Management
- **Credential Categories** — Supports organizing credentials using category information.
- **Category Structure** — Establishes the foundation for grouping and managing saved credentials.

### Activity History
- **Activity Records** — Displays recorded vault activities.
- **Activity Descriptions** — Shows information about logged actions.
- **History Interface** — Provides a dedicated page for reviewing activity records.
- **Generic Activity Logging** — Records credential-related actions without including credential titles in new activity descriptions.
- **Recycle Bin Activity Logging** — Records credential restoration, permanent deletion, and Recycle Bin emptying actions.
- **Secure Notes Activity Logging** — Records secure note creation, editing, deletion, and pinning actions.

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
- **Automatic Vault Locking** — Automatically locks the vault after the configured period of inactivity, with a default timeout of five minutes.
- **Automatic Lock Options** — Provides Never, 1 minute, 5 minutes, 10 minutes, 15 minutes, and 30 minutes options.
- **Persistent Auto-Lock Preferences** — Saves the selected automatic lock duration using Java Preferences.
- **Application Information** — Displays application and security-related information.
- **Security Information** — Displays information about the application's password hashing, encryption, and database storage.
- **Backup Management** — Provides access to encrypted backup creation and restoration.
- **Backup Password Confirmation** — Requires users to enter and confirm their backup password before creating a backup.
- **Backup File Selection** — Uses a file chooser to select the destination for new backups and existing backup files.
- **Backup Extension Handling** — Automatically adds the `.svb` extension when it is missing from a selected backup filename.
- **Restoration Confirmation** — Warns users that restoring a backup replaces the current database before proceeding.
- **Backup Error Handling** — Displays error messages when backup creation, validation, or restoration fails.
- **Login Transition Request** — Invokes the application's lock action after successful restoration to request a return to the login screen.

### Vault Management
- **Vault Panel** — Provides a dedicated interface for accessing and organizing stored credentials.
- **Credential Search** — Supports searching saved credentials by title, username, category, and notes.
- **Favorites Management** — Allows users to mark and unmark credentials as favorites.
- **Favorites Filtering** — Provides options to display all credentials, favorites only, or non-favorite credentials.
- **Credential Sorting** — Supports sorting saved credentials by newest, oldest, title, username, and category.
- **Favorite Indicators** — Displays favorite status directly in the credential table.
- **View Credentials** — Allows users to view saved credential details.
- **Edit Credentials** — Supports modifying existing credential entries.
- **Soft Delete Support** — Moves deleted credentials into the Recycle Bin instead of immediately removing them from the database.
- **Copy Password** — Allows users to copy a saved credential's password to the clipboard.
- **Clipboard Auto-Clear** — Attempts to clear copied passwords from the clipboard after 30 seconds, provided the clipboard still contains the copied password.
- **Main Application Navigation** — Uses a centralized main frame to organize the application's different panels.

### Recycle Bin & Credential Recovery
- **Recycle Bin Panel** — Provides a dedicated interface for viewing and managing deleted credentials.
- **Deleted Credential Listing** — Displays credentials that have been moved into the Recycle Bin.
- **Deleted Credential Information** — Displays credential titles, usernames, categories, creation dates, and deletion dates.
- **Deleted Credential Tracking** — Stores the date and time when a credential is moved to the Recycle Bin.
- **Active Credential Filtering** — Excludes deleted credentials from the normal vault retrieval query.
- **Deleted Credential Retrieval** — Provides a database method for retrieving credentials currently in the Recycle Bin.
- **Credential Restoration** — Allows users to restore deleted credentials back into the active Vault.
- **Individual Permanent Deletion** — Allows users to permanently delete selected credentials from the Recycle Bin.
- **Empty Recycle Bin** — Provides an interface action for permanently deleting all credentials in the Recycle Bin.
- **Deletion Confirmation** — Displays confirmation dialogs before permanently deleting individual credentials or emptying the entire Recycle Bin.
- **Recycle Bin Count** — Displays the number of deleted credentials and provides a database method for counting them.
- **Refreshable Recycle Bin** — Allows users to refresh the deleted credential list.
- **Favorite Preservation** — Retains the original favorite status when credentials are moved into and restored from the Recycle Bin.
- **Activity Logging** — Records restoration and permanent deletion actions.
- **Database Migration Support** — Adds the `deleted_at` column to existing credential tables without requiring existing credentials to be deleted.
- **Database Integration** — Connects the Recycle Bin interface directly to the existing SQLite database operations.
- **Main Navigation Integration** — Adds Recycle Bin as a dedicated page in the application's sidebar.

### Encrypted Backup & Restoration
- **Encrypted Backup Creation** — Allows users to create encrypted backup files of their SecureVault database.
- **Separate Backup Password** — Uses a dedicated backup password instead of directly relying on the master password.
- **Backup Password Validation** — Requires a minimum backup password length of 12 characters.
- **Backup Password Confirmation** — Requires the backup password to be entered twice during backup creation to reduce the chance of typing errors.
- **SQLite Database Snapshot** — Creates a consistent database snapshot before encryption.
- **AES-256-GCM Backup Encryption** — Encrypts the database snapshot before saving it as a backup file.
- **PBKDF2-HMAC-SHA256 Key Derivation** — Uses the existing cryptographic service to derive the backup encryption key from the supplied backup password.
- **Random Salt and Nonce Generation** — Uses randomly generated cryptographic values during backup encryption.
- **`.svb` Backup Format** — Stores encrypted backup information in a dedicated SecureVault backup file format.
- **Backup Format Versioning** — Uses backup format version 1 to identify and validate the backup container.
- **Backup File Validation** — Checks the backup file's signature, format version, salt length, encrypted data length, actual file size, and unexpected trailing data.
- **Maximum Backup Size Check** — Rejects backup files that exceed the configured maximum supported encrypted data size.
- **Backup Decryption** — Decrypts backup contents using the correct backup password.
- **Backup Password Verification** — Uses authenticated decryption to reject incorrect passwords or corrupted encrypted contents.
- **Database Snapshot Validation** — Checks the restored SQLite database before replacing the active database.
- **Database Integrity Checking** — Performs SQLite integrity checks and verifies required database tables and columns.
- **Safe Database Restoration** — Uses a staged database file and rollback protection when replacing the existing database.
- **Database Connection Reopening** — Reopens the database connection after successfully installing the restored database.
- **Temporary Backup File Handling** — Writes a completed backup to a temporary file before moving it to the selected destination.
- **Existing File Protection** — Avoids intentionally overwriting an existing backup file at the selected destination.
- **Sensitive Memory Cleanup** — Uses an internal copy of the supplied backup password and clears that copy after processing, rather than directly clearing the caller's password array.
- **Temporary File Cleanup** — Attempts to remove temporary snapshot, intermediate backup, and decrypted database files after processing.
- **Backup Management Service** — Uses a dedicated `BackupService` class to handle backup creation, validation, and restoration.
- **Settings Integration** — Provides Create Encrypted Backup and Restore Encrypted Backup options directly from the Settings panel.
- **Restoration Confirmation** — Warns users that restoring a backup replaces the current database before proceeding.
- **Login Transition Request** — Requests a return to the login screen after successful restoration so the restored vault can be authenticated again.
- **Restored Vault Authentication** — Requires the user to authenticate using the master password associated with the restored database.
- **Secure Notes Backup Support** — Includes the secure notes database table and its stored encrypted data in SQLite database snapshots.
- **Recycle Bin Backup Support** — Includes deleted credential records and their deletion timestamps in database snapshots.

### User Interface
- **Login Interface** — Provides the initial authentication screen for accessing the vault.
- **Login Lockout Interface** — Temporarily disables login controls after five consecutive failed authentication attempts.
- **Lockout Countdown Display** — Shows the remaining time before the login interface becomes available again.
- **Dashboard Panel** — Provides the main dashboard with credential statistics, favorite credentials, recent credentials, and recent activity.
- **Credential Entry Dialog** — Provides a dedicated dialog for preparing credential entries.
- **Password Generator Panel** — Provides a dedicated interface for generating and evaluating passwords.
- **History Panel** — Displays recorded vault activities.
- **Category Panel** — Provides an interface for credential category organization.
- **Security Panel** — Provides the security analysis interface.
- **Password Health Panel** — Provides a dedicated dashboard for reviewing password health, overall security scores, password statistics, individual analysis results, and recommendations.
- **Password Health Navigation** — Integrates Password Health into the main application's sidebar navigation.
- **Password Health Database Integration** — Retrieves saved credentials through the existing database connection and secret key for local analysis.
- **Password Health Refresh** — Automatically refreshes the analysis when the Password Health page is opened and provides a manual refresh action.
- **Secure Notes Panel** — Provides the interface for creating, editing, searching, pinning, and deleting encrypted notes.
- **Recycle Bin Panel** — Provides the interface for viewing, restoring, and permanently deleting removed credentials.
- **Settings Panel** — Provides an interface for application settings, automatic locking, encrypted backup creation, and backup restoration.
- **Main Frame** — Serves as the primary application window and navigation structure, including Password Health and Recycle Bin navigation.
- **Theme Component** — Centralizes the application's visual theme.
- **Vault Panel** — Provides the main credential management interface.

## Planned Features

The following components are planned for future updates or further development:

- **Password Expiration Tracking** — Track password age using password modification dates and identify credentials that may need updating.
- **Expanded Security Recommendations** — Introduce additional security checks and recommendations.
- **Breach Detection** — Explore privacy-preserving password breach checking without transmitting actual passwords.
- **Advanced Search & Filters** — Expand credential filtering and search capabilities.
- **Custom Folders & Tags** — Introduce additional ways to organize saved credentials.
- **Credential Details Improvements** — Expand the information and actions available when viewing individual credentials.
- **Clipboard Security Improvements** — Further improve clipboard handling and sensitive-data cleanup.
- **Two-Factor Authentication** — Add an additional authentication step when unlocking the vault.
- **Vault Analytics** — Expand dashboard statistics and introduce additional visual reports.
- **Import & Export** — Support importing and exporting credential information with appropriate security precautions.
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
│                   │   ├── PasswordHealthChecker.java
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
│                       ├── RecycleBinPanel.java
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

SecureVault is being developed gradually. Its current implementation includes the core application structure, database integration, encryption services, master password authentication, dashboard, password generator, credential entry form, category management, activity history interface, password security analysis, advanced password health analysis, encrypted backup and restoration, secure note management, and Recycle Bin functionality.

The Password Generator uses a dedicated Password Strength Analyzer to evaluate generated passwords and provide improvement recommendations. The Security Center also uses the same analyzer to identify weak passwords, detect reused passwords, calculate individual password scores, and display security recommendations.

The Advanced Password Health Checker introduces a separate analysis service that evaluates saved vault credentials. It calculates individual password scores, checks password length and character variety, identifies selected common passwords, detects exact password reuse across accounts, and generates recommendations for identified issues.

The Password Health dashboard displays the overall vault security score, total analyzed passwords, strong passwords, medium passwords, weak passwords, reused passwords, and common passwords. It also provides an individual password analysis table and a dedicated recommendations section. The dashboard retrieves credentials using the existing database connection and encryption key, and its analysis can be refreshed when needed.

The Password Health Checker currently uses local heuristic checks and a built-in list of selected common passwords. Its overall score is calculated from the average individual password scores. Password reuse and password length affect individual scores through additional penalties. These results are intended as practical security indicators rather than formal cryptographic security measurements.

Password analysis is performed locally. The health analysis result objects contain credential identifiers, account titles, scores, classifications, detected issues, and recommendations rather than plaintext password values. Passwords are not sent to external checking services by this feature.

The Password Generator uses a dedicated Password Strength Analyzer to evaluate generated passwords and provide improvement recommendations. The Security Center also uses the analyzer to identify weak passwords, detect reused passwords, calculate individual password scores, and display security recommendations.

The Vault Management interface supports credential favorites, filtering between all, favorite, and non-favorite entries, expanded credential searching, and sorting by different credential fields. Users can also copy saved passwords to the clipboard, with an automatic clearing attempt after 30 seconds.

The Dashboard includes favorite credential statistics and a Quick Access section for viewing and accessing favorite credentials. It also displays recently added credentials and recent vault activity, allowing users to review important account and activity information from the main dashboard.

Database updates support favorite credentials and soft deletion while preserving existing credential records. The database migration checks for missing columns and adds them without intentionally replacing existing data. New credential activity descriptions use generic action labels instead of including credential titles.

The application includes automatic vault locking after the configured period of inactivity, with a default timeout of five minutes, as well as a manual locking option through the Settings panel. Users can select from several inactivity durations, including disabling automatic locking, and their selection is saved using Java Preferences.

The login authentication system now includes failed-attempt tracking and temporary lockout protection. After five consecutive unsuccessful master password attempts, the login interface temporarily blocks further authentication attempts for five minutes. The remaining lockout time is displayed through a countdown, and the login controls become available again when the lockout period expires.

Login security information is stored in the SQLite database so that an active lockout can remain in effect even if the application is restarted. After successful authentication, the failed login counter is reset. This provides an additional layer of protection against repeated password guessing attempts.

The Encrypted Backup feature uses a dedicated backup service that creates a SQLite database snapshot and encrypts it using AES-256-GCM. Users can provide a separate backup password, and the resulting encrypted backup is saved in the `.svb` file format.

The backup system uses a versioned file structure containing a file signature, format version, salt, and encrypted database data. Backup validation checks the container structure, expected format version, data lengths, and unexpected trailing data. It also checks that the actual file size matches the length declared inside the backup.

Backup creation uses a temporary intermediate file before moving the completed backup to its destination. Existing files are not intentionally overwritten. The backup service also uses an internal password copy and clears temporary password and byte-array data after processing.

The backup restoration process allows users to select an existing `.svb` file, validate its structure, enter the backup password, and decrypt its contents. Before installation, the restored database is checked for SQLite integrity and required database structures. The restoration process uses a staged database file and rollback protection to help protect the existing database if installation fails.

The database restoration process reopens its connection after successfully installing the restored snapshot. The Settings panel then invokes the application's lock action to request a return to the login screen. The complete login transition and authentication flow after restoration still require integration testing with the main application frame.

The Settings panel includes both Create Encrypted Backup and Restore Encrypted Backup options. Backup creation requires users to enter and confirm a separate password of at least 12 characters. Restoration displays a warning before replacing the active database and provides error messages when the selected backup cannot be validated or restored.

The Secure Notes feature introduces a dedicated note management system integrated into the main application. Users can create, edit, search, pin, unpin, and delete private notes. Each note supports a title, content, and category, along with creation and last-updated timestamps.

Secure note titles, contents, and categories are encrypted before being stored in the dedicated `secure_notes` SQLite table. The Notes interface also displays total and pinned note statistics, supports searching by title and category, and places pinned notes before unpinned notes. Note-related actions are recorded in the activity history.

Secure Notes are stored in the same SQLite database used by the vault. As a result, the existing database snapshot mechanism includes the secure notes table and its encrypted data when creating database backups.

The Recycle Bin feature extends credential deletion by using soft deletion. Instead of immediately deleting credentials, the database records a deletion timestamp. Deleted credentials are retrieved separately from active vault entries, while their original information and favorite status are preserved.

The dedicated Recycle Bin interface displays deleted credentials along with their usernames, categories, creation dates, and deletion dates. Users can restore selected credentials to the active Vault, permanently delete individual entries, or empty the entire Recycle Bin. Confirmation dialogs are provided before irreversible deletion actions.

Recycle Bin functionality is integrated into the main application navigation through a dedicated sidebar entry. Its interface retrieves and refreshes deleted credential information using the existing database operations. Recycle Bin records are also included in SQLite database snapshots used by the encrypted backup system.

The UI structure includes dedicated components for the Dashboard, Vault, Categories, Password Generator, Activity History, Security, Password Health, Secure Notes, Recycle Bin, Settings, Login, and Main Application Frame, along with a centralized Theme component.

The latest Password Health development adds a dedicated `PasswordHealthChecker` service and connects the `PasswordHealthPanel` to the existing database and secret key through the main application frame. The panel is accessible through the sidebar and refreshes its analysis when opened.

Further development will focus on improving the application's security, expanding its functionality, and testing existing features.

## Security Notice

SecureVault is an educational project and has not undergone a professional security audit. Its encryption implementation and password strength analysis are intended for learning and development purposes. The password strength analyzer uses local heuristic checks and does not verify whether passwords have appeared in data breaches.

The Advanced Password Health Checker uses a limited built-in common-password list and heuristic scoring rules. It does not provide an exhaustive assessment of password predictability, entropy, or real-world account compromise. Exact password reuse detection identifies identical saved password strings but does not detect every variation of a reused password.

Password age analysis is not currently implemented. The credential creation timestamp should not be treated as the date a password was last changed.

Clipboard auto-clear is best-effort and cannot guarantee removal from operating system clipboard history or third-party clipboard managers.

The login lockout feature provides a basic defense against repeated master password attempts by limiting consecutive failed attempts and applying a temporary five-minute lockout. It should not be considered a complete defense against all brute-force attacks or other forms of unauthorized access.

Encrypted backup creation and restoration have been implemented in the project code. However, backup recovery, database replacement, rollback behavior, database connection reopening, and application login after restoration should be thoroughly tested before relying on the feature for important data.

Backup file validation checks the structure and declared lengths of the backup container. It does not establish that the password is correct or that the encrypted database contents are valid. Successful decryption and database integrity validation are separate restoration steps.

Temporary file deletion and sensitive-memory cleanup are best-effort measures. They do not guarantee that sensitive data has been removed from every operating system cache, storage device, or memory location.

Secure Notes encrypts note titles, contents, and categories before database storage. However, the overall application and its security mechanisms still require thorough testing and review.

The Recycle Bin supports restoring deleted credentials and permanently deleting selected or all recycled entries. Permanent deletion cannot be undone. Users should carefully review confirmation dialogs before proceeding.

Avoid storing real or sensitive credentials or private information until the application has been thoroughly tested and reviewed.

## Purpose

This project is part of my journey as an Information Technology student. It serves as an opportunity to improve my Java programming skills while exploring database management, application security, encryption, backup and recovery systems, secure data storage, password analysis, and desktop software development.
