-- Swim Nutrition App - Supabase Database Schema

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- User Profiles Table
CREATE TABLE user_profiles (
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
    age INTEGER NOT NULL,
    sex VARCHAR(10) NOT NULL CHECK (sex IN ('MALE', 'FEMALE', 'OTHER')),
    height DECIMAL(5,2) NOT NULL, -- cm
    weight DECIMAL(5,2) NOT NULL, -- kg
    body_composition_goal VARCHAR(20) NOT NULL CHECK (body_composition_goal IN ('MAINTAIN_WEIGHT', 'GAIN_MUSCLE', 'LOSE_FAT', 'OPTIMIZE_PERFORMANCE')),
    sport_name VARCHAR(50) DEFAULT 'Swimming',
    sport_level VARCHAR(20) NOT NULL,
    swimming_events TEXT[] DEFAULT '{}',
    weekly_yardage INTEGER NOT NULL,
    intensity_distribution JSONB NOT NULL,
    key_workouts TEXT[] DEFAULT '{}',
    rest_days TEXT[] DEFAULT '{}',
    school_hours VARCHAR(20) NOT NULL,
    practice_times VARCHAR(20) NOT NULL,
    commute_time INTEGER NOT NULL,
    homework_load VARCHAR(10) CHECK (homework_load IN ('LIGHT', 'MODERATE', 'HEAVY')),
    bedtime VARCHAR(10) NOT NULL,
    wake_time VARCHAR(10) NOT NULL,
    sleep_quality VARCHAR(10) CHECK (sleep_quality IN ('POOR', 'FAIR', 'GOOD', 'EXCELLENT')),
    primary_goal VARCHAR(20) NOT NULL,
    calorie_target INTEGER,
    protein_target INTEGER,
    carb_target INTEGER,
    fat_target INTEGER,
    loved_foods TEXT[] DEFAULT '{}',
    disliked_foods TEXT[] DEFAULT '{}',
    cultural_practices TEXT[] DEFAULT '{}',
    religious_restrictions TEXT[] DEFAULT '{}',
    cooking_skill VARCHAR(15) CHECK (cooking_skill IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED')),
    available_equipment TEXT[] DEFAULT '{}',
    typical_cooking_time INTEGER NOT NULL,
    supplementation TEXT[] DEFAULT '{}',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Meal Plans Table
CREATE TABLE meal_plans (
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
    date DATE NOT NULL,
    total_calories INTEGER NOT NULL,
    total_protein INTEGER NOT NULL,
    total_carbs INTEGER NOT NULL,
    total_fats INTEGER NOT NULL,
    rationale TEXT NOT NULL,
    key_focus TEXT NOT NULL,
    meals JSONB NOT NULL,
    grocery_list JSONB NOT NULL,
    prep_strategy JSONB NOT NULL,
    education_note TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(user_id, date)
);

-- Nutrition Tracking Table
CREATE TABLE nutrition_tracking (
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
    meal_plan_id UUID REFERENCES meal_plans(id) ON DELETE CASCADE,
    date DATE NOT NULL,
    meal_type VARCHAR(15) NOT NULL CHECK (meal_type IN ('BREAKFAST', 'LUNCH', 'DINNER', 'SNACK', 'PRE_WORKOUT', 'POST_WORKOUT')),
    consumed BOOLEAN DEFAULT false,
    adherence_rating INTEGER CHECK (adherence_rating BETWEEN 1 AND 5),
    energy_level INTEGER CHECK (energy_level BETWEEN 1 AND 10),
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(user_id, date, meal_type)
);

-- User Feedback Table
CREATE TABLE user_feedback (
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
    meal_plan_id UUID REFERENCES meal_plans(id) ON DELETE CASCADE,
    appealing_meals TEXT[] DEFAULT '{}',
    unrealistic_meals TEXT[] DEFAULT '{}',
    confidence_level INTEGER CHECK (confidence_level BETWEEN 1 AND 10),
    anticipated_barriers TEXT[] DEFAULT '{}',
    learnings_from_previous_week TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Training Log Table
CREATE TABLE training_log (
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
    date DATE NOT NULL,
    training_type VARCHAR(20) NOT NULL,
    yardage INTEGER,
    intensity VARCHAR(10) CHECK (intensity IN ('EASY', 'MODERATE', 'HARD')),
    duration_minutes INTEGER,
    focus TEXT,
    energy_before INTEGER CHECK (energy_before BETWEEN 1 AND 10),
    energy_after INTEGER CHECK (energy_after BETWEEN 1 AND 10),
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(user_id, date)
);

-- Create indexes for better performance
CREATE INDEX idx_user_profiles_user_id ON user_profiles(user_id);
CREATE INDEX idx_meal_plans_user_id ON meal_plans(user_id);
CREATE INDEX idx_meal_plans_date ON meal_plans(date);
CREATE INDEX idx_nutrition_tracking_user_id ON nutrition_tracking(user_id);
CREATE INDEX idx_nutrition_tracking_date ON nutrition_tracking(date);
CREATE INDEX idx_user_feedback_user_id ON user_feedback(user_id);
CREATE INDEX idx_training_log_user_id ON training_log(user_id);
CREATE INDEX idx_training_log_date ON training_log(date);

-- Enable Row Level Security
ALTER TABLE user_profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE meal_plans ENABLE ROW LEVEL SECURITY;
ALTER TABLE nutrition_tracking ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_feedback ENABLE ROW LEVEL SECURITY;
ALTER TABLE training_log ENABLE ROW LEVEL SECURITY;

-- RLS Policies
CREATE POLICY "Users can view own profile" ON user_profiles
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "Users can insert own profile" ON user_profiles
    FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update own profile" ON user_profiles
    FOR UPDATE USING (auth.uid() = user_id);

CREATE POLICY "Users can view own meal plans" ON meal_plans
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "Users can insert own meal plans" ON meal_plans
    FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update own meal plans" ON meal_plans
    FOR UPDATE USING (auth.uid() = user_id);

CREATE POLICY "Users can view own tracking" ON nutrition_tracking
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "Users can insert own tracking" ON nutrition_tracking
    FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update own tracking" ON nutrition_tracking
    FOR UPDATE USING (auth.uid() = user_id);

CREATE POLICY "Users can view own feedback" ON user_feedback
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "Users can insert own feedback" ON user_feedback
    FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can view own training log" ON training_log
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "Users can insert own training log" ON training_log
    FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update own training log" ON training_log
    FOR UPDATE USING (auth.uid() = user_id);

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Triggers for updated_at
CREATE TRIGGER update_user_profiles_updated_at BEFORE UPDATE ON user_profiles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_meal_plans_updated_at BEFORE UPDATE ON meal_plans
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();