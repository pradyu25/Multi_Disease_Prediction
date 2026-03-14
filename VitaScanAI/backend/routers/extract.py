from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from pydantic import BaseModel
from typing import Dict, Optional

from database import get_db
from models.db_models import Report, MedicalValue
from services.ocr_service import process_report

router = APIRouter(prefix="/extract", tags=["extract"])

class ExtractedData(BaseModel):
    report_id: str
    parameters: Dict[str, Optional[float]]
    detected_type: str

@router.get("/data", response_model=ExtractedData)
async def extract_data(report_id: str, db: AsyncSession = Depends(get_db)):
    result = await db.execute(select(Report).where(Report.report_id == report_id))
    report = result.scalar_one_or_none()
    
    if not report:
        raise HTTPException(status_code=404, detail="Report not found")
        
    file_path = report.file_url
    try:
        values, doc_type = await process_report(file_path, report.file_type)
        
        # Save to DB
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
