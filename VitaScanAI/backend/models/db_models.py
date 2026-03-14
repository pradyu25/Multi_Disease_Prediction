import uuid
from datetime import datetime
from sqlalchemy import String, Float, Integer, ForeignKey, DateTime, func
from sqlalchemy.orm import Mapped, mapped_column, relationship
from database import Base


def new_uuid() -> str:
    return str(uuid.uuid4())


class User(Base):
    __tablename__ = "users"

    user_id: Mapped[str]    = mapped_column(String(36), primary_key=True, default=new_uuid)
    name:    Mapped[str]    = mapped_column(String(255), nullable=False)
    email:   Mapped[str]    = mapped_column(String(255), unique=True, nullable=False, index=True)
    password_hash: Mapped[str] = mapped_column(String(255), nullable=False)
    created_at: Mapped[datetime] = mapped_column(DateTime, server_default=func.now())

    reports: Mapped[list["Report"]] = relationship("Report", back_populates="user", cascade="all, delete")


class Report(Base):
    __tablename__ = "reports"

    report_id:   Mapped[str]      = mapped_column(String(36), primary_key=True, default=new_uuid)
    user_id:     Mapped[str]      = mapped_column(String(36), ForeignKey("users.user_id"), nullable=False)
    file_url:    Mapped[str]      = mapped_column(String(1024), nullable=False)
    file_type:   Mapped[str]      = mapped_column(String(50), nullable=False)
    upload_date: Mapped[datetime] = mapped_column(DateTime, server_default=func.now())

    user:          Mapped["User"]              = relationship("User", back_populates="reports")
    medical_values: Mapped[list["MedicalValue"]] = relationship("MedicalValue", back_populates="report", cascade="all, delete")
    predictions:   Mapped[list["Prediction"]]   = relationship("Prediction", back_populates="report", cascade="all, delete")


class MedicalValue(Base):
    __tablename__ = "medical_values"

    value_id:       Mapped[int]   = mapped_column(Integer, primary_key=True, autoincrement=True)
    report_id:      Mapped[str]   = mapped_column(String(36), ForeignKey("reports.report_id"), nullable=False)
    parameter_name: Mapped[str]   = mapped_column(String(100), nullable=False)
    value:          Mapped[float] = mapped_column(Float, nullable=True)

    report: Mapped["Report"] = relationship("Report", back_populates="medical_values")


class Prediction(Base):
    __tablename__ = "predictions"

    prediction_id:    Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    report_id:        Mapped[str] = mapped_column(String(36), ForeignKey("reports.report_id"), nullable=False)
    disease:          Mapped[str] = mapped_column(String(100), nullable=False)
    risk_score:       Mapped[int] = mapped_column(Integer, nullable=False)

    report: Mapped["Report"] = relationship("Report", back_populates="predictions")
