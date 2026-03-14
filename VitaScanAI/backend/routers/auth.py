from fastapi import APIRouter, Depends, HTTPException, status
from pydantic import BaseModel
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from datetime import datetime

from database import get_db
from models.db_models import User
from core.security import hash_password, verify_password, create_access_token

router = APIRouter(prefix="/auth", tags=["auth"])

class LoginRequest(BaseModel):
    email: str
    password: str

class SignupRequest(BaseModel):
    name: str
    email: str
    password: str

class UserDto(BaseModel):
    user_id: str
    name: str
    email: str
    created_at: str

class AuthResponse(BaseModel):
    access_token: str
    token_type: str
    user: UserDto

@router.post("/signup", response_model=AuthResponse)
async def signup(req: SignupRequest, db: AsyncSession = Depends(get_db)):
    result = await db.execute(select(User).where(User.email == req.email))
    existing_user = result.scalar_one_or_none()
    
    if existing_user:
        raise HTTPException(status_code=400, detail="Email already registered")
        
    new_user = User(
        name=req.name,
        email=req.email,
        password_hash=hash_password(req.password)
    )
    db.add(new_user)
    await db.commit()
    await db.refresh(new_user)
    
    token = create_access_token(subject=new_user.user_id)
    
    user_dto = UserDto(
        user_id=new_user.user_id,
        name=new_user.name,
        email=new_user.email,
        created_at=new_user.created_at.isoformat()
    )
    return AuthResponse(access_token=token, token_type="bearer", user=user_dto)

@router.post("/login", response_model=AuthResponse)
async def login(req: LoginRequest, db: AsyncSession = Depends(get_db)):
    result = await db.execute(select(User).where(User.email == req.email))
    user = result.scalar_one_or_none()
    
    if not user or not verify_password(req.password, user.password_hash):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Incorrect email or password",
            headers={"WWW-Authenticate": "Bearer"},
        )
        
    token = create_access_token(subject=user.user_id)
    
    user_dto = UserDto(
        user_id=user.user_id,
        name=user.name,
        email=user.email,
        created_at=user.created_at.isoformat()
    )
    return AuthResponse(access_token=token, token_type="bearer", user=user_dto)
