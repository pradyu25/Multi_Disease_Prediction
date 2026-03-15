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

import httpx
from core.config import get_settings

settings = get_settings()
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
    token: HTTPAuthorizationCredentials = Depends(security),
    db: AsyncSession = Depends(get_db)
):
    user_id = get_current_user_id(token)
    
    # Verify ownership
    res = await db.execute(select(Report).where(Report.report_id == req.report_id))
    report = res.scalar_one_or_none()
    if not report or report.user_id != user_id:
        raise HTTPException(status_code=403, detail="Not authorized for this report")
        
    # Gateway Mode: Proxy to dedicated recommender service if role is 'core'
    if settings.service_role == "core" and settings.recommender_url:
        try:
            async with httpx.AsyncClient() as client:
                response = await client.post(
                    f"{settings.recommender_url.rstrip('/')}/recommend",
                    json=req.model_dump(),
                    headers={"Authorization": f"Bearer {token.credentials}"},
                    timeout=60.0 # LLM can be slow
                )
                if response.status_code == 200:
                    return RecommendationResponse(**response.json())
                else:
                    raise HTTPException(status_code=response.status_code, detail="Remote recommender error")
        except Exception as e:
            print(f"Proxy Error: {e}")
            # Fallback will happen below if we don't return
            
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
