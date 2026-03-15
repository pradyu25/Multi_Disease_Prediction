from fastapi import APIRouter, Depends, HTTPException
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from pydantic import BaseModel
from typing import Dict, Any
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select

from database import get_db
from models.db_models import Report
from services.llm_service import llm_service
from routers.predict import PredictionResponse
from core.security import decode_token

router = APIRouter(prefix="/recommend", tags=["recommend"])
security = HTTPBearer()

def get_current_user_id(credentials: HTTPAuthorizationCredentials = Depends(security)) -> str:
    user_id = decode_token(credentials.credentials)
    if not user_id:
        raise HTTPException(status_code=401, detail="Invalid token")
    return user_id

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
async def get_recommendations(
    req: RecommendationRequest,
    user_id: str = Depends(get_current_user_id),
    db: AsyncSession = Depends(get_db)
):
    # Verify ownership
    res = await db.execute(select(Report).where(Report.report_id == req.report_id))
    report = res.scalar_one_or_none()
    if not report or report.user_id != user_id:
        raise HTTPException(status_code=403, detail="Not authorized for this report")
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
