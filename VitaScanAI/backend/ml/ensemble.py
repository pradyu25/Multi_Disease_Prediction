from sklearn.ensemble import RandomForestClassifier, VotingClassifier
from sklearn.linear_model import LogisticRegression
import xgboost as xgb
import os
import joblib

def create_ensemble() -> VotingClassifier:
    """
    Creates a multiple multi-disease prediction ensemble that returns probabilities
    for multiple possible outputs (Diabetes, Anemia, Heart Disease).
    Because standard VotingClassifier doesn't handle multioutput natively easily 
    without MultiOutputClassifier wrapper, we set it up to be wrapped later.
    """
    rf = RandomForestClassifier(n_estimators=100, random_state=42)
    xgb_clf = xgb.XGBClassifier(n_estimators=100, learning_rate=0.1, random_state=42, use_label_encoder=False, eval_metric="logloss")
    lr = LogisticRegression(max_iter=1000, random_state=42)
    
    # Soft voting averages the probabilities of the constituent models
    ensemble = VotingClassifier(
        estimators=[
            ('rf', rf),
            ('xgb', xgb_clf),
            ('lr', lr)
        ],
        voting='soft'
    )
    return ensemble

def save_model(model, path="models/ensemble.joblib"):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    joblib.dump(model, path)
    print(f"Model saved to {path}")
