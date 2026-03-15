from pydantic_settings import BaseSettings, SettingsConfigDict
from functools import lru_cache


class Settings(BaseSettings):
    # App
    app_name: str = "VitaScan AI API"
    app_version: str = "1.0.0"
    debug: bool = False
    service_role: str = "all" # Options: all, core, recommender
    recommender_url: str = "" # URL for the recommender service

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

    # HuggingFace (Small model for Render 512MB RAM)
    hf_model_name: str = "google/flan-t5-small"
    use_llm: bool = True

    # Tesseract
    tesseract_cmd: str = "/usr/bin/tesseract"

    model_config = SettingsConfigDict(
        env_file=".env", 
        extra="ignore",
        protected_namespaces=('settings_',)
    )

    @property
    def database_url_async(self) -> str:
        """Ensures the database URL uses the asyncpg driver."""
        url = self.database_url
        if url.startswith("postgresql://"):
            return url.replace("postgresql://", "postgresql+asyncpg://", 1)
        return url


@lru_cache
def get_settings() -> Settings:
    return Settings()
