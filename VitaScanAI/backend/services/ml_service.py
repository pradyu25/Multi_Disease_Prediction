import os
import numpy as np
from typing import Dict, Any
from core.config import get_settings

settings = get_settings()

class MLService:
    def __init__(self):
        self.model = None
        self.is_loading_failed = False
        
        # Expected features in the exact order the model expects
        self.feature_names = [
            "glucose", "cholesterol", "hemoglobin", "platelets", 
            "vitamin_d", "blood_pressure_systolic", "blood_pressure_diastolic"
        ]

    def _load_model(self):
        if self.model is not None or self.is_loading_failed:
            return
            
        if os.path.exists(settings.model_path):
            try:
                import joblib
                print(f"Loading ML model from {settings.model_path}...")
                self.model = joblib.load(settings.model_path)
            except Exception as e:
                print(f"Failed to load ML model: {e}")
                self.is_loading_failed = True
        else:
            print(f"ML model not found at {settings.model_path}. Using fallback/mock.")
            self.is_loading_failed = True

    def predict(self, parameters: Dict[str, float]) -> Dict[str, Any]:
        self._load_model()
        
        import pandas as pd
        
        """
        Assumes parameters dictionary contains all required fields. 
        Missing fields should be imputed.
        """
        # If no model, return mock predictions for dev purposes
        if self.model is None:
            return {
                "diabetes_risk": np.random.randint(10, 90),
                "anemia_risk": np.random.randint(10, 90),
                "heart_disease_risk": np.random.randint(10, 90),
                "confidence": {
                    "diabetes": 0.85,
                    "anemia": 0.78,
                    "heart_disease": 0.92
                }
            }
            
        # Prepare input data
        input_data = []
        for feat in self.feature_names:
            # Simple imputation (e.g., population mean) if missing
            val = parameters.get(feat)
            if val is None:
                # Mock mean defaults
                defaults = {
                    "glucose": 90.0, "cholesterol": 180.0, "hemoglobin": 14.0,
                    "platelets": 250.0, "vitamin_d": 30.0, 
                    "blood_pressure_systolic": 120.0, "blood_pressure_diastolic": 80.0
                }
                val = defaults.get(feat, 0.0)
            input_data.append(val)
            
        df = pd.DataFrame([input_data], columns=self.feature_names)
        
        # In this multi-output model setup, we expect the model to return a list of predictions
        # Or if it's a MultiOutputClassifier, it returns an array of shape (n_samples, n_outputs)
        try:
            proba = self.model.predict_proba(df)
            
            # Extract probability of class '1' (disease present) for each target
            # proba is typically a list of arrays (one for each target)
            diab_prob = proba[0][0][1] * 100
            anemia_prob = proba[1][0][1] * 100
            heart_prob = proba[2][0][1] * 100
            
            return {
                "diabetes_risk": int(diab_prob),
                "anemia_risk": int(anemia_prob),
                "heart_disease_risk": int(heart_prob),
                "confidence": {
                    "diabetes": float(max(proba[0][0])),
                    "anemia": float(max(proba[1][0])),
                    "heart_disease": float(max(proba[2][0]))
                }
            }
        except Exception as e:
            print(f"Prediction error: {e}")
            return {
                "diabetes_risk": 50,
                "anemia_risk": 50,
                "heart_disease_risk": 50,
                "confidence": {}
            }

ml_service = MLService()
