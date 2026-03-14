import re
import pdfplumber
import docx
import pytesseract
from PIL import Image
from typing import Dict, Optional, Tuple
from core.config import get_settings

settings = get_settings()

pytesseract.pytesseract.tesseract_cmd = settings.tesseract_cmd

# Medical parameters to look for - more flexible regex
PARAMETERS = {
    "glucose": r"(?i)(?:glucose|sugar|fasting\s+glucose).*?\s+(\d+\.?\d*)\s*(?:mg/dl|mmol/l)?",
    "cholesterol": r"(?i)(?:total\s+)?cholesterol.*?\s+(\d+\.?\d*)\s*(?:mg/dl)?",
    "hemoglobin": r"(?i)h[a]?emoglobin.*?\s+(\d+\.?\d*)\s*(?:g/dl)?",
    "platelets": r"(?i)platelets?.*?\s+(\d+\.?\d*)\s*(?:10\^3/ul|thousands/ul|/mm3)?",
    "vitamin_d": r"(?i)vitamin\s*d.*?\s+(\d+\.?\d*)\s*(?:ng/ml|nmol/l)?",
    "blood_pressure_systolic": r"(?i)(?:bp|blood\s*pressure).*?\s+(\d{2,3})\s*/\s*(\d{2,3})",
    "blood_pressure_diastolic": r"(?i)(?:bp|blood\s*pressure).*?\s+(\d{2,3})\s*/\s*(\d{2,3})"
}


async def extract_text_from_file(file_path: str, mime_type: str) -> str:
    text = ""
    try:
        if mime_type == "application/pdf":
            with pdfplumber.open(file_path) as pdf:
                for page in pdf.pages:
                    page_text = page.extract_text()
                    if page_text:
                        text += page_text + "\n"
        elif "wordprocessingml.document" in mime_type:
            doc = docx.Document(file_path)
            for para in doc.paragraphs:
                text += para.text + "\n"
        elif mime_type.startswith("image/"):
            img = Image.open(file_path)
            text = pytesseract.image_to_string(img)
    except Exception as e:
        print(f"Error extracting text: {e}")
    return text


def parse_medical_values(text: str) -> Dict[str, Optional[float]]:
    extracted: Dict[str, Optional[float]] = {k: None for k in PARAMETERS.keys()}
    
    # Blood pressure is special because it has two groups
    bp_match = re.search(PARAMETERS["blood_pressure_systolic"], text)
    if bp_match:
        extracted["blood_pressure_systolic"] = float(bp_match.group(1))
        extracted["blood_pressure_diastolic"] = float(bp_match.group(2))
    
    # Extract other parameters
    for param, pattern in PARAMETERS.items():
        if param.startswith("blood_pressure"):
            continue
        match = re.search(pattern, text)
        if match:
            extracted[param] = float(match.group(1))
            
    return extracted

async def process_report(file_path: str, mime_type: str) -> Tuple[Dict[str, Optional[float]], str]:
    text = await extract_text_from_file(file_path, mime_type)
    values = parse_medical_values(text)
    
    type_map = {
        "application/pdf": "pdf",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document": "docx",
        "image/jpeg": "image",
        "image/png": "image",
    }
    extracted_type = type_map.get(mime_type, "unknown")
    
    return values, extracted_type
