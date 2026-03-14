# VitaScan AI

A production-ready MedTech Android application and FastAPI backend for medical report analysis.

## Features
- **Android App**: MVVM, Jetpack Compose, Coroutines, Hilt DI, Room DB offline caching.
- **FastAPI Backend**: Async endpoints, PostgreSQL, JWT Auth.
- **ML Pipeline**: Scikit-learn VotingClassifier Ensemble (RandomForest, XGBoost, LogisticRegression) for risk prediction.
- **LLM Engine**: HuggingFace `flan-t5-base` for dietary, exercise, and medical recommendations.
- **OCR Engine**: Multi-modal report parsing (PDF, DOCX, Images via Tesseract, pdfplumber, python-docx).

## System Architecture

```text
[ Android Compose UI ] <--> [ Retrofit API Layer ]
          ^                           v
          |                     [ FastAPI ] <--> [ Tesseract OCR ]
          v                           |
[ Room Local Database ]               v
                              [ ML Predictor ] <--> [ LLM Recommender ]
                                      |
                              [ PostgreSQL DB ]
```

## Quick Start (Backend)

1. **Prerequisites**: Docker and Docker Compose installed.
2. **Build and Run**:
   ```bash
   cd e:/MDP/VitaScanAI
   docker-compose up --build -d
   ```
   *Note: The first build will take several minutes as it downloads PyTorch, HuggingFace models, and generates the synthetic ML dataset.*

3. **Verify API**: Open `http://localhost:8000/docs` to view the Swagger UI.

## Android Setup

1. Open `e:/MDP/VitaScanAI` in **Android Studio**.
2. Sync the Gradle project.
3. Ensure you have an Android SDK (min SDK 26, target 35).
4. Run the app on an emulator or physical device.
   *Note: If running on a physical device, update `BASE_URL` in `app/build.gradle.kts` to point to your computer's local network IP address instead of `10.0.2.2`.*

## Default Credentials (if you want to bypass signup)
You can create a new account via the App or Swagger UI at `/auth/signup`.
