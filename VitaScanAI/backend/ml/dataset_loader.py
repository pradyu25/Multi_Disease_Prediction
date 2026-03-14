import os
import glob
import pandas as pd

def load_datasets(base_path="../datasets"):
    """
    Scans the given directory for .csv files.
    Treats each file as a separate disease prediction dataset.
    """
    datasets = {}
    if not os.path.exists(base_path):
        print(f"Directory {base_path} not found. Creating it.")
        os.makedirs(base_path, exist_ok=True)
        return datasets

    for filepath in glob.glob(os.path.join(base_path, "*.csv")):
        filename = os.path.basename(filepath)
        disease_name = os.path.splitext(filename)[0]
        try:
            df = pd.read_csv(filepath)
            datasets[disease_name] = df
            print(f"Loaded dataset for {disease_name}")
        except Exception as e:
            print(f"Error loading {filepath}: {e}")
            
    return datasets
