## README Updates — Login Security

### 1. Update the existing `Master Password Authentication` section

Add these bullets without removing the existing ones:

- **Failed Login Attempt Tracking** — Tracks unsuccessful master-password authentication attempts.
- **Maximum Login Attempts** — Limits users to five consecutive failed master-password attempts before activating a temporary lockout.
- **Automatic Login Lockout** — Temporarily prevents additional login attempts after reaching the configured failed-attempt limit.
- **Five-Minute Lockout Duration** — Applies a five-minute lockout after five unsuccessful authentication attempts.
- **Persistent Lockout State** — Stores failed-attempt counts and lockout expiration timestamps in the SQLite database.
- **Restart-Protected Lockout** — Preserves an active login lockout even when the application is closed and reopened.
- **Login Countdown Timer** — Displays the remaining lockout time on the login screen.
- **Login Input Protection** — Disables the master-password fields and unlock button while the lockout is active.
- **Automatic Lockout Expiration** — Re-enables login after the lockout period expires.
- **Failed Attempt Reset** — Resets the failed-login counter after successful master-password authentication or lockout expiration.

### 2. Add this new section after `Master Password Authentication`

### Login Security & Brute-Force Protection

- **Login Security Database Table** — Introduces a dedicated `login_security` SQLite table for storing failed login attempts and lockout information.
- **Failed Attempt Persistence** — Keeps track of unsuccessful authentication attempts between application sessions.
- **Lockout Timestamp Storage** — Records the expiration time of an active login lockout using a timestamp.
- **Automatic Lockout Checking** — Checks whether a lockout is active before allowing another authentication attempt.
- **Configurable Attempt Limit** — Uses a maximum of five failed login attempts before applying a temporary lockout.
- **Temporary Authentication Restriction** — Prevents additional login attempts for five minutes after the attempt limit is reached.
- **Login Security Reset** — Provides database methods for clearing failed attempts and expired lockout information.
- **Database Migration Compatibility** — Creates the login security table without intentionally deleting existing vault records.
- **Login Interface Integration** — Connects the login screen to the database login security methods.
- **Lockout Countdown Display** — Updates the remaining lockout time on the login screen.
- **Authentication Protection** — Checks the lockout status before attempting to unlock an existing vault.

### 3. Add these bullets to the existing `Database Integration` section under `Core Application`

- **Login Security Storage** — Adds a dedicated SQLite table for tracking failed login attempts and temporary lockout information.
- **Persistent Authentication State** — Stores login security information in the local database.
- **Login Security Database Methods** — Provides methods for recording failed attempts, checking lockout expiration, and resetting login failures.

### 4. Add these bullets to the existing `Login Interface` section under `User Interface`

- **Login Lockout Integration** — Connects the login interface to the persistent login security system.
- **Failed Attempt Feedback** — Displays the remaining number of allowed login attempts after unsuccessful authentication.
- **Lockout Countdown Interface** — Displays a live countdown while login access is temporarily restricted.
- **Disabled Login Controls** — Prevents password entry and login submissions during an active lockout.

### 5. Add this paragraph to the `Project Status` section

The latest Security Hardening update introduces a persistent login protection mechanism. SecureVault now tracks unsuccessful master-password authentication attempts using a dedicated `login_security` table in SQLite. After five failed attempts, the login interface applies a temporary five-minute lockout. The lockout expiration timestamp is stored in the database, allowing the restriction to remain active even if the application is restarted. During an active lockout, the login screen disables its password fields and unlock button while displaying a countdown of the remaining time. Failed login information is reset after successful authentication or when the lockout expires.

### 6. Add this paragraph to the `Security Notice` section

The login protection mechanism introduces a temporary lockout after repeated unsuccessful master-password attempts. This is an additional application-level safeguard and should not be considered a complete defense against every brute-force attack or unauthorized modification of local database files. The login security implementation, persistence behavior, and lockout expiration should be tested thoroughly.
