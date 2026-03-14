import os
import joblib
from sklearn.pipeline import Pipeline
from sklearn.ensemble import RandomForestClassifier, VotingClassifier
from sklearn.linear_model import LogisticRegression
from xgboost import XGBClassifier
from sklearn.neural_network import MLPClassifier
from sklearn.model_selection import train_test_split

from ml.dataset_loader import load_datasets
from ml.preprocessing import get_preprocessing_pipeline, split_features_target

def train_and_save_all_models(datasets_path="../datasets", models_dir="models"):
    os.makedirs(models_dir, exist_ok=True)
    datasets = load_datasets(datasets_path)
    
    if not datasets:
        print(f"No datasets found in {datasets_path} to train.")
        return

    for disease_name, df in datasets.items():
        print(f"Training pipeline for {disease_name}...")
        
        X, y = split_features_target(df)
        X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)
        
        # Define base models
        rf = RandomForestClassifier(n_estimators=100, random_state=42)
        lr = LogisticRegression(max_iter=1000, random_state=42)
        xgb = XGBClassifier(use_label_encoder=False, eval_metric="logloss", random_state=42)
        mlp = MLPClassifier(max_iter=1000, random_state=42)
        
        # Ensemble predictor
        ensemble = VotingClassifier(
            estimators=[('rf', rf), ('lr', lr), ('xgb', xgb), ('mlp', mlp)],
            voting='soft'
        )
        
        preprocessing = get_preprocessing_pipeline()
        
        # Combine preprocessing and model into a single pipeline
        pipeline = Pipeline(steps=[
            ('preprocessing', preprocessing),
            ('classifier', ensemble)
        ])
        
        # Attach feature names for inference time mapping
        pipeline.feature_names_in_ = list(X.columns)
        
        pipeline.fit(X_train, y_train)
        
        acc = pipeline.score(X_test, y_test)
        print(f"Accuracy for {disease_name}: {acc:.4f}")
        
        model_path = os.path.join(models_dir, f"{disease_name}_ensemble.pkl")
        joblib.dump(pipeline, model_path)
        print(f"Saved {disease_name} model to {model_path}")

if __name__ == "__main__":
    import sys
    ds_path = sys.argv[1] if len(sys.argv) > 1 else "../datasets"
    md_path = sys.argv[2] if len(sys.argv) > 2 else "models"
    train_and_save_all_models(ds_path, md_path)
