from sqlalchemy.ext.asyncio import create_async_engine, async_sessionmaker, AsyncSession
from sqlalchemy.orm import DeclarativeBase
from core.config import get_settings

settings = get_settings()

engine = create_async_engine(
    settings.database_url_async,
    echo=settings.debug,
    pool_pre_ping=True,
    connect_args={"ssl": True} if "postgresql" in settings.database_url_async else {}
)

AsyncSessionLocal = async_sessionmaker(
    engine,
    class_=AsyncSession,
    expire_on_commit=False
)


class Base(DeclarativeBase):
    pass


async def get_db():
    async with AsyncSessionLocal() as session:
        try:
            yield session
        except Exception:
            await session.rollback()
            raise
        finally:
            await session.close()


async def init_db():
    db_host = settings.database_url_async.split('@')[-1]
    print(f"Connecting to database at: {db_host}")
    
    # Check for placeholder value
    if "placeholder-replace-in-dashboard" in settings.database_url_async:
        print("\n" + "="*60)
        print("CRITICAL ALERT: DEFAULT DATABASE PLACEHOLDER DETECTED")
        print("="*60)
        print("Your service is still using the placeholder 'DATABASE_URL'.")
        print("ACTION REQUIRED:")
        print("1. Go to your Render Dashboard.")
        print("2. Click 'Env Groups' in the sidebar.")
        print("3. Edit the 'vitascan-shared' group.")
        print("4. Replace DATABASE_URL with your actual PostgreSQL Internal URL.")
        print("="*60 + "\n")
        raise ConnectionError("Database URL not configured. See logs above for instructions.")

    try:
        async with engine.begin() as conn:
            await conn.run_sync(Base.metadata.create_all)
        print("Database tables initialized successfully.")
    except Exception as e:
        print(f"DATABASE ERROR: {e}")
        if "gaierror" in str(e):
            print("CRITICAL: Hostname could not be resolved. Please check your DATABASE_URL.")
            print("If using Render, ensure you are using the EXTERNAL Database URL if regions differ.")
        raise e
