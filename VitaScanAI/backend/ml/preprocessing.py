import pandas as pd
from sklearn.pipeline import Pipeline
from sklearn.impute import SimpleImputer
from sklearn.preprocessing import StandardScaler

def get_preprocessing_pipeline() -> Pipeline:
    """
    Returns a reusable scikit-learn pipeline to handle missing values
    and normalize features.
    """
    return Pipeline(steps=[
        ('imputer', SimpleImputer(strategy='median')),
        ('scaler', StandardScaler())
    ])

def split_features_target(df: pd.DataFrame):
    """
    Splits the dataframe into features and target.
    Assumes the target is the last column.
    """
    X = df.iloc[:, :-1]
    y = df.iloc[:, -1]
    return X, y
