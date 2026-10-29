# Libra — Library Management System Website

A demo-ready website with HTML/CSS/JavaScript frontend and Core Java HTTP backend.

## Requirements
- Windows 10/11
- JDK 17 or newer (JDK 21 recommended)
- VS Code (optional; you can also run from PowerShell)

## Easiest way to run
1. Extract the ZIP to a normal folder (not inside the ZIP preview).
2. Double-click `run.bat`.
3. A terminal window starts the Java server. Keep it open.
4. Your browser should open `http://localhost:8080`. If not, open that address manually.
5. Use the website. To stop the server, press Ctrl+C in the terminal.

## If Windows blocks run.bat
Open PowerShell in this folder and run:
```powershell
New-Item -ItemType Directory -Force out
javac -d out backend\src\*.java
java -cp out LibraryManagementSystem
```
Then open http://localhost:8080.

## Demo data
Books: 101 Effective Java, 102 Clean Code, 103 Head First Java.
Members: 201 Aarav Sharma (Student), 301 Dr. Meera Singh (Faculty).

## Features
- Dashboard summary cards
- Add/search/list books
- Register student and faculty members
- Issue and return books
- Transaction history and due dates
- Fine calculation: Rs. 5 per overdue day
- Validation and readable error messages
- Responsive sidebar dashboard UI

## OOP concepts
Encapsulation, inheritance, abstraction, interface (`FineCalculator`), polymorphism, custom exception (`LibraryException`), try/catch.

## Architecture
- `frontend/index.html`, `style.css`, `app.js`: website UI
- `backend/src/*.java`: Core Java backend and HTTP API
- Java serves the frontend and API on port 8080.

## Limitation
This is a demo version with in-memory storage; data resets when the Java server stops. It uses no database.
