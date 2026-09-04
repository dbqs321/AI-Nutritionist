"""
Configuration for RAG-based meal planning system
"""
import os
from pathlib import Path

# Base paths
BASE_DIR = Path(__file__).parent
SOURCES_DIR = BASE_DIR / "sources"
EMBEDDINGS_DIR = BASE_DIR / "embeddings"
CHROMA_DB_DIR = BASE_DIR / "chroma_db"

# API Keys
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY", "your-openai-api-key")
ANTHROPIC_API_KEY = os.getenv("ANTHROPIC_API_KEY", "your-anthropic-api-key")

# Embedding settings
EMBEDDING_MODEL = "text-embedding-3-small"
CHUNK_SIZE = 400  # words
CHUNK_OVERLAP = 50  # words
MAX_TOKENS = 8191  # for text-embedding-3-small

# Retrieval settings
TOP_K_RETRIEVAL = 5
SIMILARITY_THRESHOLD = 0.7

# Approved sources metadata
APPROVED_SOURCES = {
    "ACSM_AND_DC_2016": {
        "title": "ACSM/AND/DC Joint Position Stand: Nutrition and Athletic Performance",
        "publication_date": "2016",
        "url": "https://www.acsm.org/docs/default-source/files-for-docs/resource-library/position-stand-nutrition-athletic-performance.pdf",
        "type": "position_stand",
        "authority": "highest"
    },
    "ISSN_PROTEIN_2017": {
        "title": "International Society of Sports Nutrition Position Stand: Protein and Exercise",
        "publication_date": "2017",
        "url": "https://jissn.biomedcentral.com/articles/10.1186/s1550-0273-16-43",
        "type": "position_stand",
        "authority": "high"
    },
    "ISSN_NUTRIENT_TIMING_2008": {
        "title": "International Society of Sports Nutrition Position Stand: Nutrient Timing",
        "publication_date": "2008",
        "url": "https://jissn.biomedcentral.com/articles/10.1186/1550-0273-5-17",
        "type": "position_stand",
        "authority": "high"
    },
    "AIS_SUPPLEMENT_FRAMEWORK_2023": {
        "title": "Australian Institute of Sport Supplement Framework",
        "publication_date": "2023",
        "url": "https://www.ais.gov.au/nutrition/supplements",
        "type": "framework",
        "authority": "high"
    },
    "IOC_CONSENSUS_2010": {
        "title": "IOC Consensus Statement on Sports Nutrition 2010",
        "publication_date": "2010",
        "url": "https://www.olympics.com/athletes/health/",
        "type": "consensus_statement",
        "authority": "high"
    }
}

# Evidence-based formulas for nutrition targets
ACSM_CARBOHYDRATE_RANGES = {
    "light": {"g_per_kg": (3, 5), "calories_per_gram": 4},
    "moderate": {"g_per_kg": (5, 7), "calories_per_gram": 4},
    "heavy": {"g_per_kg": (6, 10), "calories_per_gram": 4},
    "very_heavy": {"g_per_kg": (8, 12), "calories_per_gram": 4}
}

ISSN_PROTEIN_RANGES = {
    "endurance": {"g_per_kg": (1.4, 1.6)},
    "strength": {"g_per_kg": (1.6, 1.8)},
    "heavy_training": {"g_per_kg": (1.6, 2.2)},
    "adolescent_growth": {"g_per_kg": (1.8, 2.0)}
}

FAT_RANGES = {
    "general": {"g_per_kg": (0.8, 1.2), "calories_per_gram": 9}
}

# Training load classification
TRAINING_LOAD_CATEGORIES = {
    "recovery": {"yardage_threshold": 0, "intensity": "very_light"},
    "light": {"yardage_threshold": 3000, "intensity": "light"},
    "moderate": {"yardage_threshold": 5000, "intensity": "moderate"},
    "heavy": {"yardage_threshold": 7000, "intensity": "hard"},
    "very_heavy": {"yardage_threshold": 10000, "intensity": "very_hard"}
}