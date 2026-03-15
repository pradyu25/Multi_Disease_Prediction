import torch
from transformers import AutoModelForSeq2SeqLM, AutoTokenizer
from typing import Dict, Any
from core.config import get_settings

settings = get_settings()

class LLMService:
    def __init__(self):
        self.model_name = settings.hf_model_name
        self.tokenizer = None
        self.model = None
        self.is_loaded = False
        
        # Lazy loading to avoid blocking startup
    
    def _load_model(self):
        if not self.is_loaded:
            try:
                print(f"Loading {self.model_name}...")
                self.tokenizer = AutoTokenizer.from_pretrained(self.model_name)
                self.model = AutoModelForSeq2SeqLM.from_pretrained(self.model_name)
                # Move to GPU if available
                self.device = "cuda" if torch.cuda.is_available() else "cpu"
                self.model.to(self.device)
                self.is_loaded = True
                print("LLM loaded successfully.")
            except Exception as e:
                print(f"Error loading LLM: {e}")

    def generate_recommendations(self, parameters: Dict[str, float], risks: Dict[str, int]) -> Dict[str, str]:
        if not settings.use_llm:
            return self._fallback_recommendations(risks)
            
        self._load_model()
        
        if not self.is_loaded:
            # Fallback if model failed to load
            return self._fallback_recommendations(risks)
            
        with torch.no_grad():
            # Construct prompt for a more professional medical AI assistant
            context = (
                f"The patient has the following estimated disease risks based on lab values: "
                f"Diabetes: {risks.get('diabetes_risk') or 0}%, "
                f"Heart Disease: {risks.get('heart_disease_risk') or 0}%, "
                f"Anemia: {risks.get('anemia_risk') or 0}%. "
            )
                      
            param_str = ", ".join([f"{k}: {v}" for k, v in parameters.items()])
            context += f"Relevant lab parameters detected: {param_str}."
            
            # More structured prompts for FLAN-T5
            diet_prompt = f"Context: {context} Task: Provide one specific dietary recommendation to reduce these risks. Answer:"
            exercise_prompt = f"Context: {context} Task: Suggest one specific physical activity or exercise routine suitable for this profile. Answer:"
            doctor_prompt = f"Context: {context} Task: Which medical specialist should this patient consult first? (e.g. Cardiologist, Endocrinologist, GP). Answer:"
            lifestyle_prompt = f"Context: {context} Task: Suggest one important lifestyle change (like sleep, stress, or habits). Answer:"
            
            diet = self._generate_text(diet_prompt)
            exercise = self._generate_text(exercise_prompt)
            doctor = self._generate_text(doctor_prompt)
            lifestyle = self._generate_text(lifestyle_prompt)
            
            return {
                "diet": diet,
                "exercise": exercise,
                "doctor": doctor,
                "lifestyle": lifestyle
            }
        
    def _generate_text(self, prompt: str) -> str:
        try:
            inputs = self.tokenizer(prompt, return_tensors="pt", max_length=512, truncation=True).to(self.device)
            outputs = self.model.generate(
                **inputs, 
                max_length=64, 
                num_beams=4, 
                early_stopping=True,
                temperature=0.7
            )
            return self.tokenizer.decode(outputs[0], skip_special_tokens=True).strip()
        except Exception as e:
            print(f"Generation error: {e}")
            return "Recommendation unavailable"

    def _fallback_recommendations(self, risks: Dict[str, int]) -> Dict[str, str]:
        diab = risks.get("diabetes_risk", 0)
        heart = risks.get("heart_disease_risk", 0)
        
        diet = "Maintain a balanced diet rich in vegetables and lean proteins."
        if diab > 50:
            diet = "Reduce refined carbohydrates and added sugars. Focus on low glycemic index foods."
        elif heart > 50:
            diet = "Adopt a heart-healthy diet low in saturated fats and sodium. Follow the DASH diet."
            
        doctor = "General Practitioner"
        if diab > 70: doctor = "Endocrinologist"
        elif heart > 70: doctor = "Cardiologist"
        
        return {
            "diet": diet,
            "exercise": "Aim for 150 minutes of moderate aerobic activity per week, as tolerated.",
            "doctor": doctor,
            "lifestyle": "Ensure 7-8 hours of quality sleep and manage stress."
        }

llm_service = LLMService()
