from sqlalchemy.ext.asyncio import create_async_engine, async_sessionmaker, AsyncSession
from sqlalchemy.orm import DeclarativeBase
from core.config import get_settings

settings = get_settings()

engine = create_async_engine(
    settings.database_url_async,
    echo=settings.debug,
    pool_pre_ping=True,
    connect_args={
        "ssl": "prefer" # Try SSL first, but allow non-SSL if server doesn't support it
    } if "postgresql" in settings.database_url_async else {}
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
    print(f"Connecting to database at: {settings.database_url_async.split('@')[-1]}") # Log host details only
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
