import os
import glob
import joblib

class ModelLoader:
    def __init__(self, models_dir="models"):
        self.models_dir = models_dir
        self.models = {}

    def load_all_models(self):
        self.models = {}
        if not os.path.exists(self.models_dir):
            print(f"Models directory '{self.models_dir}' not found.")
            return

        pkl_files = glob.glob(os.path.join(self.models_dir, "*_ensemble.pkl"))
        for filepath in pkl_files:
            filename = os.path.basename(filepath)
            disease = filename.replace("_ensemble.pkl", "")
            try:
                model = joblib.load(filepath)
                self.models[disease] = model
                print(f"Successfully loaded model: {disease}")
            except Exception as e:
                print(f"Failed to load {filepath}: {e}")

model_loader = ModelLoader("models")
