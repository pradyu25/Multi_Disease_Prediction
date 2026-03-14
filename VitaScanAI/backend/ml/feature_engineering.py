import pandas as pd
import numpy as np
from sklearn.preprocessing import StandardScaler

class MedicalFeatureEngineer:
    """
    Handles scaling and normalization of medical parameters for inference.
    """
    def __init__(self):
        self.scaler = StandardScaler()
        # Mock pre-fit stats covering typical biological ranges
        self.means = np.array([100.0, 190.0, 14.5, 250.0, 35.0, 120.0, 80.0])
        self.scale = np.array([20.0, 40.0, 1.5, 50.0, 10.0, 15.0, 10.0])
        self.scaler.mean_ = self.means
        self.scaler.scale_ = self.scale
        
    def transform(self, df: pd.DataFrame) -> pd.DataFrame:
        """Standardises input dataframe based on population means."""
        scaled = self.scaler.transform(df)
        return pd.DataFrame(scaled, columns=df.columns)
        
    def fit_transform(self, df: pd.DataFrame) -> pd.DataFrame:
        scaled = self.scaler.fit_transform(df)
        return pd.DataFrame(scaled, columns=df.columns)
