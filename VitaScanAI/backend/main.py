from contextlib import asynccontextmanager
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
import os

from core.config import get_settings
from database import init_db

from routers import auth, upload, extract, predict, recommend, reports

settings = get_settings()

@asynccontextmanager
async def lifespan(app: FastAPI):
    # Initialize Database
    await init_db()
    
    # Ensure upload directory exists
    os.makedirs(settings.upload_dir, exist_ok=True)
    
    yield
    # Cleanup on shutdown...

app = FastAPI(
    title=settings.app_name,
    version=settings.app_version,
    lifespan=lifespan
)

# CORS configuration for Android emulator
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Register routers
app.include_router(auth.router)
app.include_router(upload.router)
app.include_router(extract.router)
app.include_router(predict.router)
app.include_router(recommend.router)
app.include_router(reports.router)

@app.get("/")
async def root():
    return {"message": f"Welcome to {settings.app_name} API"}
