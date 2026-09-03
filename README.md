# Swim Nutrition App

A personalized sports nutrition and meal planning Android app designed specifically for adolescent competitive swimmers and high-performance athletes.

## Features

- **Personalized Meal Plans**: AI-powered nutrition plans based on training load, goals, and preferences
- **Training Load Integration**: Adjusts nutrition recommendations based on workout intensity and schedule
- **Competition Day Support**: Specialized fueling strategies for meets and travel
- **Grocery Management**: Automated shopping lists organized by category
- **Progress Tracking**: Monitor adherence and adjust plans based on feedback
- **Multi-Provider AI Support**: Switch between OpenAI, Anthropic, and open-source models

## Tech Stack

- **Platform**: Android (Kotlin)
- **UI Framework**: Jetpack Compose
- **Backend**: Supabase (PostgreSQL + Auth + Real-time)
- **AI Services**: Multi-provider support (OpenAI, Anthropic, Open Source)
- **Architecture**: MVVM with Clean Architecture principles

## Project Structure

```
swim-nutrition-app/
├── app/
│   ├── src/main/
│   │   ├── java/com/swimnutrition/app/
│   │   │   ├── agent/              # AI agent implementation
│   │   │   ├── data/               # Data layer (repositories, services)
│   │   │   ├── domain/             # Domain models and use cases
│   │   │   └── ui/                 # UI layer (screens, navigation, theme)
│   │   ├── res/                    # Android resources
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── .devin/
│   └── skills/                     # Agent skills configuration
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## Getting Started

### Prerequisites

- Android Studio Hedgehog or later
- JDK 17
- Android SDK (API 34)
- Supabase account (free tier)

### Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd swim-nutrition-app
   ```

2. **Configure Android SDK**
   - Update `local.properties` with your Android SDK path:
   ```
   sdk.dir=/Users/<your-username>/Library/Android/sdk
   ```

3. **Set up Supabase**
   - Create a new project at [supabase.com](https://supabase.com)
   - Create the following tables in your Supabase database:
     - `user_profiles`
     - `meal_plans`
     - `nutrition_tracking`
   - Get your Supabase URL and anon key from project settings

4. **Configure API Keys**
   - Create `app/src/main/secrets.properties`:
   ```properties
   SUPABASE_URL=your-supabase-url
   SUPABASE_ANON_KEY=your-supabase-anon-key
   OPENAI_API_KEY=your-openai-key
   ANTHROPIC_API_KEY=your-anthropic-key
   ```

5. **Build and Run**
   - Open the project in Android Studio
   - Sync Gradle files
   - Run on an emulator or physical device

## Core Features

### Nutrition Agent

The app includes a sophisticated AI agent that:

- Generates personalized meal plans based on user profiles
- Adjusts recommendations based on training intensity
- Provides competition-day nutrition strategies
- Handles travel scenarios with hotel-friendly options
- Learns from user feedback to improve recommendations

### User Profile Management

Comprehensive profile tracking including:
- Physical characteristics (age, weight, height)
- Training load and schedule
- Food preferences and cultural considerations
- Cooking constraints and skill level
- Goals and past nutrition attempts

### Meal Planning

Each meal plan includes:
- Daily nutrition summary with macro targets
- Detailed meal-by-meal breakdown
- Grocery lists organized by category
- Prep strategies for busy schedules
- Educational notes about nutrition principles

## Architecture

The app follows Clean Architecture principles:

- **Domain Layer**: Business logic and models
- **Data Layer**: Repository implementations and external services
- **UI Layer**: Compose screens and navigation
- **Agent Layer**: AI integration and decision making

## Development

### Building the Project

```bash
./gradlew assembleDebug
```

### Running Tests

```bash
./gradlew test
```

### Code Style

The project follows Kotlin coding conventions and Material Design 3 guidelines.

## License

[Your License Here]

## Contributing

Contributions are welcome! Please read our contributing guidelines before submitting pull requests.

## Support

For issues and questions, please open an issue on GitHub.