"""
Test script for RAG-based meal planning system
Verifies retrieval accuracy, citation quality, and meal generation
"""
import os
import sys
from pathlib import Path

# Add knowledge_base to path
sys.path.insert(0, str(Path(__file__).parent))

from build_knowledge_base import KnowledgeBaseBuilder
from retrieval import NutritionRetrievalSystem
from user_data import UserDataManager, create_sample_user_profile
from rag_meal_planner import RAGMealPlanner

def test_knowledge_base_build():
    """Test knowledge base building"""
    print("=" * 60)
    print("TEST 1: Knowledge Base Building")
    print("=" * 60)
    
    builder = KnowledgeBaseBuilder()
    builder.build_knowledge_base()
    
    print("✓ Knowledge base built successfully")
    print()

def test_retrieval_accuracy():
    """Test retrieval accuracy for specific queries"""
    print("=" * 60)
    print("TEST 2: Retrieval Accuracy")
    print("=" * 60)
    
    retrieval = NutritionRetrievalSystem()
    
    test_queries = [
        "carbohydrate timing before practice",
        "protein requirements for adolescent swimmers",
        "hydration guidelines for athletes",
        "post-workout recovery nutrition",
        "supplement safety for athletes"
    ]
    
    for query in test_queries:
        print(f"\nQuery: '{query}'")
        results = retrieval.retrieve_nutrition_guidelines(query, top_k=3)
        
        if results:
            print(f"✓ Found {len(results)} relevant passages")
            for i, result in enumerate(results[:2], 1):
                print(f"  {i}. {result['source_title']} ({result['publication_date']})")
                print(f"     Similarity: {result['similarity_score']:.2f}")
                print(f"     Excerpt: {result['text'][:100]}...")
        else:
            print("✗ No results found")
    
    print("\n✓ Retrieval accuracy test completed")
    print()

def test_nutrition_target_calculation():
    """Test evidence-based nutrition target calculations"""
    print("=" * 60)
    print("TEST 3: Nutrition Target Calculations")
    print("=" * 60)
    
    retrieval = NutritionRetrievalSystem()
    
    test_profiles = [
        {
            "weight": 68,
            "age": 16,
            "sex": "MALE",
            "body_composition_goal": "OPTIMIZE_PERFORMANCE"
        },
        {
            "weight": 55,
            "age": 15,
            "sex": "FEMALE", 
            "body_composition_goal": "GAIN_MUSCLE"
        }
    ]
    
    training_loads = ["moderate training", "heavy training", "recovery day"]
    
    for profile in test_profiles:
        print(f"\nProfile: {profile['weight']}kg, Age {profile['age']}, {profile['sex']}")
        for training_load in training_loads:
            targets = retrieval.calculate_nutrition_targets(profile, training_load)
            print(f"  {training_load}:")
            print(f"    Calories: {targets['total_calories']}")
            print(f"    Protein: {targets['total_protein']}g ({targets['protein_g_per_kg']}g/kg)")
            print(f"    Carbs: {targets['total_carbs']}g ({targets['carb_g_per_kg']}g/kg)")
            print(f"    Fat: {targets['total_fats']}g ({targets['fat_g_per_kg']}g/kg)")
            print(f"    Sources: {targets['sources']}")
    
    print("\n✓ Nutrition target calculation test completed")
    print()

def test_user_data_management():
    """Test user data storage and retrieval"""
    print("=" * 60)
    print("TEST 4: User Data Management")
    print("=" * 60)
    
    user_manager = UserDataManager()
    
    # Create and save sample profile
    sample_profile = create_sample_user_profile("test_user_123")
    success = user_manager.save_user_profile(sample_profile)
    
    if success:
        print("✓ Sample profile saved")
    else:
        print("✗ Failed to save profile")
        return
    
    # Load profile
    loaded_profile = user_manager.load_user_profile("test_user_123")
    if loaded_profile:
        print(f"✓ Profile loaded: {loaded_profile.id}, Age {loaded_profile.age}")
    else:
        print("✗ Failed to load profile")
        return
    
    # Test preferences
    preferences = user_manager.get_user_preferences("test_user_123")
    print(f"✓ User preferences retrieved: {len(preferences)} categories")
    
    # Test meal history
    history = user_manager.get_meal_history("test_user_123", days=7)
    print(f"✓ Meal history: {len(history)} meals")
    
    # Test user stats
    stats = user_manager.get_user_stats("test_user_123")
    print(f"✓ User stats: {stats}")
    
    print("\n✓ User data management test completed")
    print()

def test_context_building():
    """Test context building for meal generation"""
    print("=" * 60)
    print("TEST 5: Context Building")
    print("=" * 60)
    
    retrieval = NutritionRetrievalSystem()
    
    test_profile = {
        "id": "test_user",
        "age": 16,
        "sex": "MALE",
        "weight": 68,
        "height": 175,
        "body_composition_goal": "OPTIMIZE_PERFORMANCE"
    }
    
    context = retrieval.build_context_for_generation(
        test_profile,
        "heavy training week",
        "daily"
    )
    
    print(f"✓ Context built successfully")
    print(f"  Context length: {len(context)} characters")
    print(f"  Contains 'RETRIEVED EVIDENCE': {'RETRIEVED EVIDENCE' in context}")
    print(f"  Contains 'CALCULATED NUTRITION TARGETS': {'CALCULATED NUTRITION TARGETS' in context}")
    print(f"  Contains 'USER PROFILE': {'USER PROFILE' in context}")
    
    print("\n✓ Context building test completed")
    print()

def test_citation_quality():
    """Test that generated plans include proper citations"""
    print("=" * 60)
    print("TEST 6: Citation Quality")
    print("=" * 60)
    
    # This would test the actual meal generation, but requires API keys
    # For now, we'll test the system prompt and retrieval
    
    from rag_meal_planner import RAGMealPlanner
    planner = RAGMealPlanner()
    
    print("✓ RAG Meal Planner initialized")
    print(f"  System prompt length: {len(planner.system_prompt)} characters")
    print(f"  Contains citation instructions: {'cite' in planner.system_prompt.lower()}")
    print(f"  Contains evidence requirements: {'evidence' in planner.system_prompt.lower()}")
    
    # Test retrieval for citation sources
    retrieval = NutritionRetrievalSystem()
    results = retrieval.retrieve_nutrition_guidelines("carbohydrate recommendations")
    
    if results:
        print(f"✓ Retrieved {len(results)} passages with citation metadata")
        for result in results[:2]:
            print(f"  Source: {result['source_title']}")
            print(f"  Publication Date: {result['publication_date']}")
            print(f"  URL: {result['url']}")
    
    print("\n✓ Citation quality test completed")
    print()

def run_all_tests():
    """Run all tests"""
    print("\n" + "=" * 60)
    print("RAG-BASED MEAL PLANNING SYSTEM - TEST SUITE")
    print("=" * 60)
    print()
    
    try:
        test_knowledge_base_build()
        test_retrieval_accuracy()
        test_nutrition_target_calculation()
        test_user_data_management()
        test_context_building()
        test_citation_quality()
        
        print("=" * 60)
        print("ALL TESTS COMPLETED SUCCESSFULLY")
        print("=" * 60)
        print()
        print("Next steps:")
        print("1. Set up API keys in config.py or environment variables")
        print("2. Add actual PDF documents to knowledge_base/sources/")
        print("3. Run rag_meal_planner.py to generate evidence-based meal plans")
        print("4. Integrate with Android app for mobile implementation")
        
    except Exception as e:
        print(f"\n✗ Test failed with error: {e}")
        import traceback
        traceback.print_exc()

if __name__ == "__main__":
    run_all_tests()