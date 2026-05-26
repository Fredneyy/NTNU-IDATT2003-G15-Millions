# 📈 Millions – Stock Market Simulator
## Overview
Millions is a JavaFX-based stock market simulator where players can manage an investment portfolio, trade virtual stocks, track global market changes, and respond to dynamic news events. Built to mimic realistic market behaviors using robust underlying logic, this application provides an immersive and interactive environment for learning and experimenting with stock transactions.
## Features
- **Live Market Table:** Explore real-time simulated stock prices with integrated buy/sell functionality.
- **Dynamic News Events:** A living news archive that systematically affects stock prices globally or per sector.
- **Smart Filtering:** Winner/loser insights via auto-sorting and dynamically filtered statistics.
- **Portfolio Management:** Keep track of owned shares, evaluate current valuation against invested capital.
- **Transaction History:** Check all your past purchase and sale receipts.
- **Save/Load System:** JSON-based persistent save files so you can continue your millionaire journey anytime.
- **Interactive Visuals:** Dialog boxes, charts, and clear indicators for tracking trends.
## Getting Started
### Prerequisites
- Java 25 (or newer)
- JavaFX 25
- Apache Maven 3.x
### Installation & Run Instructions
1. **Clone the repository:**
   `ash
   git clone <repository_url>
   cd millions
   `
2. **Compile and Test:**
   Use Maven to run all unit tests and compile the project to ensure everything is working:
   `ash
   mvn clean test
   `
3. **Run the Application:**
   Start the game directly using the JavaFX Maven plugin:
   `ash
   mvn javafx:run
   `
## How to Play
1. Enter the game via the Main Menu, or load a prior save.
2. Visit the **Market** perspective to view all active stocks.
3. Observe the **News** alerts. They will give hints on which sectors might bounce or plummet.
4. Click on a stock to buy shares. Use your starting capital wisely!
5. Fast-forward or wait to see how prices develop, then check your **Portfolio** to evaluate your profit/loss.
6. Sell your shares for a profit, and repeat until you hit *Millions*!
## Project Structure
- model: Core logic, business objects (Player, Stock, Portfolio, NewsArchive), validation, and pure Java models without UI dependencies.
- iew: JavaFX GUI rendering, styles, and custom UI components (GameView, MarketTableView, and specialized prompt Dialogs).
- controller: The mediators connecting UI events to model updates (MainController, MarketController, PlayerController).
- utilities: Helpers for processing IO, such as CsvParser, JsonParser, Game Saving schemas, and tasks logic.
## Technologies Used
- **Java 25** & **JavaFX 25**
- **Maven:** Project management, dependency resolution, and build system.
- **JUnit 5:** Robust unit testing environment.
- **Ikonli:** For rendering vector icons directly in JavaFX (FontAwesome, MaterialDesign2).
- **JSON & CSV Parsing:** For initial application seed data and saving progress.
## Known Limitations
- Simulated stock data only (no real-world API integration yet).
- Save/load relies on manual triggering (no autosave system).
## Future Work
- Database integration (SQLite) for autosave and global leaderboards.
- Real historical data fetches via REST from local markets (e.g., Oslo Børs).
- Extended analytical charts (Candlesticks, historical rolling average).
## Authors
- **[Replace Name / Olsen]** 
- **[Replace Name / Torkildsen]**
