# Swim Nutrition App - Setup Guide

## Quick Start

This guide will help you set up the Swim Nutrition Android app from scratch.

## Prerequisites

- **Android Studio**: Hedgehog (2023.1.1) or later
- **JDK**: Version 17
- **Android SDK**: API 34
- **Supabase Account**: Free tier at [supabase.com](https://supabase.com)
- **AI API Keys**: At least one of:
  - OpenAI API key (recommended)
  - Anthropic API key
  - Open source model endpoint

## Step 1: Clone and Open Project

```bash
cd /Users/sorencarlson/swim-nutrition-app
```

Open the project in Android Studio:
1. File → Open
2. Navigate to `/Users/sorencarlson/swim-nutrition-app`
3. Click "OK"

## Step 2: Configure Android SDK

Update `local.properties` with your Android SDK path:

```properties
sdk.dir=/Users/sorencarlson/Library/Android/sdk
```

If you don't know your SDK path:
- Android Studio → Preferences → Appearance & Behavior → System Settings → Android SDK
- Copy the "Android SDK Location" path

## Step 3: Set Up Supabase

### 3.1 Create Supabase Project

1. Go to [supabase.com](https://supabase.com)
2. Sign up/login
3. Click "New Project"
4. Choose organization and name your project
5. Set a strong password
6. Wait for project to be ready (~2 minutes)

### 3.2 Create Database Tables

1. Go to your Supabase project dashboard
2. Navigate to SQL Editor
3. Click "New Query"
4. Copy the contents of `supabase-schema.sql` from this project
5. Paste into the SQL Editor
6. Click "Run" to execute

### 3.3 Get API Credentials

1. In Supabase dashboard, go to Settings → API
2. Copy:
   - **Project URL** (e.g., `https://your-project.supabase.co`)
   - **anon/public key** (the long string under "Project API keys")

## Step 4: Configure API Keys

### Option A: Environment Variables (Recommended)

Add these to your system environment variables:

```bash
export SUPABASE_URL="https://your-project.supabase.co"
export SUPABASE_ANON_KEY="your-anon-key"
export OPENAI_API_KEY="sk-your-openai-key"
export ANTHROPIC_API_KEY="sk-ant-your-anthropic-key"
```

### Option B: Modify SecretsManager.kt

For development only, edit `app/src/main/java/com/swimnutrition/app/data/service/SecretsManager.kt`:

```kotlin
fun getSupabaseUrl(): String {
    return "https://your-project.supabase.co"
}

fun getSupabaseAnonKey(): String {
    return "your-anon-key"
}

fun getOpenAIApiKey(): String {
    return "sk-your-openai-key"
}

fun getAnthropicApiKey(): String {
    return "sk-ant-your-anthropic-key"
}
```

## Step 5: Get AI API Keys

### OpenAI (Recommended)

1. Go to [platform.openai.com](https://platform.openai.com)
2. Sign up/login
3. Navigate to API Keys
4. Click "Create new secret key"
5. Copy the key (starts with `sk-`)

### Anthropic

1. Go to [console.anthropic.com](https://console.anthropic.com)
2. Sign up/login
3. Navigate to API Keys
4. Click "Create Key"
5. Copy the key (starts with `sk-ant-`)

## Step 6: Build and Run

### 6.1 Sync Gradle

1. In Android Studio, click "Sync Project with Gradle Files"
2. Wait for dependencies to download

### 6.2 Create Virtual Device

1. Tools → Device Manager
2. Click "Create Device"
3. Choose a device (e.g., Pixel 6)
4. Choose a system image (API 34 recommended)
5. Click "Finish"

### 6.3 Run the App

1. Click the green "Run" button (▶️)
2. Choose your virtual device
3. Wait for the app to build and install

## Step 7: Test the App

1. **Home Screen**: You should see the main navigation
2. **Profile Setup**: Click "Setup Your Profile" and fill in your information
3. **Meal Plan**: Navigate to "View Today's Meal Plan" to generate a personalized plan
4. **Grocery List**: Check the automated shopping list
5. **Tracking**: Monitor your nutrition adherence

## Troubleshooting

### Build Errors

**"SDK location not found"**
- Check your `local.properties` file has the correct SDK path

**"Gradle sync failed"**
- Make sure you have internet connection
- Try File → Invalidate Caches → Invalidate and Restart

### Runtime Errors

**"Supabase connection failed"**
- Verify your Supabase URL and anon key are correct
- Check your Supabase project is active

**"AI API error"**
- Verify your API keys are valid
- Check you have available credits in your AI account
- Try switching AI providers in the app

### Database Issues

**"Table not found"**
- Make sure you ran the `supabase-schema.sql` script
- Check the SQL Editor for any execution errors

## Production Deployment

When you're ready for production:

1. **Security**: Never hardcode API keys in production
2. **Authentication**: Implement proper user authentication
3. **Error Handling**: Add comprehensive error handling
4. **Testing**: Write unit and integration tests
5. **Performance**: Optimize for battery and network usage
6. **Release**: Sign your APK and publish to Play Store

## Next Steps

- [ ] Implement user authentication with Supabase Auth
- [ ] Add push notifications for meal reminders
- [ ] Integrate with wearable devices for activity tracking
- [ ] Add social features for team nutrition planning
- [ ] Implement offline mode for areas with poor connectivity
- [ ] Add analytics to understand user behavior

## Support

For issues:
1. Check the troubleshooting section above
2. Review the Supabase and AI provider documentation
3. Open an issue on GitHub

## Additional Resources

- [Supabase Documentation](https://supabase.com/docs)
- [OpenAI API Documentation](https://platform.openai.com/docs)
- [Anthropic API Documentation](https://docs.anthropic.com)
- [Android Developers Guide](https://developer.android.com/guide)
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)