from decimal import Decimal
import io
import re
from typing import Dict

from fastapi import FastAPI, UploadFile, File
from PIL import Image
from pypdf import PdfReader

try:
    import pytesseract
except ImportError:  # pragma: no cover
    pytesseract = None

app = FastAPI(title="SmartTax OCR Service")


@app.get("/health")
def health() -> dict:
    return {"status": "ok"}


@app.post("/extract")
async def extract(file: UploadFile = File(...)) -> dict:
    raw = await file.read()
    text = extract_text(file.filename or "", raw)
    fields = parse_tax_fields(text)
    return {
        "fileName": file.filename,
        "documentType": guess_document_type(text),
        "annualIncome": fields.get("annualIncome", "0"),
        "section80C": fields.get("section80C", "0"),
        "hraExemption": fields.get("hraExemption", "0"),
        "tdsPaid": fields.get("tdsPaid", "0"),
        "rawFields": fields,
    }


def extract_text(filename: str, raw: bytes) -> str:
    lowered = filename.lower()
    if lowered.endswith(".txt"):
        return raw.decode("utf-8", errors="ignore")
    if lowered.endswith(".pdf"):
        reader = PdfReader(io.BytesIO(raw))
        return "\n".join(page.extract_text() or "" for page in reader.pages)
    if pytesseract is None:
        return ""
    image = Image.open(io.BytesIO(raw))
    return pytesseract.image_to_string(image)


def parse_tax_fields(text: str) -> Dict[str, str]:
    normalized = re.sub(r"[,₹]", "", text, flags=re.IGNORECASE)
    return {
        "annualIncome": find_amount(normalized, [r"gross\s+salary", r"annual\s+income", r"total\s+income"]),
        "section80C": find_amount(normalized, [r"80c", r"section\s+80c"]),
        "hraExemption": find_amount(normalized, [r"hra", r"house\s+rent\s+allowance"]),
        "tdsPaid": find_amount(normalized, [r"tds", r"tax\s+deducted"]),
    }


def find_amount(text: str, labels: list[str]) -> str:
    for label in labels:
        match = re.search(label + r"[^0-9]{0,40}([0-9]+(?:\.[0-9]{1,2})?)", text, flags=re.IGNORECASE)
        if match:
            return str(Decimal(match.group(1)).quantize(Decimal("1")))
    return "0"


def guess_document_type(text: str) -> str:
    lowered = text.lower()
    if "form 16" in lowered or "form-16" in lowered:
        return "FORM_16"
    if "salary slip" in lowered or "payslip" in lowered:
        return "SALARY_SLIP"
    return "UNKNOWN"
