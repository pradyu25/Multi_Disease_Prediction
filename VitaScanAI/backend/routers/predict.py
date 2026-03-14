from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel
from sqlalchemy.ext.asyncio import AsyncSession
from typing import Dict, Any

from database import get_db
from models.db_models import Prediction
from services.ml_service import ml_service

router = APIRouter(prefix="/predict", tags=["predict"])

class PredictionRequest(BaseModel):
    report_id: str
    parameters: Dict[str, float]

class PredictionResponse(BaseModel):
    report_id: str
    diabetes_risk: int
    anemia_risk: int
    heart_disease_risk: int
    confidence: Dict[str, float]

@router.post("/disease", response_model=PredictionResponse)
async def predict_disease(req: PredictionRequest, db: AsyncSession = Depends(get_db)):
    try:
        # Run inference
        results = ml_service.predict(req.parameters)
        
        # Save to DB
        prediction = Prediction(
            report_id=req.report_id,
            disease="Multiple",
            risk_score=max(
                results["diabetes_risk"], 
                results["anemia_risk"], 
                results["heart_disease_risk"]
            )
        )
        db.add(prediction)
        await db.commit()
        
        return PredictionResponse(
            report_id=req.report_id,
            diabetes_risk=results["diabetes_risk"],
            anemia_risk=results["anemia_risk"],
            heart_disease_risk=results["heart_disease_risk"],
            confidence=results["confidence"]
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
