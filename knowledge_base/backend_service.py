"""
Backend service for RAG-based meal planning
Provides REST API endpoints for Android app integration
"""
from flask import Flask, request, jsonify
from flask_cors import CORS
import json
from datetime import datetime
from rag_meal_planner import RAGMealPlanner
from user_data import UserDataManager, UserProfile
from config import OPENAI_API_KEY, ANTHROPIC_API_KEY

app = Flask(__name__)
CORS(app)  # Enable CORS for Android app

# Initialize components
meal_planner = RAGMealPlanner()
user_manager = UserDataManager()

@app.route('/health', methods=['GET'])
def health_check():
    """Health check endpoint"""
    return jsonify({
        'status': 'healthy',
        'timestamp': datetime.now().isoformat(),
        'components': {
            'meal_planner': 'ready',
            'user_manager': 'ready',
            'retrieval_system': 'ready'
        }
    })

@app.route('/api/meal-plan/generate', methods=['POST'])
def generate_meal_plan():
    """
    Generate evidence-based meal plan
    
    Expected JSON body:
    {
        "user_id": "string",
        "training_load": "string",
        "meal_type": "daily|competition|travel",
        "date": "YYYY-MM-DD" (optional)
    }
    """
    try:
        data = request.get_json()
        
        user_id = data.get('user_id')
        training_load = data.get('training_load', 'moderate training')
        meal_type = data.get('meal_type', 'daily')
        date = data.get('date')
        
        if not user_id:
            return jsonify({'error': 'user_id is required'}), 400
        
        # Generate meal plan
        meal_plan = meal_planner.generate_meal_plan(
            user_id=user_id,
            training_load=training_load,
            meal_type=meal_type,
            date=date
        )
        
        return jsonify({
            'success': True,
            'meal_plan': meal_plan
        })
        
    except Exception as e:
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500

@app.route('/api/meal-plan/adjust', methods=['POST'])
def adjust_meal_plan():
    """
    Adjust existing meal plan for different training load
    
    Expected JSON body:
    {
        "meal_plan_id": "string",
        "new_training_load": "string"
    }
    """
    try:
        data = request.get_json()
        
        meal_plan_id = data.get('meal_plan_id')
        new_training_load = data.get('new_training_load')
        
        if not meal_plan_id or not new_training_load:
            return jsonify({'error': 'meal_plan_id and new_training_load are required'}), 400
        
        # Adjust meal plan
        adjusted_plan = meal_planner.adjust_for_training_load(
            meal_plan_id=meal_plan_id,
            new_training_load=new_training_load
        )
        
        return jsonify({
            'success': True,
            'meal_plan': adjusted_plan
        })
        
    except Exception as e:
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500

@app.route('/api/meal-plan/competition', methods=['POST'])
def competition_meal_plan():
    """
    Generate competition day meal plan
    
    Expected JSON body:
    {
        "user_id": "string",
        "event_time": "string"
    }
    """
    try:
        data = request.get_json()
        
        user_id = data.get('user_id')
        event_time = data.get('event_time')
        
        if not user_id or not event_time:
            return jsonify({'error': 'user_id and event_time are required'}), 400
        
        # Generate competition plan
        comp_plan = meal_planner.handle_competition_day(
            user_id=user_id,
            event_time=event_time
        )
        
        return jsonify({
            'success': True,
            'meal_plan': comp_plan
        })
        
    except Exception as e:
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500

@app.route('/api/meal-plan/travel', methods=['POST'])
def travel_meal_plan():
    """
    Generate travel-friendly meal plan
    
    Expected JSON body:
    {
        "user_id": "string",
        "destination": "string"
    }
    """
    try:
        data = request.get_json()
        
        user_id = data.get('user_id')
        destination = data.get('destination')
        
        if not user_id or not destination:
            return jsonify({'error': 'user_id and destination are required'}), 400
        
        # Generate travel plan
        travel_plan = meal_planner.handle_travel_scenario(
            user_id=user_id,
            destination=destination
        )
        
        return jsonify({
            'success': True,
            'meal_plan': travel_plan
        })
        
    except Exception as e:
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500

@app.route('/api/user/profile', methods=['GET', 'POST', 'PUT'])
def user_profile():
    """Handle user profile operations"""
    try:
        if request.method == 'GET':
            user_id = request.args.get('user_id')
            if not user_id:
                return jsonify({'error': 'user_id is required'}), 400
            
            profile = user_manager.load_user_profile(user_id)
            if profile:
                return jsonify({
                    'success': True,
                    'profile': profile.__dict__
                })
            else:
                return jsonify({
                    'success': False,
                    'error': 'Profile not found'
                }), 404
        
        elif request.method == 'POST':
            data = request.get_json()
            profile = UserProfile(**data)
            success = user_manager.save_user_profile(profile)
            
            if success:
                return jsonify({'success': True})
            else:
                return jsonify({'success': False, 'error': 'Failed to save profile'}), 500
        
        elif request.method == 'PUT':
            data = request.get_json()
            profile = UserProfile(**data)
            success = user_manager.save_user_profile(profile)
            
            if success:
                return jsonify({'success': True})
            else:
                return jsonify({'success': False, 'error': 'Failed to update profile'}), 500
    
    except Exception as e:
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500

@app.route('/api/user/feedback', methods=['POST'])
def submit_feedback():
    """
    Submit meal feedback
    
    Expected JSON body:
    {
        "meal_plan_id": "string",
        "meal_name": "string",
        "rating": int (1-5),
        "adherence": bool,
        "notes": "string",
        "energy_level": int (1-10),
        "digestion_quality": int (1-10),
        "would_repeat": bool
    }
    """
    try:
        data = request.get_json()
        
        from user_data import MealFeedback
        
        feedback = MealFeedback(
            meal_plan_id=data.get('meal_plan_id'),
            meal_name=data.get('meal_name'),
            rating=data.get('rating'),
            adherence=data.get('adherence', True),
            notes=data.get('notes', ''),
            energy_level=data.get('energy_level', 5),
            digestion_quality=data.get('digestion_quality', 5),
            would_repeat=data.get('would_repeat', True),
            timestamp=datetime.now().isoformat()
        )
        
        success = user_manager.save_meal_feedback(feedback)
        
        if success:
            return jsonify({'success': True})
        else:
            return jsonify({'success': False, 'error': 'Failed to save feedback'}), 500
    
    except Exception as e:
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500

@app.route('/api/user/history', methods=['GET'])
def meal_history():
    """Get user's meal history"""
    try:
        user_id = request.args.get('user_id')
        days = request.args.get('days', 7, type=int)
        
        if not user_id:
            return jsonify({'error': 'user_id is required'}), 400
        
        history = user_manager.get_meal_history(user_id, days)
        
        return jsonify({
            'success': True,
            'history': history
        })
    
    except Exception as e:
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500

@app.route('/api/user/preferences', methods=['GET'])
def user_preferences():
    """Get user preferences and constraints"""
    try:
        user_id = request.args.get('user_id')
        
        if not user_id:
            return jsonify({'error': 'user_id is required'}), 400
        
        preferences = user_manager.get_user_preferences(user_id)
        
        return jsonify({
            'success': True,
            'preferences': preferences
        })
    
    except Exception as e:
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500

@app.route('/api/user/stats', methods=['GET'])
def user_stats():
    """Get user statistics and insights"""
    try:
        user_id = request.args.get('user_id')
        
        if not user_id:
            return jsonify({'error': 'user_id is required'}), 400
        
        stats = user_manager.get_user_stats(user_id)
        
        return jsonify({
            'success': True,
            'stats': stats
        })
    
    except Exception as e:
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500

@app.route('/api/knowledge-base/status', methods=['GET'])
def knowledge_base_status():
    """Get knowledge base status"""
    try:
        from retrieval import NutritionRetrievalSystem
        from pathlib import Path
        
        retrieval = NutritionRetrievalSystem()
        
        # Check if collection exists
        collection_exists = retrieval.collection is not None
        chunk_count = retrieval.collection.count() if collection_exists else 0
        
        # Check for source files
        sources_dir = Path(__file__).parent / "sources"
        source_files = list(sources_dir.glob("*")) if sources_dir.exists() else []
        
        return jsonify({
            'success': True,
            'status': {
                'collection_exists': collection_exists,
                'total_chunks': chunk_count,
                'source_files_count': len(source_files),
                'embedding_model': 'text-embedding-3-small'
            }
        })
    
    except Exception as e:
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500

if __name__ == '__main__':
    print("Starting RAG Meal Planning Backend Service...")
    print("API Documentation:")
    print("  POST /api/meal-plan/generate - Generate meal plan")
    print("  POST /api/meal-plan/adjust - Adjust meal plan")
    print("  POST /api/meal-plan/competition - Competition day plan")
    print("  POST /api/meal-plan/travel - Travel meal plan")
    print("  GET/POST/PUT /api/user/profile - User profile operations")
    print("  POST /api/user/feedback - Submit meal feedback")
    print("  GET /api/user/history - Get meal history")
    print("  GET /api/user/preferences - Get user preferences")
    print("  GET /api/user/stats - Get user statistics")
    print("  GET /api/knowledge-base/status - Knowledge base status")
    print("  GET /health - Health check")
    
    app.run(host='0.0.0.0', port=5000, debug=True)