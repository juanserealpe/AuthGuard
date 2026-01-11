# AuthGuard

A secure and lightweight authentication plugin for Minecraft Paper servers. AuthGuard provides robust player registration and login functionality with password hashing, session management, and optional spawn point control for unauthenticated players.

## Features

- **Secure Authentication**: BCrypt password hashing with configurable work factor
- **Session Management**: In-memory session tracking for authenticated players
- **Spawn Control**: Optional spawn point for unauthenticated players
- **LuckPerms Integration**: Automatic rank assignment upon registration
- **SQLite Database**: Lightweight, file-based storage with HikariCP connection pooling
- **Async Operations**: Non-blocking database operations for optimal performance
- **Fully Configurable**: Customizable messages and settings via config.yml
- **Event Protection**: Prevents unauthenticated players from interacting with the server

## Requirements

- **Minecraft Server**: Paper 1.21.1 or higher
- **Java**: Version 21 or higher
- **Required**: LuckPerms (for rank management)

## Installation

1. Download the latest `AuthGuard-X.X.X.jar` from [Releases](https://github.com/YOUR-USERNAME/AuthGuard/releases)
2. Place the JAR file in your server's `plugins/` folder
3. Restart or reload your server
4. Configure the plugin by editing `plugins/AuthGuard/config.yml`
5. Reload the config with `/reload confirm` or restart the server

## Commands

Command: /register
Description: Register a new account
Permission: None
Usage: /register <password>

Command: /login
Description: Login to your account
Permission: None
Usage: /login <password>

Command: /setspawn
Description: Set spawn for unauthenticated players
Permission: authguard.spawn.manage
Usage: /setspawn

Command: /delspawn
Description: Disable spawn teleportation
Permission: authguard.spawn.manage
Usage: /delspawn

## Permissions

- `authguard.admin` - Administrative access (default: op)
- `authguard.spawn.manage` - Manage spawn locations (default: op)

## Configuration

Default `config.yml`:

```yaml
database:
  file: "authguard.db"

spawn:
  enabled: false
  world: ""
  x: 0.0
  y: 0.0
  z: 0.0
  yaw: 0.0
  pitch: 0.0

messages:
  usage-register: "&cYou are not registered. Use: /register <password>"
  registration-success: "&aSuccessfully registered! Welcome!"
  usage-login: "&cPlease login: /login <password>"
  login-success: "&aSuccessfully logged in!"
  invalid-password: "&cInvalid password. Try again."
  not-registered-command-blocked: "&cYou must register first: /register <password>"
  registered-command-blocked: "&cYou must login first: /login <password>"
  spawn-set-success: "&aSpawn set at your current location."
  spawn-deleted-success: "&aSpawn disabled successfully."
  no-permission: "&cYou don't have permission to use this command."
  already-authenticated: "&cYou are already authenticated."
  internal-error: "&cAn error occurred. Please contact an administrator."
  input-console-command-error: "&cThis command can only be used by players."

default-rank: "player"
```

## How It Works

### Registration Flow
1. Player joins the server
2. If not registered, they receive periodic reminders to use `/register <password>`
3. Upon registration:
  - Password is hashed with BCrypt (work factor: 12)
  - User data is stored in SQLite database
  - Player is automatically authenticated
  - Optional: LuckPerms rank is assigned

### Login Flow
1. Registered player joins the server
2. They receive reminders to use `/login <password>`
3. Upon successful login:
  - Password is verified against stored hash
  - Session is created
  - Last login timestamp is updated
  - Optional: LuckPerms rank is re-assigned

### Security Features
- Players cannot move, chat, interact, break/place blocks, or open inventories until authenticated
- Only `/register` or `/login` commands are available to unauthenticated players
- Passwords are never stored in plain text
- Sessions are cleared on logout/disconnect

## Building from Source

```bash
# Clone the repository
git clone https://github.com/juanserealpe/AuthGuard.git
cd AuthGuard

# Build with Maven
mvn clean package

# The compiled JAR will be in target/authguard-1.0-SNAPSHOT.jar
```

## Dependencies

- **Paper API** 1.21.1-R0.1-SNAPSHOT
- **jBCrypt** 0.4 (bundled)
- **HikariCP** 6.2.1 (bundled)
- **LuckPerms API** 5.4 

## Database Schema

```sql
CREATE TABLE users (
    uuid TEXT PRIMARY KEY,
    username TEXT NOT NULL,
    password_hash TEXT NOT NULL,
    rank TEXT NOT NULL,
    created_at TEXT NOT NULL,
    last_login TEXT NOT NULL
);
```

## Roadmap

- [ ] MySQL/PostgreSQL support
- [ ] Email verification
- [ ] Two-factor authentication (2FA)
- [ ] Password reset system
- [ ] Session timeout configuration
- [ ] Admin commands for user management
- [ ] Password strength requirements

## Contributing

Contributions are welcome! Please feel free to submit pull requests or open issues for bugs and feature requests.

### Development Setup

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Commit your changes: `git commit -m 'Add amazing feature'`
4. Push to the branch: `git push origin feature/amazing-feature`
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

If you encounter any issues or have questions:
- Open an [Issue](https://github.com/YOUR-USERNAME/AuthGuard/issues)
- Check existing issues for solutions
- Read the documentation carefully

## Credits

Developed by [TheNexoz/Juanserealpe](https://github.com/YOUR-USERNAME)

## Changelog

### v1.0.0 (2026-01-11)
- Initial release
- Basic registration and login system
- BCrypt password hashing
- SQLite database support
- Spawn management
- LuckPerms integration
- Configurable messages