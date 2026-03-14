from pydantic_settings import BaseSettings, SettingsConfigDict
from functools import lru_cache


class Settings(BaseSettings):
    # App
    app_name: str = "VitaScan AI API"
    app_version: str = "1.0.0"
    debug: bool = False

    # Database
    database_url: str = "postgresql+asyncpg://user:password@localhost:5432/vitascan"

    # JWT
    secret_key: str = "CHANGE_ME_IN_PRODUCTION_USE_64_CHAR_RANDOM_HEX"
    algorithm: str = "HS256"
    access_token_expire_minutes: int = 1440  # 24 hours

    # Storage (local fallback, set s3_bucket for S3)
    upload_dir: str = "uploads"
    s3_bucket: str = ""
    aws_region: str = "us-east-1"

    # ML model paths
    model_path: str = "ml/models/ensemble.joblib"

    # HuggingFace
    hf_model_name: str = "google/flan-t5-base"

    # Tesseract
    tesseract_cmd: str = "/usr/bin/tesseract"

    model_config = SettingsConfigDict(env_file=".env", extra="ignore")


@lru_cache
def get_settings() -> Settings:
    return Settings()
