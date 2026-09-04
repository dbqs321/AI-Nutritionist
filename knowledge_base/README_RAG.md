# RAG-Based Evidence Grounding Documentation

## Overview

The RAG (Retrieval-Augmented Generation) system adds evidence-based grounding to the meal planning AI. It retrieves relevant passages from authoritative sports nutrition guidelines before generating meal plans, making recommendations more accurate, traceable, and updatable.

## System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Knowledge Base                             │
│  ├─ Sources (PDFs, text files)                              │
│  ├─ Build Script (chunking, embedding)                      │
│  └─ ChromaDB (vector storage)                                │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                    Retrieval System                          │
│  ├─ Query processing and embedding                          │
│  ├─ Similarity search in ChromaDB                           │
│  ├─ Nutrition target calculation                            │
│  └─ Context building                                         │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                    User Data Storage                          │
│  ├─ User profiles                                           │
│  ├─ Meal history and feedback                                │
│  └─ Preferences and constraints                             │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                    RAG Meal Planner                          │
│  ├─ Evidence-based system prompt                            │
│  ├─ Context assembly (evidence + user data)                 │
│  ├─ AI generation with citations                            │
│  └─ Response parsing and structuring                         │
└─────────────────────────────────────────────────────────────┘
```

## Components

### 1. Knowledge Base (`knowledge_base/`)

#### Directory Structure
```
knowledge_base/
├── sources/              # Original PDF/text documents
├── embeddings/          # Cached embeddings (optional)
├── chroma_db/          # ChromaDB vector database
├── config.py           # Configuration and approved sources
├── build_knowledge_base.py  # Document processing script
├── retrieval.py        # Retrieval system
├── user_data.py        # User data management
├── rag_meal_planner.py # Main RAG meal planner
├── test_rag_system.py  # Test suite
├── requirements.txt    # Python dependencies
└── README_RAG.md       # This documentation
```

#### Approved Sources

The system uses only authoritative sources:

1. **ACSM/AND/DC Joint Position Stand (2016)** - Highest authority
2. **ISSN Position Stands** - High authority (Protein, Nutrient Timing)
3. **AIS Supplement Framework** - High authority
4. **IOC Consensus Statements** - High authority
5. **USA Swimming Resources** - Sport-specific guidance

### 2. Build System (`build_knowledge_base.py`)

#### Features
- Chunks documents into 300-500 word passages with overlap
- Creates embeddings using OpenAI's text-embedding-3-small model
- Stores in ChromaDB with metadata (source, section, page, URL)
- Handles both text files and PDFs (PDF parsing requires additional setup)

#### Usage
```bash
cd knowledge_base
python build_knowledge_base.py
```

#### Adding New Sources

1. Place documents in `knowledge_base/sources/`
2. Add source metadata to `config.py` in `APPROVED_SOURCES`:
```python
"NEW_SOURCE_ID": {
    "title": "Source Title",
    "publication_date": "2024",
    "url": "https://source-url.com",
    "type": "position_stand",
    "authority": "high"
}
```
3. Rebuild knowledge base: `python build_knowledge_base.py`

### 3. Retrieval System (`retrieval.py`)

#### Key Functions

**`retrieve_nutrition_guidelines(query, top_k=5)`**
- Embeds query and searches ChromaDB
- Returns relevant passages with metadata
- Filters by similarity threshold (0.7)

**`calculate_nutrition_targets(user_profile, training_load)`**
- Uses evidence-based formulas from ACSM and ISSN
- Calculates calories, protein, carbs, fats
- Returns with source citations

**`build_context_for_generation(user_profile, training_load, meal_type)`**
- Assembles comprehensive context for AI generation
- Includes user profile, training context, calculated targets
- Retrieves relevant evidence passages
- Includes user history and preferences

#### Usage Example
```python
from retrieval import NutritionRetrievalSystem

retrieval = NutritionRetrievalSystem()

# Retrieve evidence
evidence = retrieval.retrieve_nutrition_guidelines("carbohydrate timing before practice")

# Calculate targets
targets = retrieval.calculate_nutrition_targets(profile, "heavy training")

# Build context
context = retrieval.build_context_for_generation(profile, "heavy training", "daily")
```

### 4. User Data Management (`user_data.py`)

#### Data Structures

**UserProfile**: Complete user profile with demographics, training, preferences
**MealFeedback**: Individual meal ratings and feedback
**WeeklyFeedback**: Summary feedback for meal plans

#### Key Functions

**`save_user_profile(profile)`** - Save user profile
**`load_user_profile(user_id)`** - Load user profile
**`save_meal_feedback(feedback)`** - Save meal feedback
**`get_meal_history(user_id, days)`** - Get recent meal history
**`get_meals_to_avoid(user_id, days)`** - Get recently disliked meals
**`get_preferred_meals(user_id, days)`** - Get highly-rated meals

#### Usage Example
```python
from user_data import UserDataManager, UserProfile, create_sample_user_profile

manager = UserDataManager()

# Create and save profile
profile = create_sample_user_profile("user123")
manager.save_user_profile(profile)

# Load profile
loaded = manager.load_user_profile("user123")

# Get preferences
preferences = manager.get_user_preferences("user123")
```

### 5. RAG Meal Planner (`rag_meal_planner.py`)

#### Main Functions

**`generate_meal_plan(user_id, training_load, meal_type, date)`**
- Main entry point for meal plan generation
- Integrates retrieval, user data, and AI generation
- Returns structured meal plan with evidence citations

**`adjust_for_training_load(meal_plan_id, new_training_load)`**
- Adjusts existing plan for different training load

**`handle_competition_day(user_id, event_time)`**
- Generates competition-specific meal plan

**`handle_travel_scenario(user_id, destination)`**
- Generates travel-friendly meal plan

#### Usage Example
```python
from rag_meal_planner import RAGMealPlanner

planner = RAGMealPlanner()

# Generate daily meal plan
meal_plan = planner.generate_meal_plan(
    user_id="user123",
    training_load="heavy training week - 8000 yards",
    meal_type="daily"
)

# Generate competition day plan
comp_plan = planner.handle_competition_day("user123", "2:00 PM")
```

## Evidence-Based Formulas

### Carbohydrate (ACSM Guidelines)
- **Light training**: 3-5 g/kg body weight
- **Moderate training**: 5-7 g/kg body weight
- **Heavy training**: 6-10 g/kg body weight
- **Very heavy training**: 8-12 g/kg body weight

### Protein (ISSN Guidelines)
- **Endurance athletes**: 1.4-1.6 g/kg body weight
- **Strength/power athletes**: 1.6-2.0 g/kg body weight
- **Heavy training**: 1.6-2.2 g/kg body weight
- **Adolescent growth**: 1.8-2.0 g/kg body weight

### Fat
- **General**: 0.8-1.2 g/kg body weight

## System Prompt

The RAG system uses a specialized system prompt that:

1. **Emphasizes evidence grounding** - Requires citations for all major recommendations
2. **Establishes decision hierarchy** - Safety > Evidence > Goals > Preferences > Variety
3. **Sets citation rules** - Must identify supporting sources from retrieved evidence
4. **Prioritizes approved sources** - ACSM, ISSN, IOC, AIS get highest priority
5. **Includes safety guidelines** - Medical disclaimer, supplement safety
6. **Defines output format** - Structured with evidence section

## Testing

### Run Test Suite
```bash
cd knowledge_base
python test_rag_system.py
```

### Test Coverage
1. **Knowledge base building** - Verifies document processing and embedding
2. **Retrieval accuracy** - Tests query relevance and passage quality
3. **Nutrition calculations** - Validates evidence-based formulas
4. **User data management** - Tests profile storage and retrieval
5. **Context building** - Verifies comprehensive context assembly
6. **Citation quality** - Ensures proper source attribution

## Configuration

### API Keys
Set in `config.py` or environment variables:
```bash
export OPENAI_API_KEY="your-openai-key"
export ANTHROPIC_API_KEY="your-anthropic-key"
```

### Settings
Modify in `config.py`:
- `EMBEDDING_MODEL`: OpenAI embedding model
- `CHUNK_SIZE`: Words per chunk (default: 400)
- `TOP_K_RETRIEVAL`: Number of passages to retrieve (default: 5)
- `SIMILARITY_THRESHOLD`: Minimum similarity score (default: 0.7)

## Integration with Android App

### Backend Service
The RAG system can be deployed as a backend service:

1. **Python Flask/FastAPI server** - Expose REST endpoints
2. **Docker containerization** - Package dependencies
3. **Cloud deployment** - AWS, Google Cloud, or Azure

### Android Integration
Update Android app to call RAG backend:

```kotlin
// In NutritionAgentImpl.kt
private suspend fun callRAGBackend(prompt: String): String {
    val service = RAGBackendService()
    return service.generateMealPlan(prompt)
}
```

### Offline Support
For offline functionality:
1. Pre-build knowledge base on device
2. Use local embeddings (sentence-transformers)
3. Cache retrieval results
4. Implement fallback to local generation

## Troubleshooting

### Common Issues

**Knowledge base empty**
- Ensure `build_knowledge_base.py` has been run
- Check that source documents exist in `sources/` directory
- Verify API keys are configured

**Poor retrieval results**
- Adjust similarity threshold in `config.py`
- Increase chunk size for more context
- Add more relevant source documents
- Improve query specificity

**API errors**
- Verify OpenAI API key is valid
- Check API quota and billing
- Ensure internet connectivity for API calls

**Memory issues**
- Reduce chunk size for processing
- Use batch processing for large documents
- Clear ChromaDB cache and rebuild

## Performance Optimization

### Embedding Caching
- Cache embeddings to avoid recomputation
- Store in `embeddings/` directory
- Implement hash-based cache invalidation

### Batch Processing
- Process multiple documents in batches
- Use async embedding calls
- Implement parallel processing where possible

### Query Optimization
- Use query expansion for better retrieval
- Implement query caching for repeated queries
- Use hybrid search (keyword + semantic)

## Future Enhancements

### Planned Features
1. **Multi-source citation** - Support multiple sources per recommendation
2. **Confidence scoring** - Assign confidence to each recommendation
3. **Evidence quality scoring** - Rate source quality and relevance
4. **User feedback integration** - Learn from user citation preferences
5. **Real-time updates** - Auto-update knowledge base from new sources
6. **Visual citation display** - Show sources in mobile app UI

### Advanced Features
1. **Multi-modal retrieval** - Include images, charts from sources
2. **Cross-language support** - Retrieve from non-English sources
3. **Temporal relevance** - Weight newer evidence higher
4. **Personalized ranking** - Adjust retrieval based on user history
5. **Explanation generation** - Explain why evidence was selected

## Security Considerations

### API Key Management
- Never commit API keys to version control
- Use environment variables or secure storage
- Rotate keys regularly
- Monitor API usage for anomalies

### Data Privacy
- Encrypt user data at rest
- Anonymize usage data for analysis
- Implement proper authentication
- Follow GDPR/CCPA compliance

### Content Safety
- Validate retrieved content for harmful recommendations
- Implement content filtering
- Provide medical disclaimers
- Enable user content reporting

## Contributing

### Adding New Sources
1. Verify source authority and credibility
2. Add to `APPROVED_SOURCES` in `config.py`
3. Place document in `sources/` directory
4. Rebuild knowledge base
5. Test retrieval for relevant queries

### Improving Formulas
1. Research current evidence-based guidelines
2. Update formulas in `config.py`
3. Add source citations
4. Test with various user profiles
5. Document changes in this README

## License and Attribution

### Source Attribution
All meal plan recommendations must cite:
- Source title and publication date
- Specific section or page number
- URL when available
- Authority level (position stand, consensus, etc.)

### Medical Disclaimer
The system must include:
- Clear medical disclaimer
- Recommendation to consult professionals
- Warning about individual variability
- Guidance for medical conditions

## Support and Maintenance

### Regular Updates
- Update knowledge base quarterly
- Review and add new research
- Refresh embeddings with new models
- Monitor retrieval quality metrics

### Monitoring
- Track API usage and costs
- Monitor retrieval success rates
- Analyze user feedback on citations
- Review system performance metrics

## Contact and Issues

For issues or questions:
1. Check this documentation first
2. Review test suite for examples
3. Check source document availability
4. Open issue with detailed description
5. Include error messages and steps to reproduce

---

**Last Updated**: 2026-08-28
**Version**: 1.0.0
**Status**: Production Ready