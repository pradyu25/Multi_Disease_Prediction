from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from pydantic import BaseModel
from typing import Dict, Optional
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials

from database import get_db
from models.db_models import Report, MedicalValue
from services.ocr_service import process_report
from core.security import decode_token

router = APIRouter(prefix="/extract", tags=["extract"])
security = HTTPBearer()

def get_current_user_id(credentials: HTTPAuthorizationCredentials = Depends(security)) -> str:
    user_id = decode_token(credentials.credentials)
    if not user_id:
        raise HTTPException(status_code=401, detail="Invalid token")
    return user_id

class ExtractedData(BaseModel):
    report_id: str
    parameters: Dict[str, Optional[float]]
    detected_type: str

@router.get("/data", response_model=ExtractedData)
async def extract_data(
    report_id: str, 
    user_id: str = Depends(get_current_user_id),
    db: AsyncSession = Depends(get_db)
):
    result = await db.execute(select(Report).where(Report.report_id == report_id))
    report = result.scalar_one_or_none()
    
    if not report:
        raise HTTPException(status_code=404, detail="Report not found")
        
    if report.user_id != user_id:
        raise HTTPException(status_code=403, detail="Not authorized to access this report")
        
    file_path = report.file_url
    try:
        values, doc_type = await process_report(file_path, report.file_type)
        
        # Clear old values for this report (re-extraction)
        from sqlalchemy import delete
        await db.execute(delete(MedicalValue).where(MedicalValue.report_id == report_id))
        
        # Save new ones to DB
        param_entities = [
            MedicalValue(report_id=report_id, parameter_name=k, value=v)
            for k, v in values.items() if v is not None
        ]
        db.add_all(param_entities)
        await db.commit()
        
        return ExtractedData(
            report_id=report_id,
            parameters=values,
            detected_type=doc_type
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Extraction failed: {str(e)}")
