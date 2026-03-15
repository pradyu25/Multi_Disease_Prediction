from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, desc
from pydantic import BaseModel
from typing import List, Optional
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials

from database import get_db
from models.db_models import Report, Prediction, MedicalValue
from core.security import decode_token

router = APIRouter(prefix="/reports", tags=["reports"])
security = HTTPBearer()

def get_current_user(credentials: HTTPAuthorizationCredentials = Depends(security)):
    user_id = decode_token(credentials.credentials)
    if not user_id:
        raise HTTPException(status_code=401, detail="Invalid token")
    return user_id

class PredictionSummary(BaseModel):
    report_id: str
    diabetes_risk: int
    anemia_risk: int
    heart_disease_risk: int
    confidence: dict = {}

class ReportSummary(BaseModel):
    report_id: str
    file_url: str
    upload_date: str
    file_type: str
    predictions: Optional[PredictionSummary]

class HistoryResponse(BaseModel):
    reports: List[ReportSummary]
    total: int

class ParameterChange(BaseModel):
    parameter: str
    previous: Optional[float]
    current: Optional[float]
    change: Optional[float]
    status: str

class CompareResponse(BaseModel):
    report_id_previous: str
    report_id_current: str
    changes: List[ParameterChange]

@router.get("/history", response_model=HistoryResponse)
async def get_history(
    page: int = 1,
    limit: int = 10,
    user_id: str = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    offset = (page - 1) * limit
    
    # Needs a join with predictions in a real setup, simplified here
    result = await db.execute(
        select(Report)
        .where(Report.user_id == user_id)
        .order_by(desc(Report.upload_date))
        .offset(offset)
        .limit(limit)
    )
    reports_orm = result.scalars().all()
    
    # In a fully optimised setup, we'd use joinedload.
    # Here we do N+1 queries for simplicity in V1
    reports = []
    for r in reports_orm:
        pred_res = await db.execute(select(Prediction).where(Prediction.report_id == r.report_id))
        all_preds = pred_res.scalars().all()
        
        pred_summary = None
        if all_preds:
            # Map predictions to dictionary for easy lookup
            p_map = {p.disease: p.risk_score for p in all_preds}
            
            # If we have individual records (New system)
            diabetes = p_map.get("Diabetes")
            anemia = p_map.get("Anemia")
            heart = p_map.get("Heart Disease")
            
            # Fallback for old records or partial data
            if diabetes is None and anemia is None and heart is None:
                # Might be an old 'Multiple' record
                multiple = p_map.get("Multiple", 0)
                diabetes, anemia, heart = multiple, max(0, multiple-10), max(0, multiple-20)
            
            pred_summary = PredictionSummary(
                report_id=r.report_id,
                diabetes_risk=diabetes or 0,
                anemia_risk=anemia or 0,
                heart_disease_risk=heart or 0,
            )
            
        reports.append(ReportSummary(
            report_id=r.report_id,
            file_url=r.file_url,
            upload_date=r.upload_date.isoformat(),
            file_type=r.file_type,
            predictions=pred_summary
        ))
        
    return HistoryResponse(reports=reports, total=len(reports))

@router.get("/compare", response_model=CompareResponse)
async def compare_reports(
    report_id_1: str,
    report_id_2: str,
    user_id: str = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    # Fetch parameters for both
    res1 = await db.execute(select(MedicalValue).where(MedicalValue.report_id == report_id_1))
    vals1 = {v.parameter_name: v.value for v in res1.scalars().all()}
    
    res2 = await db.execute(select(MedicalValue).where(MedicalValue.report_id == report_id_2))
    vals2 = {v.parameter_name: v.value for v in res2.scalars().all()}
    
    all_params = set(vals1.keys()).union(set(vals2.keys()))
    changes = []
    
    for param in all_params:
        v1 = vals1.get(param)
        v2 = vals2.get(param)
        
        change_val = None
        status = "new"
        
        if v1 is not None and v2 is not None:
            change_val = v2 - v1
            
            # Simple status logic (Lower is better for most except vitamin D/platelets)
            if param in ["vitamin_d", "platelets"]:
                status = "improved" if change_val > 0 else "worsened" if change_val < 0 else "stable"
            else:
                status = "worsened" if change_val > 0 else "improved" if change_val < 0 else "stable"
                
        changes.append(ParameterChange(
            parameter=param,
            previous=v1,
            current=v2,
            change=change_val,
            status=status
        ))
        
    return CompareResponse(
        report_id_previous=report_id_1,
        report_id_current=report_id_2,
        changes=changes
    )
