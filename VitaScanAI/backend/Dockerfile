FROM python:3.11-slim

# Install system dependencies required for OCR (tesseract) and OpenCV/Image processing
RUN apt-get update && apt-get install -y \
    tesseract-ocr \
    libtesseract-dev \
    libgl1 \
    libglib2.0-0 \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Install CPU-only PyTorch to save 5GB+ of docker image space
RUN pip install torch==2.5.1+cpu --index-url https://download.pytorch.org/whl/cpu

# Copy requirements and install
COPY requirements.txt .
# Use no-cache-dir to reduce image size. 
# We increase timeout because PyTorch/Transformers can be large
RUN pip install --no-cache-dir --default-timeout=100 -r requirements.txt
RUN pip install PyJWT==2.10.1

# Copy source code
COPY . .

# Run the ML training script during build to pre-generate the ensemble model
RUN python ml/train_model.py

# Expose FastAPI port
EXPOSE 8000

# Run uvicorn server with dynamic port for Render
CMD ["sh", "-c", "uvicorn main:app --host 0.0.0.0 --port ${PORT:-8000}"]
