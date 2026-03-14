import os
import shutil
from fastapi import APIRouter, Depends, HTTPException, File, UploadFile, Form
from pydantic import BaseModel
from sqlalchemy.ext.asyncio import AsyncSession
from typing import Optional
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
import jwt

from database import get_db
from models.db_models import Report
from core.config import get_settings
from core.security import decode_token

router = APIRouter(prefix="/upload", tags=["upload"])
settings = get_settings()
security = HTTPBearer()

class UploadResponse(BaseModel):
    report_id: str
    file_url: str
    message: str

def get_current_user_id(credentials: HTTPAuthorizationCredentials = Depends(security)) -> str:
    user_id = decode_token(credentials.credentials)
    if not user_id:
        raise HTTPException(status_code=401, detail="Invalid or expired token")
    return user_id

@router.post("/report", response_model=UploadResponse)
async def upload_report(
    file: UploadFile = File(...),
    user_id: str = Form(...),
    token: HTTPAuthorizationCredentials = Depends(security),
    db: AsyncSession = Depends(get_db)
):
    auth_user_id = get_current_user_id(token)
    if auth_user_id != user_id:
        raise HTTPException(status_code=403, detail="Not authorized to upload for this user")

    # Save file with unique ID
    os.makedirs(settings.upload_dir, exist_ok=True)
    file_ext = os.path.splitext(file.filename)[1]
    unique_filename = f"{new_report.report_id}{file_ext}"
    file_path = os.path.join(settings.upload_dir, unique_filename)
    
    with open(file_path, "wb") as buffer:
        shutil.copyfileobj(file.file, buffer)
        
    file_url = f"uploads/{unique_filename}"
    new_report.file_url = file_url
    await db.commit()
    await db.refresh(new_report)
    
    return UploadResponse(
        report_id=new_report.report_id,
        file_url=file_url,
        message="File uploaded successfully"
    )
