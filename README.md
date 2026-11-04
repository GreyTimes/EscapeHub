
# EscapeHub

**Collaborative Mystery Missions Platform Using Open Data**

EscapeHub is a web application where users participate individually
or in teams in interactive mystery missions.

Players investigate evidence, solve puzzles, explore multiple paths
and collaborate to uncover the truth.

## Technology Stack

- Java 25
- Spring Boot
- Gradle
- Spring Web
- JUnit 5
- PostgreSQL (planned)
- Docker (planned)

## Requirements

- JDK 25
- Git

## Running the Application

Windows:

    .\gradlew.bat bootRun

Linux/macOS:

    ./gradlew bootRun

## Running Tests

Windows:

    .\gradlew.bat test

Linux/macOS:

    ./gradlew test

## Building

Windows:

    .\gradlew.bat clean build

Linux/macOS:

    ./gradlew clean build

## Project Structure

The backend is organized into feature-based packages:

- user
- team
- mission
- puzzle
- evidence
- progress
- integration
- config
- common
- security

## Development Workflow

All development follows:

Issue → Branch → Commit → Push → Pull Request
→ Code Review → CI → Merge

Direct commits to main are not permitted.

## Team

EscapeHub is developed as part of the Software Engineering
Laboratory semester project.
