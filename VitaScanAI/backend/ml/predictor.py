import pandas as pd
from typing import Dict
from ml.model_loader import model_loader

class Predictor:
    def predict(self, input_features: Dict[str, float]) -> Dict[str, int]:
        """
        Runs input parameters through all loaded models 
        and returns a dictionary of risk percentages.
        """
        predictions = {}
        if not model_loader.models:
            return {"error": "No models loaded"}

        for disease, model in model_loader.models.items():
            try:
                # Retrieve the feature names expected by the pipeline
                if hasattr(model, 'feature_names_in_'):
                    expected_features = model.feature_names_in_
                else:
                    expected_features = list(input_features.keys())
                
                # Create a sample with features in correct order
                # Fill missing mapping with 0
                sample = {feat: [input_features.get(feat, 0.0)] for feat in expected_features}
                df_input = pd.DataFrame(sample)
                
                # predict_proba returns array of shape (1, n_classes). We take prob of class 1.
                proba = model.predict_proba(df_input)
                risk_score = float(proba[0][1]) * 100
                predictions[f"{disease}_risk"] = int(risk_score)
                
            except Exception as e:
                print(f"Error predicting risk for {disease}: {e}")
                predictions[f"{disease}_risk"] = 0
                
        return predictions

predictor = Predictor()
