# 📈 Millions – Stock Market Simulator

A JavaFX stock-market simulator where you grow a virtual portfolio against a live-ticking, news-driven market.

## Table of Contents
- [Overview](#overview)
- [Features](#features)
- [Getting Started](#getting-started)
- [How to Play](#how-to-play)
- [Project Structure](#project-structure)
- [Save Files](#save-files)
- [Technologies](#technologies)
- [Known Limitations](#known-limitations)
- [Future Work](#future-work)
- [Authors](#authors)

## Overview
Millions simulates an evolving stock market with sector-tagged news events, weekly price ticks, and a portfolio you actually have to manage. Tune the difficulty, ride the auto-advance ticker, or step through week by week. Save your progress to JSON and pick up exactly where you left off.

## Features
- **Live Market Table** — simulated stock prices with one-click buy and sell.
- **News Events** — sector-tagged news that nudges drift and volatility for a limited duration.
- **Portfolio & Stats** — net worth, cash, unrealized P/L, and player status, all bound to live data.
- **Stock Chart Dialog** — area chart of full price history with current price, last change, and all-time high/low.
- **Sell All** — one-click cash-out of every share you hold, behind a confirmation.
- **Transactions** — full ledger of buys and sells with re-openable receipts.
- **Difficulty Settings** — slider from 0.5x to 2.5x that scales market volatility and news frequency.
- **Onboarding Tutorial** — short multi-step walkthrough on first launch (skippable).
- **Save / Load** — JSON saves you can resume later, including stock price history.
- **Auto-Advance** — toggle the weekly ticker to let the market run hands-free.

## Getting Started

### Prerequisites
- Java 25 (matches `maven.compiler.source` in `pom.xml`)
- Apache Maven 3.x

JavaFX is pulled in via Maven, so you do not need to install it separately.

### Clone & Run
```bash
git clone https://github.com/Fredneyy/NTNU-IDATT2003-G15-Millions.git
cd NTNU-IDATT2003-G15-Millions

# Run the test suite
mvn clean test

# Launch the app
mvn javafx:run
```

### Running from IntelliJ
1. Open the project folder in IntelliJ.
2. If you don't see Maven targets, right-click `pom.xml` → **Add as Maven Project**.
3. Run `Launcher.main` — that's the JavaFX entry point. Do not run `App.main` directly, or you will hit module-path errors.

## How to Play
1. Launch the app and either start a new game or load a previous save from the Main Menu.
2. Walk through the onboarding tutorial, or hit **Skip tutorial** if you've seen it before.
3. Browse the **Market** tab to see all listed stocks. Use the search bar to filter by symbol or company.
4. Click any row to open the stock chart with full price history, current price, and all-time high/low.
5. Buy shares from the Market tab; check your holdings under **Portfolio**.
6. Watch the **News** tab for sector events that move prices. Use the settings cog in the header to tune difficulty.
7. Advance the week with the calendar button, or toggle **Auto-advance** for a live ticker.
8. Sell individual holdings from the Portfolio tab, or cash out everything with **Sell All**.
9. Use the save icon in the header to save your run. The exit icon prompts you to save before leaving.

## Project Structure
```
src/main/java/ntnu/idatt2003/group15/
├── model/         Domain logic (Player, Stock, Portfolio, NewsArchive, Exchange) — pure Java, no UI.
├── view/          JavaFX UI (GameView, MarketTableView, PortfolioTableView, dialogs, ...).
├── controller/    Mediators between view events and model updates
│                  (MainController, ExchangeController, PlayerController,
│                   PortfolioController, NewsController, SettingsController, ...).
└── utilities/     IO helpers (CsvParser, JsonParser, SaveGameUtil, LoadGameUtil, TaskUtil).
```

## Save Files
- **Save location** — chosen by you at save time, written as pretty-printed JSON.
- **Recent saves index** — `~/.millions/recent-saves.tsv` tracks the last few saves so the Main Menu can offer a "Continue" shortcut.

## Technologies
- **Java 25** + **JavaFX 25**
- **Maven** for builds and dependencies
- **JUnit 5** for unit tests
- **Ikonli** for vector icons (FontAwesome, Material Design)
- Hand-rolled JSON and CSV parsers for save files and seed data

## Known Limitations
- All market data is simulated. No live or historical real-world data.
- Saves are manual; there is no autosave.
- Active news effects are not persisted across save/load. Loading a save starts with a clean news feed.

## Future Work
- Database-backed autosave and leaderboards (SQLite).
- Real historical data via REST (e.g., Oslo Børs).
- Extended chart types (candlesticks, rolling averages).

## Authors
- **Olsen**
- **Torkildsen**
