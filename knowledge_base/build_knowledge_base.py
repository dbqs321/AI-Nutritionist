"""
Build knowledge base for RAG-based meal planning system
Chunks documents, creates embeddings, and stores in ChromaDB
"""
import os
import json
import re
from pathlib import Path
from typing import List, Dict, Any
import chromadb
from chromadb.config import Settings
import openai
from openai import OpenAI
from config import (
    BASE_DIR, SOURCES_DIR, EMBEDDINGS_DIR, CHROMA_DB_DIR,
    EMBEDDING_MODEL, CHUNK_SIZE, CHUNK_OVERLAP, MAX_TOKENS,
    APPROVED_SOURCES, OPENAI_API_KEY
)

class KnowledgeBaseBuilder:
    def __init__(self):
        self.client = OpenAI(api_key=OPENAI_API_KEY)
        self.chroma_client = chromadb.PersistentClient(
            path=str(CHROMA_DB_DIR),
            settings=Settings(anonymized_telemetry=False)
        )
        self.collection = None
        
    def create_collection(self):
        """Create or get ChromaDB collection"""
        collection_name = "nutrition_guidelines"
        
        # Delete existing collection if it exists
        try:
            self.chroma_client.delete_collection(name=collection_name)
        except:
            pass
            
        self.collection = self.chroma_client.create_collection(
            name=collection_name,
            metadata={"hnsw:space": "cosine"}
        )
        print(f"Created collection: {collection_name}")
        
    def chunk_text(self, text: str, source_id: str, metadata: Dict[str, Any]) -> List[Dict[str, Any]]:
        """Chunk text into passages with overlap"""
        words = text.split()
        chunks = []
        
        for i in range(0, len(words), CHUNK_SIZE - CHUNK_OVERLAP):
            chunk_words = words[i:i + CHUNK_SIZE]
            chunk_text = " ".join(chunk_words)
            
            chunk_metadata = {
                "source_id": source_id,
                "chunk_index": len(chunks),
                "word_count": len(chunk_words),
                **metadata
            }
            
            chunks.append({
                "text": chunk_text,
                "metadata": chunk_metadata
            })
            
        return chunks
    
    def create_embedding(self, text: str) -> List[float]:
        """Create embedding using OpenAI API"""
        try:
            response = self.client.embeddings.create(
                model=EMBEDDING_MODEL,
                input=text[:MAX_TOKENS]  # Ensure within token limit
            )
            return response.data[0].embedding
        except Exception as e:
            print(f"Error creating embedding: {e}")
            return []
    
    def process_source_file(self, file_path: Path, source_id: str) -> List[Dict[str, Any]]:
        """Process a single source file"""
        print(f"Processing: {file_path.name}")
        
        # Read file content
        if file_path.suffix == '.pdf':
            # For PDF files, you would need to add PDF parsing
            # For now, we'll assume text files
            print(f"Warning: PDF parsing not implemented for {file_path}")
            return []
        
        with open(file_path, 'r', encoding='utf-8') as f:
            text = f.read()
        
        # Get source metadata
        source_metadata = APPROVED_SOURCES.get(source_id, {})
        
        # Chunk the text
        chunks = self.chunk_text(text, source_id, source_metadata)
        
        print(f"Created {len(chunks)} chunks from {file_path.name}")
        return chunks
    
    def add_chunks_to_collection(self, chunks: List[Dict[str, Any]]):
        """Add chunks to ChromaDB collection"""
        if not chunks:
            return
            
        # Prepare data for batch insertion
        texts = [chunk["text"] for chunk in chunks]
        metadatas = [chunk["metadata"] for chunk in chunks]
        
        # Create embeddings
        print("Creating embeddings...")
        embeddings = []
        for i, text in enumerate(texts):
            if i % 10 == 0:
                print(f"Processing embedding {i+1}/{len(texts)}")
            
            embedding = self.create_embedding(text)
            if embedding:
                embeddings.append(embedding)
            else:
                print(f"Failed to create embedding for chunk {i}")
                embeddings.append([0.0] * 1536)  # Fallback
        
        # Generate IDs
        ids = [f"{chunk['metadata']['source_id']}_chunk_{chunk['metadata']['chunk_index']}" 
               for chunk in chunks]
        
        # Add to collection
        self.collection.add(
            documents=texts,
            embeddings=embeddings,
            metadatas=metadatas,
            ids=ids
        )
        
        print(f"Added {len(chunks)} chunks to collection")
    
    def build_knowledge_base(self):
        """Build the complete knowledge base"""
        print("Building knowledge base...")
        
        # Create collection
        self.create_collection()
        
        # Process all source files
        all_chunks = []
        
        # Check for source files
        if not SOURCES_DIR.exists():
            print(f"Warning: Sources directory {SOURCES_DIR} does not exist")
            print("Creating sample knowledge base...")
            self.create_sample_knowledge_base()
            return
        
        # Process each source file
        for source_id in APPROVED_SOURCES.keys():
            # Look for matching files
            for file_path in SOURCES_DIR.glob("*"):
                if source_id.lower() in file_path.name.lower():
                    chunks = self.process_source_file(file_path, source_id)
                    all_chunks.extend(chunks)
        
        if not all_chunks:
            print("No source files found. Creating sample knowledge base...")
            self.create_sample_knowledge_base()
            return
        
        # Add chunks to collection
        self.add_chunks_to_collection(all_chunks)
        
        # Save metadata
        self.save_collection_metadata()
        
        print(f"Knowledge base built successfully with {len(all_chunks)} chunks")
    
    def create_sample_knowledge_base(self):
        """Create a sample knowledge base with placeholder content"""
        print("Creating sample knowledge base with sports nutrition guidelines...")
        
        sample_content = {
            "ACSM_AND_DC_2016": """
            Carbohydrate Recommendations:
            For athletes engaging in moderate to high-intensity exercise, carbohydrate 
            intake should range from 6-10 g/kg body weight per day. For light training, 
            3-5 g/kg is sufficient. Endurance athletes may require 8-12 g/kg during heavy 
            training periods. Carbohydrate loading strategies may be beneficial for 
            endurance events lasting longer than 90 minutes.
            
            Protein Recommendations:
            Protein requirements for athletes range from 1.2-2.0 g/kg body weight per day, 
            depending on training intensity and type. Strength and power athletes may need 
            1.6-2.0 g/kg, while endurance athletes typically need 1.2-1.4 g/kg. Protein 
            intake should be distributed throughout the day (20-25g per meal) to optimize 
            muscle protein synthesis.
            
            Hydration Guidelines:
            Athletes should consume 400-600 mL of fluid 2-4 hours before exercise and 
            150-250 mL every 15-20 minutes during exercise. Post-exercise hydration 
            should replace 125-150% of fluid lost during exercise. Electrolyte replacement 
            is important for exercise lasting longer than 1 hour.
            """,
            
            "ISSN_PROTEIN_2017": """
            Protein and Exercise:
            Current evidence suggests that athletes should consume 1.4-2.0 g/kg of protein 
            per day. For strength and power athletes, the upper end of this range (1.6-2.0 g/kg) 
            is recommended. For endurance athletes, 1.2-1.4 g/kg is generally sufficient. 
            
            Protein Timing:
            While total daily protein intake is most important, consuming protein within 
            2 hours post-exercise may enhance muscle protein synthesis. The anabolic window 
            appears to be wider than previously thought, with benefits observed up to 24 hours 
            after exercise.
            
            Protein Quality:
            Complete proteins containing all essential amino acids are preferred. Animal sources 
            and soy are complete proteins, while most plant proteins are incomplete but can be 
            combined to form complete proteins.
            """,
            
            "ISSN_NUTRIENT_TIMING_2008": """
            Nutrient Timing:
            Consuming carbohydrates and protein before, during, and after exercise can 
            enhance performance and recovery. Pre-exercise carbohydrate intake (1-4 g/kg) 
            1-4 hours before exercise can maximize glycogen stores. During exercise, 
            30-60 g/hour of carbohydrate can maintain blood glucose and delay fatigue.
            
            Post-Exercise Nutrition:
            Consuming carbohydrates (1.0-1.2 g/kg) and protein (0.3-0.4 g/kg) within 2 hours 
            post-exercise can optimize glycogen resynthesis and muscle protein synthesis. 
            The ratio of 3:1 or 4:1 carbohydrates to protein is often recommended.
            
            """,
            
            "AIS_SUPPLEMENT_FRAMEWORK_2023": """
            Supplement Classification:
            The Australian Institute of Sport categorizes supplements into 4 groups:
            Group A: Supported by evidence (e.g., caffeine, creatine, beta-alanine)
            Group B: Under investigation (e.g., some herbal supplements)
            Group C: Little to no evidence (e.g., many muscle-building supplements)
            Group D: Banned or high risk (e.g., some prohormones)
            
            Supplement Safety:
            Athletes should only use supplements that are third-party tested to minimize 
            contamination risk. Natural supplements are not necessarily safe or effective. 
            Consult with a sports dietitian before starting any supplement regimen.
            """,
            
            "IOC_CONSENSUS_2010": """
            Sports Nutrition Consensus:
            Adequate energy intake is fundamental for performance and health. Low energy 
            availability can impair performance, recovery, and health. Periodization of 
            nutrition intake should match training demands.
            
            Micronutrients:
            Athletes at risk for deficiencies include those with energy restrictions, 
            poor dietary variety, or specific medical conditions. Routine supplementation 
            is not recommended unless a deficiency is diagnosed.
            """
        }
        
        # Process sample content
        all_chunks = []
        for source_id, content in sample_content.items():
            source_metadata = APPROVED_SOURCES.get(source_id, {})
            chunks = self.chunk_text(content, source_id, source_metadata)
            all_chunks.extend(chunks)
        
        # Add to collection
        self.add_chunks_to_collection(all_chunks)
        
        # Save metadata
        self.save_collection_metadata()
        
        print(f"Sample knowledge base created with {len(all_chunks)} chunks")
    
    def save_collection_metadata(self):
        """Save collection metadata for reference"""
        metadata = {
            "total_chunks": self.collection.count(),
            "embedding_model": EMBEDDING_MODEL,
            "chunk_size": CHUNK_SIZE,
            "chunk_overlap": CHUNK_OVERLAP,
            "approved_sources": list(APPROVED_SOURCES.keys())
        }
        
        metadata_path = BASE_DIR / "collection_metadata.json"
        with open(metadata_path, 'w') as f:
            json.dump(metadata, f, indent=2)
        
        print(f"Collection metadata saved to {metadata_path}")

def main():
    """Main function to build knowledge base"""
    builder = KnowledgeBaseBuilder()
    builder.build_knowledge_base()

if __name__ == "__main__":
    main()