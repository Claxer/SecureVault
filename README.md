# SecureVault — Password & Credential Manager

SecureVault is a Java-based Password & Credential Manager designed to provide a centralized way to organize and protect account credentials. Built with **Java 25, Java Swing, SQLite, and Maven**, the project focuses on secure data storage, encryption, and credential organization through a modern dark-themed desktop application.

The project is currently under active development, with its core structure and initial components being implemented.

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

### Category Management
- **Credential Categories** — Supports organizing credentials using category information.
- **Category Structure** — Establishes the foundation for grouping and managing saved credentials.

## Planned Features

The following components are planned and will be added as development continues:

- **Master Password Authentication** — Initial vault setup and unlocking.
- **Credential Management** — Add, view, edit, delete, and search saved credentials.
- **Dashboard** — Overview of saved credentials and recent activity.
- **Password Generator** — Generate customizable passwords.
- **Security Analysis** — Identify weak and reused passwords.
- **Activity History** — Track important vault activities.
- **Settings** — Manage application preferences and vault controls.
- **Complete Desktop Interface** — Dark-themed navigation and fully integrated application pages.

## Technologies Used

- Java 25
- Java Swing
- SQLite
- SQLite JDBC
- Java Cryptography Architecture (JCA)
- AES-256-GCM
- PBKDF2-HMAC-SHA256
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

The complete desktop application is still under development, so not all planned features are currently available.

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
│                       └── CategoriesPanel.java
├── pom.xml
└── README.md
```

## Project Status

**Under Active Development**

SecureVault is being developed gradually, with the core application structure, database integration, encryption services, and initial category management components serving as the foundation.

Additional interface components, credential management tools, and security features will be introduced in future updates.

## Security Notice

SecureVault is an educational project and has not undergone a professional security audit. Its encryption implementation is intended for learning and development purposes. Avoid storing real or sensitive credentials until the application has been thoroughly tested and reviewed.

## Purpose

This project is part of my journey as an Information Technology student. It serves as an opportunity to improve my Java programming skills while exploring database management, application security, encryption, and desktop software development.
