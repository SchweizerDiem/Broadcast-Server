# 📡 Broadcast Server

A simple broadcast server that allows clients to connect, send messages, and have those messages broadcast to all connected clients.

This repository contains a lightweight Java-based broadcast server (see the `App/` directory for the application entry point). The README below gives quick start instructions, usage examples, and guidance for contributing and testing.

## ✨ Features
- Accept multiple client connections
- Broadcast messages from any client to every connected client
- Small and easy to run locally for testing and learning

## 📦 Requirements
- Java 8+ (Java 11+ recommended)
- Maven since the project was build on top of it.

## 🚀 Quick start

1. Inspect the App directory to find the main class and build instructions:
   - Look in `App/` for the application's entry point (Main class).

2. Build
    ```bash
    mvn clean package
   ```
    The packaged jar is typically under `target/`. Run it with:
	  ```bash
	  mvn exec:java -Dexec.mainClass="com.schweizer.app.BroadcastApp" -Dexec.args="broadcast-server start"
	  ```

## Configuration
- Port: commonly provided as a command-line argument or environment variable. See the project's main class for exact options.
- Maximum clients / timeouts / logging: check the server source for available configuration and adjust as necessary.

## Logging & Shutdown
- The server likely logs basic connection/events to stdout/stderr. Use Ctrl+C to stop a locally running server, or use whatever graceful shutdown mechanism the implementation provides.

## Development & Contributing
- Found a bug or want an enhancement? Please open an issue in this repository.
- Want to contribute code? Fork the repository, make your changes, and open a pull request. Include:
  - A clear description of the change
  - How to reproduce the issue (if fixing a bug)
  - Any tests or manual steps you used to verify the change

## 📝 To-do
- [ ] Add encryption for messages
- [ ] Add support for media send through the channel.

## License
- Still needs license!

## Contact
- For questions about this repository, open an issue or reach out via the GitHub profile at https://github.com/SchweizerDiem.

