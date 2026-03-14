from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel
from typing import Dict, Any

from services.llm_service import llm_service
from routers.predict import PredictionResponse

router = APIRouter(prefix="/recommend", tags=["recommend"])

class RecommendationRequest(BaseModel):
    report_id: str
    parameters: Dict[str, float]
    predictions: PredictionResponse

class RecommendationResponse(BaseModel):
    report_id: str
    diet: str
    exercise: str
    doctor: str
    lifestyle: str

@router.post("", response_model=RecommendationResponse)
async def get_recommendations(req: RecommendationRequest):
    try:
        risks = {
            "diabetes_risk": req.predictions.diabetes_risk,
            "heart_disease_risk": req.predictions.heart_disease_risk,
            "anemia_risk": req.predictions.anemia_risk
        }
        recs = llm_service.generate_recommendations(req.parameters, risks)
        
        return RecommendationResponse(
            report_id=req.report_id,
            diet=recs["diet"],
            exercise=recs["exercise"],
            doctor=recs["doctor"],
            lifestyle=recs["lifestyle"]
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
