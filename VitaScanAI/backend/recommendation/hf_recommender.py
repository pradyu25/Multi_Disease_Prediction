import torch
from transformers import AutoModelForSeq2SeqLM, AutoTokenizer
from typing import Dict

class HuggingFaceRecommender:
    def __init__(self, model_name="google/flan-t5-base"):
        self.model_name = model_name
        self.tokenizer = None
        self.model = None
        self.is_loaded = False

    def load_model(self):
        if not self.is_loaded:
            print(f"Loading HuggingFace model: {self.model_name}")
            try:
                self.tokenizer = AutoTokenizer.from_pretrained(self.model_name)
                self.model = AutoModelForSeq2SeqLM.from_pretrained(self.model_name)
                self.device = "cuda" if torch.cuda.is_available() else "cpu"
                self.model.to(self.device)
                self.is_loaded = True
                print("Hugging Face Recommendations Model loaded successfully")
            except Exception as e:
                print(f"Error loading Hugging Face model: {e}")

    def generate_recommendations(self, medical_values: dict, predictions: dict) -> Dict[str, str]:
        if not self.is_loaded:
            return {
                "diet": "Error: model not loaded",
                "exercise": "Error: model not loaded",
                "doctor": "Error: model not loaded",
                "explanation": "Error: model not loaded"
            }

        context = self._build_context(medical_values, predictions)
        
        # Sequentially prompt models for components
        diet_prompt = f"Patient profile: {context} Give a specific 1-sentence dietary restriction or suggestion."
        exercise_prompt = f"Patient profile: {context} Give a specific 1-sentence exercise suggestion."
        doctor_prompt = f"Patient profile: {context} Name the strict medical specialist this patient should consult. Just the title."
        explanation_prompt = f"Patient profile: {context} Provide a concise 2-sentence explanation of what these results mean overall."

        return {
            "diet": self._generate_inference(diet_prompt),
            "exercise": self._generate_inference(exercise_prompt),
            "doctor": self._generate_inference(doctor_prompt),
            "explanation": self._generate_inference(explanation_prompt)
        }

    def _build_context(self, medical_values: dict, predictions: dict) -> str:
        parts = []
        if medical_values:
            params = ", ".join([f"{k}:{v}" for k, v in medical_values.items()])
            parts.append(f"Labs: {params}.")
        if predictions:
            # Drop the _risk suffix for clarity
            preds = ", ".join([f"{k.replace('_risk', '')}:{v}%" for k, v in predictions.items()])
            parts.append(f"Risks: {preds}.")
        return " ".join(parts)

    def _generate_inference(self, prompt: str) -> str:
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
        except Exception:
            return "Recommendation unavailable"

hf_recommender = HuggingFaceRecommender()
