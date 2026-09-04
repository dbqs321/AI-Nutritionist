"""
User data storage and management system
Handles user profiles, preferences, meal history, and feedback
"""
import json
import os
from datetime import datetime, timedelta
from pathlib import Path
from typing import Dict, Any, List, Optional
from dataclasses import dataclass, asdict
from config import BASE_DIR

@dataclass
class UserProfile:
    """User profile data structure"""
    id: str
    age: int
    sex: str  # MALE, FEMALE, OTHER
    height: float  # cm
    weight: float  # kg
    body_composition_goal: str  # MAINTAIN_WEIGHT, GAIN_MUSCLE, LOSE_FAT, OPTIMIZE_PERFORMANCE
    sport_level: str
    weekly_yardage: int
    practice_times: str
    school_hours: str
    commute_time: int
    homework_load: str
    bedtime: str
    wake_time: str
    sleep_quality: str
    cooking_skill: str
    typical_cooking_time: int
    loved_foods: List[str]
    disliked_foods: List[str]
    cultural_practices: List[str]
    religious_restrictions: List[str]
    available_equipment: List[str]
    supplementation: List[str]
    allergies: List[str]
    budget_constraints: str
    created_at: str
    updated_at: str

@dataclass
class MealFeedback:
    """Meal feedback data structure"""
    meal_plan_id: str
    meal_name: str
    rating: int  # 1-5 scale
    adherence: bool
    notes: str
    energy_level: int  # 1-10
    digestion_quality: int  # 1-10
    would_repeat: bool
    timestamp: str

@dataclass
class WeeklyFeedback:
    """Weekly feedback summary"""
    meal_plan_id: str
    appealing_meals: List[str]
    unrealistic_meals: List[str]
    confidence_level: int  # 1-10
    anticipated_barriers: List[str]
    learnings_from_previous_week: str
    overall_adherence: int  # 1-10
    timestamp: str

class UserDataManager:
    """Manages user data storage and retrieval"""
    
    def __init__(self, data_dir: Optional[Path] = None):
        self.data_dir = data_dir or BASE_DIR / "user_data"
        self.data_dir.mkdir(exist_ok=True)
        
    def save_user_profile(self, profile: UserProfile) -> bool:
        """Save user profile to JSON file"""
        try:
            profile_path = self.data_dir / f"profile_{profile.id}.json"
            
            # Update timestamp
            profile.updated_at = datetime.now().isoformat()
            
            with open(profile_path, 'w') as f:
                json.dump(asdict(profile), f, indent=2)
            
            print(f"Profile saved for user: {profile.id}")
            return True
        except Exception as e:
            print(f"Error saving profile: {e}")
            return False
    
    def load_user_profile(self, user_id: str) -> Optional[UserProfile]:
        """Load user profile from JSON file"""
        try:
            profile_path = self.data_dir / f"profile_{user_id}.json"
            
            if not profile_path.exists():
                return None
            
            with open(profile_path, 'r') as f:
                data = json.load(f)
            
            return UserProfile(**data)
        except Exception as e:
            print(f"Error loading profile: {e}")
            return None
    
    def save_meal_feedback(self, feedback: MealFeedback) -> bool:
        """Save meal feedback"""
        try:
            feedback_path = self.data_dir / f"feedback_{feedback.meal_plan_id}.json"
            
            # Load existing feedback or create new
            existing_feedback = []
            if feedback_path.exists():
                with open(feedback_path, 'r') as f:
                    existing_feedback = json.load(f)
            
            # Add new feedback
            existing_feedback.append(asdict(feedback))
            
            with open(feedback_path, 'w') as f:
                json.dump(existing_feedback, f, indent=2)
            
            print(f"Meal feedback saved for plan: {feedback.meal_plan_id}")
            return True
        except Exception as e:
            print(f"Error saving meal feedback: {e}")
            return False
    
    def save_weekly_feedback(self, feedback: WeeklyFeedback) -> bool:
        """Save weekly feedback summary"""
        try:
            feedback_path = self.data_dir / f"weekly_feedback_{feedback.meal_plan_id}.json"
            
            with open(feedback_path, 'w') as f:
                json.dump(asdict(feedback), f, indent=2)
            
            print(f"Weekly feedback saved for plan: {feedback.meal_plan_id}")
            return True
        except Exception as e:
            print(f"Error saving weekly feedback: {e}")
            return False
    
    def get_meal_history(self, user_id: str, days: int = 7) -> List[Dict[str, Any]]:
        """Get user's meal history for specified number of days"""
        try:
            cutoff_date = datetime.now() - timedelta(days=days)
            meal_history = []
            
            # Get all feedback files for this user
            feedback_files = list(self.data_dir.glob(f"feedback_*_{user_id}.json"))
            
            for feedback_file in feedback_files:
                with open(feedback_file, 'r') as f:
                    feedback_list = json.load(f)
                
                for feedback in feedback_list:
                    try:
                        feedback_date = datetime.fromisoformat(feedback['timestamp'])
                        if feedback_date >= cutoff_date:
                            meal_history.append(feedback)
                    except:
                        continue
            
            # Sort by timestamp
            meal_history.sort(key=lambda x: x['timestamp'], reverse=True)
            
            return meal_history
        except Exception as e:
            print(f"Error getting meal history: {e}")
            return []
    
    def get_user_preferences(self, user_id: str) -> Dict[str, Any]:
        """Get user preferences and constraints"""
        profile = self.load_user_profile(user_id)
        if not profile:
            return {}
        
        return {
            'loved_foods': profile.loved_foods,
            'disliked_foods': profile.disliked_foods,
            'cultural_practices': profile.cultural_practices,
            'religious_restrictions': profile.religious_restrictions,
            'allergies': profile.allergies,
            'cooking_skill': profile.cooking_skill,
            'typical_cooking_time': profile.typical_cooking_time,
            'available_equipment': profile.available_equipment,
            'budget_constraints': profile.budget_constraints
        }
    
    def get_meals_to_avoid(self, user_id: str, days: int = 7) -> List[str]:
        """Get list of meals to avoid based on recent negative feedback"""
        meal_history = self.get_meal_history(user_id, days)
        meals_to_avoid = []
        
        for feedback in meal_history:
            if feedback.get('rating', 5) <= 2 or not feedback.get('would_repeat', True):
                meal_name = feedback.get('meal_name', '')
                if meal_name and meal_name not in meals_to_avoid:
                    meals_to_avoid.append(meal_name)
        
        return meals_to_avoid
    
    def get_preferred_meals(self, user_id: str, days: int = 30) -> List[str]:
        """Get list of preferred meals based on positive feedback"""
        meal_history = self.get_meal_history(user_id, days)
        preferred_meals = []
        
        for feedback in meal_history:
            if feedback.get('rating', 5) >= 4 and feedback.get('would_repeat', False):
                meal_name = feedback.get('meal_name', '')
                if meal_name and meal_name not in preferred_meals:
                    preferred_meals.append(meal_name)
        
        return preferred_meals
    
    def get_user_stats(self, user_id: str) -> Dict[str, Any]:
        """Get user statistics and insights"""
        meal_history = self.get_meal_history(user_id, days=30)
        
        if not meal_history:
            return {
                'total_meals_rated': 0,
                'average_rating': 0,
                'average_adherence': 0,
                'most_common_issues': []
            }
        
        total_meals = len(meal_history)
        average_rating = sum(f.get('rating', 5) for f in meal_history) / total_meals
        adherence_count = sum(1 for f in meal_history if f.get('adherence', False))
        average_adherence = (adherence_count / total_meals) * 100
        
        # Find common issues
        issues = {}
        for feedback in meal_history:
            notes = feedback.get('notes', '').lower()
            if 'time' in notes:
                issues['time'] = issues.get('time', 0) + 1
            if 'taste' in notes:
                issues['taste'] = issues.get('taste', 0) + 1
            if 'preparation' in notes:
                issues['preparation'] = issues.get('preparation', 0) + 1
        
        most_common_issues = sorted(issues.items(), key=lambda x: x[1], reverse=True)[:3]
        
        return {
            'total_meals_rated': total_meals,
            'average_rating': round(average_rating, 1),
            'average_adherence': round(average_adherence, 1),
            'most_common_issues': [issue[0] for issue in most_common_issues]
        }

def create_sample_user_profile(user_id: str = "sample_user") -> UserProfile:
    """Create a sample user profile for testing"""
    return UserProfile(
        id=user_id,
        age=16,
        sex="MALE",
        height=175.0,
        weight=68.0,
        body_composition_goal="OPTIMIZE_PERFORMANCE",
        sport_level="High School",
        weekly_yardage=15000,
        practice_times="4:00 PM - 6:00 PM",
        school_hours="8:00 AM - 3:00 PM",
        commute_time=15,
        homework_load="MODERATE",
        bedtime="10:30 PM",
        wake_time="6:00 AM",
        sleep_quality="GOOD",
        cooking_skill="INTERMEDIATE",
        typical_cooking_time=30,
        loved_foods=["pasta", "chicken", "smoothies", "oatmeal"],
        disliked_foods=["fish", "mushrooms", "eggplant"],
        cultural_practices=[],
        religious_restrictions=[],
        available_equipment=["microwave", "stove", "blender", "rice cooker"],
        supplementation=["multivitamin", "protein powder"],
        allergies=[],
        budget_constraints="moderate",
        created_at=datetime.now().isoformat(),
        updated_at=datetime.now().isoformat()
    )

def main():
    """Test the user data management system"""
    print("Testing User Data Management System...")
    
    manager = UserDataManager()
    
    # Create and save sample profile
    sample_profile = create_sample_user_profile()
    manager.save_user_profile(sample_profile)
    
    # Load profile
    loaded_profile = manager.load_user_profile("sample_user")
    print(f"\nLoaded profile for: {loaded_profile.id}")
    print(f"Age: {loaded_profile.age}, Weight: {loaded_profile.weight}kg")
    
    # Test meal feedback
    sample_feedback = MealFeedback(
        meal_plan_id="test_plan_001",
        meal_name="Grilled Chicken with Rice",
        rating=4,
        adherence=True,
        notes="Tasted great, easy to prepare",
        energy_level=8,
        digestion_quality=9,
        would_repeat=True,
        timestamp=datetime.now().isoformat()
    )
    
    manager.save_meal_feedback(sample_feedback)
    
    # Get meal history
    history = manager.get_meal_history("sample_user", days=7)
    print(f"\nMeal history (last 7 days): {len(history)} meals")
    
    # Get user preferences
    preferences = manager.get_user_preferences("sample_user")
    print(f"\nUser preferences: {preferences}")
    
    # Get user stats
    stats = manager.get_user_stats("sample_user")
    print(f"\nUser stats: {stats}")

if __name__ == "__main__":
    main()