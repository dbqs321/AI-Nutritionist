"""
RAG-based Meal Planner
Evidence-based meal planning using retrieval-augmented generation
"""
import json
from typing import Dict, Any, Optional
from datetime import datetime
from config import OPENAI_API_KEY, ANTHROPIC_API_KEY
from retrieval import NutritionRetrievalSystem
from user_data import UserDataManager, UserProfile, create_sample_user_profile
from openai import OpenAI
import anthropic

class RAGMealPlanner:
    """Evidence-based meal planner using RAG"""
    
    def __init__(self):
        self.retrieval_system = NutritionRetrievalSystem()
        self.user_manager = UserDataManager()
        self.openai_client = OpenAI(api_key=OPENAI_API_KEY)
        self.anthropic_client = anthropic.Anthropic(api_key=ANTHROPIC_API_KEY)
        
        self.system_prompt = self._get_rag_system_prompt()
    
    def _get_rag_system_prompt(self) -> str:
        """Get the RAG-aware system prompt"""
        return """You are FuelPlan, an evidence-based sports-nutrition meal-planning assistant for a competitive adolescent athlete.

You will receive:
1. USER PROFILE: goals, body metrics, dietary restrictions, schedule, cooking resources, budget, and food preferences.
2. TRAINING CONTEXT: today's and upcoming training duration, intensity, workout type, competition schedule, recovery status, and travel constraints.
3. PERSONAL HISTORY: meal ratings, adherence, appetite, energy, digestion, and performance feedback.
4. RETRIEVED EVIDENCE: excerpts from approved sports-nutrition guidelines and sources, each labeled with source title, publication date, section, and URL.
5. CALCULATED TARGETS: daily calories, protein (g), carbohydrates (g), fats (g), and fluid targets computed from evidence-based formulas.

Decision hierarchy:
1. Safety and medical/allergy restrictions.
2. Retrieved evidence from approved sources.
3. User goals and current training demands.
4. User preferences, schedule, budget, cooking ability, and food availability.
5. Variety, taste, and practical adherence.

Rules:
- Ground nutrition recommendations in the retrieved evidence. Do not invent guideline numbers, medical claims, supplement benefits, or citations.
- If the retrieved evidence does not support a specific claim, say that the evidence provided is insufficient rather than guessing.
- Treat guidelines as starting ranges, not exact prescriptions. Explain the adjustment made for the user's training load and feedback.
- For each macro target or timing recommendation, identify the supporting source and section from RETRIEVED EVIDENCE.
- Use only sources marked APPROVED. Give highest priority to ACSM/AND/DC, IOC, ISSN position stands, AIS resources, and guidance from credentialed sports dietitians.
- Never recommend extreme caloric restriction, rapid weight loss, or unsafe supplement use. For medical conditions, suspected low energy availability, eating-disorder concerns, significant unexplained weight change, or persistent gastrointestinal symptoms, advise consultation with a physician and sports registered dietitian.
- For supplements, only discuss products supported by retrieved approved sources. Flag anti-doping and contamination risks, and advise third-party tested products where relevant.
- Do not moralize food. Prioritize enough energy, performance, recovery, growth, enjoyment, affordability, and consistency.

When creating a meal plan:
- State the training-day category and explain why it changes carbohydrate, protein, meal timing, and hydration priorities.
- Make meals realistic for the user's exact schedule.
- Offer appealing variety and avoid recently disliked or overused meals.
- Include substitutions for unavailable ingredients and a lower-effort option for busy school nights.
- Verify that the meal-level macros add up approximately to the daily target.
- Clearly label calculated estimates versus evidence-based guideline ranges.

Output format:
1. Today's fueling priority.
2. Daily targets: calories, protein, carbohydrates, fat, fluids; include both the target and the source-supported rationale.
3. Meal schedule: time, meal, ingredients/portions, estimated macros, prep time, and purpose relative to training.
4. Adjustments based on user preferences and prior feedback.
5. Grocery/prep list if requested.
6. Evidence used: source title and section for every major recommendation.
7. One concise question that would most improve the next plan."""
    
    def generate_meal_plan(
        self, 
        user_id: str, 
        training_load: str = "moderate training",
        meal_type: str = "daily",
        date: Optional[str] = None
    ) -> Dict[str, Any]:
        """
        Generate evidence-based meal plan using RAG
        
        Args:
            user_id: User identifier
            training_load: Description of training load
            meal_type: Type of meal plan (daily, competition, travel)
            date: Date for the meal plan (defaults to today)
            
        Returns:
            Generated meal plan with evidence citations
        """
        if date is None:
            date = datetime.now().strftime("%Y-%m-%d")
        
        # Load user profile
        user_profile = self.user_manager.load_user_profile(user_id)
        if not user_profile:
            print(f"User profile not found for {user_id}. Creating sample profile.")
            user_profile = create_sample_user_profile(user_id)
            self.user_manager.save_user_profile(user_profile)
        
        # Convert profile to dict for processing
        profile_dict = {
            'id': user_profile.id,
            'age': user_profile.age,
            'sex': user_profile.sex,
            'weight': user_profile.weight,
            'height': user_profile.height,
            'body_composition_goal': user_profile.body_composition_goal,
            'sport_level': user_profile.sport_level,
            'weekly_yardage': user_profile.weekly_yardage,
            'practice_times': user_profile.practice_times,
            'school_hours': user_profile.school_hours,
            'commute_time': user_profile.commute_time,
            'homework_load': user_profile.homework_load,
            'bedtime': user_profile.bedtime,
            'wake_time': user_profile.wake_time,
            'sleep_quality': user_profile.sleep_quality,
            'cooking_skill': user_profile.cooking_skill,
            'typical_cooking_time': user_profile.typical_cooking_time,
            'loved_foods': user_profile.loved_foods,
            'disliked_foods': user_profile.disliked_foods,
            'cultural_practices': user_profile.cultural_practices,
            'religious_restrictions': user_profile.religious_restrictions,
            'available_equipment': user_profile.available_equipment,
            'supplementation': user_profile.supplementation,
            'allergies': user_profile.allergies,
            'budget_constraints': user_profile.budget_constraints
        }
        
        # Get user preferences and constraints
        meals_to_avoid = self.user_manager.get_meals_to_avoid(user_id, days=7)
        preferred_meals = self.user_manager.get_preferred_meals(user_id, days=30)
        user_stats = self.user_manager.get_user_stats(user_id)
        
        # Build RAG context
        rag_context = self.retrieval_system.build_context_for_generation(
            profile_dict, 
            training_load,
            meal_type
        )
        
        # Add user-specific context
        user_context = f"""
USER PREFERENCES AND CONSTRAINTS:
- Meals to avoid (recently disliked): {', '.join(meals_to_avoid) if meals_to_avoid else 'None'}
- Preferred meals (highly rated): {', '.join(preferred_meals[:5]) if preferred_meals else 'None'}
- User stats: {json.dumps(user_stats, indent=2)}
- Dietary restrictions: {', '.join(user_profile.religious_restrictions + user_profile.allergies) if (user_profile.religious_restrictions + user_profile.allergies) else 'None'}
"""
        
        # Build full prompt
        full_prompt = f"""{rag_context}

{user_context}

DATE: {date}
MEAL TYPE: {meal_type}

Please generate a comprehensive meal plan following the system instructions. Pay special attention to:
1. Citing specific sources from the RETRIEVED EVIDENCE for all major recommendations
2. Respecting the user's preferences and avoiding recently disliked meals
3. Making meals realistic given the user's cooking skill and time constraints
4. Ensuring the meal plan aligns with the CALCULATED NUTRITION TARGETS
"""
        
        # Generate meal plan using OpenAI
        meal_plan_text = self._generate_with_openai(full_prompt)
        
        # Parse and structure the response
        meal_plan = self._parse_meal_plan_response(meal_plan_text, date, user_id)
        
        return meal_plan
    
    def _generate_with_openai(self, prompt: str) -> str:
        """Generate meal plan using OpenAI"""
        try:
            response = self.openai_client.chat.completions.create(
                model="gpt-4",
                messages=[
                    {"role": "system", "content": self.system_prompt},
                    {"role": "user", "content": prompt}
                ],
                temperature=0.7,
                max_tokens=3000
            )
            return response.choices[0].message.content
        except Exception as e:
            print(f"Error generating with OpenAI: {e}")
            return self._generate_with_anthropic(prompt)
    
    def _generate_with_anthropic(self, prompt: str) -> str:
        """Generate meal plan using Anthropic (fallback)"""
        try:
            response = self.anthropic_client.messages.create(
                model="claude-3-sonnet-20240229",
                max_tokens=3000,
                system=self.system_prompt,
                messages=[
                    {"role": "user", "content": prompt}
                ]
            )
            return response.content[0].text
        except Exception as e:
            print(f"Error generating with Anthropic: {e}")
            return "Error generating meal plan. Please try again."
    
    def _parse_meal_plan_response(self, response_text: str, date: str, user_id: str) -> Dict[str, Any]:
        """Parse the AI response into structured meal plan"""
        # This is a simplified parser - in production, you'd want more sophisticated parsing
        # potentially using the AI to format the response as JSON
        
        return {
            'id': f"meal_plan_{user_id}_{date}",
            'user_id': user_id,
            'date': date,
            'generated_text': response_text,
            'created_at': datetime.now().isoformat(),
            'system_used': 'RAG-based evidence generation',
            'contains_citations': 'ACSM' in response_text or 'ISSN' in response_text or 'IOC' in response_text
        }
    
    def adjust_for_training_load(
        self, 
        meal_plan_id: str, 
        new_training_load: str
    ) -> Dict[str, Any]:
        """Adjust existing meal plan for different training load"""
        # Extract user_id from meal_plan_id
        user_id = meal_plan_id.split('_')[2] if '_' in meal_plan_id else "default"
        
        return self.generate_meal_plan(
            user_id=user_id,
            training_load=new_training_load,
            meal_type="daily"
        )
    
    def handle_competition_day(
        self, 
        user_id: str, 
        event_time: str
    ) -> Dict[str, Any]:
        """Generate competition day meal plan"""
        return self.generate_meal_plan(
            user_id=user_id,
            training_load=f"competition day with event at {event_time}",
            meal_type="competition"
        )
    
    def handle_travel_scenario(
        self, 
        user_id: str, 
        destination: str
    ) -> Dict[str, Any]:
        """Generate travel-friendly meal plan"""
        return self.generate_meal_plan(
            user_id=user_id,
            training_load=f"travel to {destination}",
            meal_type="travel"
        )

def main():
    """Test the RAG meal planner"""
    print("Testing RAG-based Meal Planner...")
    
    planner = RAGMealPlanner()
    
    # Generate a meal plan
    print("\nGenerating meal plan for sample user...")
    meal_plan = planner.generate_meal_plan(
        user_id="sample_user",
        training_load="heavy training week - 8000 yards with interval sets",
        meal_type="daily"
    )
    
    print(f"\nMeal Plan Generated:")
    print(f"ID: {meal_plan['id']}")
    print(f"Date: {meal_plan['date']}")
    print(f"Contains Citations: {meal_plan['contains_citations']}")
    print(f"\nGenerated Plan (first 1500 characters):")
    print(meal_plan['generated_text'][:1500] + "...")

if __name__ == "__main__":
    main()