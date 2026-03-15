from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from pydantic import BaseModel
from typing import Dict, Any
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials

from database import get_db
from models.db_models import Report, Prediction
from services.ml_service import ml_service
from core.security import decode_token

router = APIRouter(prefix="/predict", tags=["predict"])
security = HTTPBearer()

def get_current_user_id(credentials: HTTPAuthorizationCredentials = Depends(security)) -> str:
    user_id = decode_token(credentials.credentials)
    if not user_id:
        raise HTTPException(status_code=401, detail="Invalid token")
    return user_id

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
async def predict_disease(
    req: PredictionRequest, 
    user_id: str = Depends(get_current_user_id),
    db: AsyncSession = Depends(get_db)
):
    # Verify ownership
    res = await db.execute(select(Report).where(Report.report_id == req.report_id))
    report = res.scalar_one_or_none()
    if not report or report.user_id != user_id:
        raise HTTPException(status_code=403, detail="Not authorized for this report")

    try:
        # Run inference
        results = ml_service.predict(req.parameters)
        
        # Clear old predictions for this report
        from sqlalchemy import delete
        await db.execute(delete(Prediction).where(Prediction.report_id == req.report_id))
        
        # Save individual predictions to DB for accurate history
        predictions = [
            Prediction(report_id=req.report_id, disease="Diabetes", risk_score=results["diabetes_risk"]),
            Prediction(report_id=req.report_id, disease="Anemia", risk_score=results["anemia_risk"]),
            Prediction(report_id=req.report_id, disease="Heart Disease", risk_score=results["heart_disease_risk"])
        ]
        db.add_all(predictions)
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
