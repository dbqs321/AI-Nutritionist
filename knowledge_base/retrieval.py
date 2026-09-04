"""
Retrieval system for RAG-based meal planning
Queries knowledge base and builds context for meal generation
"""
import json
from typing import List, Dict, Any, Optional
from pathlib import Path
import chromadb
from chromadb.config import Settings
import openai
from openai import OpenAI
from config import (
    BASE_DIR, CHROMA_DB_DIR, EMBEDDING_MODEL, 
    TOP_K_RETRIEVAL, SIMILARITY_THRESHOLD,
    APPROVED_SOURCES, OPENAI_API_KEY,
    ACSM_CARBOHYDRATE_RANGES, ISSN_PROTEIN_RANGES, FAT_RANGES,
    TRAINING_LOAD_CATEGORIES
)

class NutritionRetrievalSystem:
    def __init__(self):
        self.client = OpenAI(api_key=OPENAI_API_KEY)
        self.chroma_client = chromadb.PersistentClient(
            path=str(CHROMA_DB_DIR),
            settings=Settings(anonymized_telemetry=False)
        )
        self.collection = None
        self._load_collection()
        
    def _load_collection(self):
        """Load ChromaDB collection"""
        try:
            self.collection = self.chroma_client.get_collection(
                name="nutrition_guidelines"
            )
            print(f"Loaded collection with {self.collection.count()} chunks")
        except Exception as e:
            print(f"Error loading collection: {e}")
            print("Please run build_knowledge_base.py first")
    
    def create_embedding(self, text: str) -> List[float]:
        """Create embedding for query text"""
        try:
            response = self.client.embeddings.create(
                model=EMBEDDING_MODEL,
                input=text
            )
            return response.data[0].embedding
        except Exception as e:
            print(f"Error creating embedding: {e}")
            return []
    
    def retrieve_nutrition_guidelines(self, query: str, top_k: int = TOP_K_RETRIEVAL) -> List[Dict[str, Any]]:
        """
        Retrieve relevant nutrition guideline passages
        
        Args:
            query: The search query
            top_k: Number of top results to return
            
        Returns:
            List of relevant passages with metadata
        """
        if not self.collection:
            print("Collection not loaded")
            return []
        
        # Create query embedding
        query_embedding = self.create_embedding(query)
        if not query_embedding:
            return []
        
        # Query collection
        results = self.collection.query(
            query_embeddings=[query_embedding],
            n_results=top_k
        )
        
        # Format results
        retrieved_passages = []
        if results['documents'] and results['documents'][0]:
            for i, doc in enumerate(results['documents'][0]):
                metadata = results['metadatas'][0][i] if results['metadatas'] and results['metadatas'][0] else {}
                distance = results['distances'][0][i] if results['distances'] and results['distances'][0] else 0.0
                
                # Convert distance to similarity score (cosine distance -> similarity)
                similarity = 1 - distance
                
                if similarity >= SIMILARITY_THRESHOLD:
                    source_info = APPROVED_SOURCES.get(metadata.get('source_id', ''), {})
                    
                    retrieved_passages.append({
                        'text': doc,
                        'metadata': metadata,
                        'source_title': source_info.get('title', 'Unknown Source'),
                        'publication_date': source_info.get('publication_date', 'Unknown'),
                        'url': source_info.get('url', ''),
                        'similarity_score': similarity,
                        'source_id': metadata.get('source_id', '')
                    })
        
        return retrieved_passages
    
    def retrieve_user_context(self, user_id: str) -> Dict[str, Any]:
        """
        Retrieve user-specific data from storage
        
        Args:
            user_id: User identifier
            
        Returns:
            User profile and preferences
        """
        # In a real implementation, this would query a database
        # For now, we'll return a placeholder
        user_data_path = BASE_DIR / f"user_data_{user_id}.json"
        
        if user_data_path.exists():
            with open(user_data_path, 'r') as f:
                return json.load(f)
        
        return {
            "user_id": user_id,
            "profile": {},
            "preferences": {},
            "meal_history": [],
            "feedback": []
        }
    
    def retrieve_meal_history(self, user_id: str, days: int = 7) -> List[Dict[str, Any]]:
        """
        Retrieve user's recent meal history
        
        Args:
            user_id: User identifier
            days: Number of days of history to retrieve
            
        Returns:
            List of recent meals with ratings and feedback
        """
        user_context = self.retrieve_user_context(user_id)
        meal_history = user_context.get('meal_history', [])
        
        # Filter by days if timestamps are available
        # For now, return all history
        return meal_history
    
    def calculate_nutrition_targets(self, user_profile: Dict[str, Any], training_load: str) -> Dict[str, Any]:
        """
        Calculate daily nutrition targets based on evidence-based formulas
        
        Args:
            user_profile: User profile data
            training_load: Training load category
            
        Returns:
            Calculated targets with source citations
        """
        weight_kg = user_profile.get('weight', 70)  # default 70kg
        age = user_profile.get('age', 18)
        sex = user_profile.get('sex', 'MALE')
        goal = user_profile.get('body_composition_goal', 'OPTIMIZE_PERFORMANCE')
        
        # Determine training category
        training_category = self._classify_training_load(training_load)
        
        # Calculate carbohydrate targets
        carb_range = ACSM_CARBOHYDRATE_RANGES.get(training_category, ACSM_CARBOHYDRATE_RANGES['moderate'])
        carb_g_per_kg = sum(carb_range['g_per_kg']) / 2  # Use midpoint
        total_carbs = round(carb_g_per_kg * weight_kg)
        carb_calories = total_carbs * carb_range['calories_per_gram']
        
        # Calculate protein targets
        protein_range = ISSN_PROTEIN_RANGES.get('heavy_training', ISSN_PROTEIN_RANGES['endurance'])
        # Adjust for adolescents
        if age < 19:
            protein_range = ISSN_PROTEIN_RANGES['adolescent_growth']
        
        protein_g_per_kg = sum(protein_range['g_per_kg']) / 2
        total_protein = round(protein_g_per_kg * weight_kg)
        protein_calories = total_protein * 4  # 4 calories per gram protein
        
        # Calculate fat targets
        fat_range = FAT_RANGES['general']
        fat_g_per_kg = sum(fat_range['g_per_kg']) / 2
        total_fat = round(fat_g_per_kg * weight_kg)
        fat_calories = total_fat * fat_range['calories_per_gram']
        
        # Calculate total calories
        total_calories = carb_calories + protein_calories + fat_calories
        
        # Adjust for goals
        if goal == 'LOSE_FAT':
            total_calories = round(total_calories * 0.85)  # 15% reduction
        elif goal == 'GAIN_MUSCLE':
            total_calories = round(total_calories * 1.10)  # 10% increase
        
        return {
            'total_calories': total_calories,
            'total_protein': total_protein,
            'total_carbs': total_carbs,
            'total_fats': total_fat,
            'carb_g_per_kg': round(carb_g_per_kg, 1),
            'protein_g_per_kg': round(protein_g_per_kg, 1),
            'fat_g_per_kg': round(fat_g_per_kg, 1),
            'training_category': training_category,
            'sources': {
                'carbohydrate': 'ACSM/AND/DC Joint Position Stand (2016)',
                'protein': 'ISSN Position Stand: Protein and Exercise (2017)',
                'fat': 'General sports nutrition guidelines'
            }
        }
    
    def _classify_training_load(self, training_load: str) -> str:
        """Classify training load into categories"""
        training_load = training_load.lower()
        
        if 'recovery' in training_load or 'rest' in training_load:
            return 'light'
        elif 'heavy' in training_load or 'intense' in training_load:
            return 'heavy'
        elif 'very' in training_load or 'extreme' in training_load:
            return 'very_heavy'
        else:
            return 'moderate'
    
    def build_context_for_generation(
        self, 
        user_profile: Dict[str, Any], 
        training_load: str,
        meal_type: str = "daily"
    ) -> str:
        """
        Build comprehensive context for meal plan generation
        
        Args:
            user_profile: User profile data
            training_load: Training load description
            meal_type: Type of meal plan (daily, competition, travel)
            
        Returns:
            Formatted context string with retrieved evidence
        """
        context_parts = []
        
        # 1. User Profile
        context_parts.append("USER PROFILE:")
        context_parts.append(json.dumps(user_profile, indent=2))
        context_parts.append("")
        
        # 2. Training Context
        context_parts.append("TRAINING CONTEXT:")
        context_parts.append(f"Training Load: {training_load}")
        context_parts.append(f"Meal Type: {meal_type}")
        context_parts.append("")
        
        # 3. Calculate Nutrition Targets
        nutrition_targets = self.calculate_nutrition_targets(user_profile, training_load)
        context_parts.append("CALCULATED NUTRITION TARGETS:")
        context_parts.append(json.dumps(nutrition_targets, indent=2))
        context_parts.append("")
        
        # 4. Retrieve Relevant Evidence
        # Build queries based on training load and meal type
        queries = self._build_retrieval_queries(training_load, meal_type)
        
        context_parts.append("RETRIEVED EVIDENCE:")
        all_evidence = []
        
        for query in queries:
            evidence = self.retrieve_nutrition_guidelines(query, top_k=3)
            all_evidence.extend(evidence)
        
        # Deduplicate and format evidence
        seen_sources = set()
        for evidence in all_evidence:
            source_id = evidence['source_id']
            if source_id not in seen_sources:
                seen_sources.add(source_id)
                context_parts.append(f"Source: {evidence['source_title']} ({evidence['publication_date']})")
                context_parts.append(f"Section: {evidence['metadata'].get('source_id', 'N/A')}")
                context_parts.append(f"URL: {evidence['url']}")
                context_parts.append(f"Relevance: {evidence['similarity_score']:.2f}")
                context_parts.append(f"Excerpt: {evidence['text'][:500]}...")
                context_parts.append("")
        
        # 5. User History and Preferences
        user_id = user_profile.get('id', 'default')
        meal_history = self.retrieve_meal_history(user_id, days=7)
        
        if meal_history:
            context_parts.append("USER MEAL HISTORY (Last 7 days):")
            context_parts.append(json.dumps(meal_history, indent=2))
            context_parts.append("")
        
        return "\n".join(context_parts)
    
    def _build_retrieval_queries(self, training_load: str, meal_type: str) -> List[str]:
        """Build relevant queries for evidence retrieval"""
        queries = []
        
        # Base nutrition queries
        queries.extend([
            "carbohydrate recommendations for athletes",
            "protein requirements for training",
            "nutrient timing around exercise",
            "hydration guidelines for athletes"
        ])
        
        # Training-specific queries
        if 'heavy' in training_load.lower() or 'intense' in training_load.lower():
            queries.extend([
                "nutrition for heavy training load",
                "carbohydrate loading strategies",
                "recovery nutrition after intense exercise"
            ])
        
        # Meal type specific queries
        if meal_type == "competition":
            queries.extend([
                "pre-competition meal timing",
                "competition day nutrition",
                "between-race fueling strategies"
            ])
        elif meal_type == "travel":
            queries.extend([
                "travel nutrition for athletes",
                "restaurant ordering for athletes",
                "portable nutrition options"
            ])
        
        return queries

def main():
    """Test the retrieval system"""
    print("Testing Nutrition Retrieval System...")
    
    retrieval = NutritionRetrievalSystem()
    
    # Test retrieval
    test_query = "carbohydrate timing before practice"
    print(f"\nTesting query: '{test_query}'")
    results = retrieval.retrieve_nutrition_guidelines(test_query)
    
    print(f"\nFound {len(results)} relevant passages:")
    for i, result in enumerate(results[:3], 1):
        print(f"\n{i}. {result['source_title']} ({result['publication_date']})")
        print(f"   Similarity: {result['similarity_score']:.2f}")
        print(f"   Excerpt: {result['text'][:200]}...")
    
    # Test context building
    print("\n\nTesting context building...")
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
    
    print(f"\nGenerated context (first 1000 chars):")
    print(context[:1000] + "...")

if __name__ == "__main__":
    main()