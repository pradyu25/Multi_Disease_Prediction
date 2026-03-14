import pandas as pd
import numpy as np
import os
from sklearn.multioutput import MultiOutputClassifier
from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score

from ensemble import create_ensemble, save_model

def generate_synthetic_data(n_samples=5000):
    """
    Generates synthetic medical data since no real dataset was provided.
    Models relationships logically:
    - High glucose -> Diabetes risk
    - Low hemoglobin/platelets -> Anemia risk
    - High blood pressure & cholesterol -> Heart disease risk
    """
    print(f"Generating {n_samples} synthetic medical records...")
    np.random.seed(42)
    
    # Features
    glucose = np.random.normal(100, 30, n_samples)
    cholesterol = np.random.normal(190, 40, n_samples)
    hemoglobin = np.random.normal(14.5, 2.0, n_samples)
    platelets = np.random.normal(250, 50, n_samples)
    vitamin_d = np.random.normal(35, 15, n_samples)
    bp_sys = np.random.normal(120, 20, n_samples)
    bp_dia = np.random.normal(80, 15, n_samples)
    
    # Outcomes (0 or 1)
    diabetes = (glucose > 140).astype(int)
    anemia = (hemoglobin < 12).astype(int)
    heart_disease = ((cholesterol > 240) | (bp_sys > 140)).astype(int)
    
    # Add some noise
    diabetes ^= np.random.binomial(1, 0.05, n_samples)
    anemia ^= np.random.binomial(1, 0.05, n_samples)
    heart_disease ^= np.random.binomial(1, 0.05, n_samples)
    
    df = pd.DataFrame({
        'glucose': glucose,
        'cholesterol': cholesterol,
        'hemoglobin': hemoglobin,
        'platelets': platelets,
        'vitamin_d': vitamin_d,
        'blood_pressure_systolic': bp_sys,
        'blood_pressure_diastolic': bp_dia,
        'target_diabetes': diabetes,
        'target_anemia': anemia,
        'target_heart': heart_disease
    })
    return df

def train():
    df = generate_synthetic_data()
    
    features = [
        "glucose", "cholesterol", "hemoglobin", "platelets", 
        "vitamin_d", "blood_pressure_systolic", "blood_pressure_diastolic"
    ]
    targets = ["target_diabetes", "target_anemia", "target_heart"]
    
    X = df[features]
    y = df[targets]
    
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)
    
    print("Initializing Ensemble models...")
    base_ensemble = create_ensemble()
    
    # Wrap in MultiOutputClassifier to handle 3 targets simultaneously
    model = MultiOutputClassifier(base_ensemble, n_jobs=-1)
    
    print("Training Multi-target Ensemble (RandomForest + XGBoost + LogisticRegression)...")
    model.fit(X_train, y_train)
    
    # Evaluate
    y_pred = model.predict(X_test)
    for i, target in enumerate(targets):
        acc = accuracy_score(y_test.iloc[:, i], y_pred[:, i])
        print(f"Accuracy for {target}: {acc:.4f}")
        
    save_model(model, "ml/models/ensemble.joblib")

if __name__ == "__main__":
    train()
