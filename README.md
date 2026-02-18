# Java Task Engine

A robust backend task management system built in Java, demonstrating core software engineering principles including MVC architecture, Gradle build automation, and unit testing.

## Overview
This repository contains a fully functional backend system designed to manage tasks and data workflows. It serves as a structural blueprint for backend application development, prioritizing clean code, testability, and modular design. The skills demonstrated here (Java, object-oriented design, automated building) are fundamental to developing scalable backend systems and enterprise software integrations.

## Architecture
The application uses the Model-View-Controller (MVC) architectural pattern:
* **Models**: Define the core data structures (Tasks, Entities) and handle state.
* **Controllers**: Manage the business logic and coordinate data flow between the view and the model.
* **Views**: Abstracted interfaces for output and user interaction.

## Tech Stack
* **Language**: Java
* **Build System**: Gradle (Wrapper included)
* **Testing**: JUnit for automated unit and integration tests

## Key Features
* **Modular Codebase**: Strict separation of concerns allows for easy scaling and feature additions.
* **Automated Builds**: Uses Gradle for dependency management and reproducible builds, ensuring the system can be deployed anywhere effortlessly.
* **Persistent Data**: Handles reading and writing to underlying data files (`data/tasks.txt`).

## Getting Started

### Prerequisites
- Java Development Kit (JDK) 11 or higher.

### Building and Running
You do not need to install Gradle globally. The repository includes the Gradle Wrapper.

```bash
# Build the project
./gradlew build

# Run the tests
./gradlew test
```

---
*Project Context: Originally developed as a software engineering proof-of-concept and refined to showcase backend architecture and build automation.*
