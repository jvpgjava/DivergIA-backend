from io import BytesIO

from docling.datamodel.base_models import DocumentStream
from docling.document_converter import DocumentConverter
from docling.exceptions import ConversionError
from fastapi import FastAPI, HTTPException, UploadFile

app = FastAPI(title="DivergIA — Extração de Documento")

_converter = DocumentConverter()


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "ok"}


@app.post("/extrair")
async def extrair(arquivo: UploadFile) -> dict[str, str]:
    conteudo = await arquivo.read()
    if not conteudo:
        raise HTTPException(status_code=400, detail="Arquivo vazio")

    stream = DocumentStream(name=arquivo.filename or "documento", stream=BytesIO(conteudo))

    try:
        resultado = _converter.convert(stream)
    except ConversionError as erro:
        raise HTTPException(
            status_code=422, detail=f"Não foi possível extrair texto do documento: {erro}"
        ) from erro

    return {"texto": resultado.document.export_to_text()}
