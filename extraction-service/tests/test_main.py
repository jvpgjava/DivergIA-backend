from pathlib import Path

from fastapi.testclient import TestClient

from app.main import app

client = TestClient(app)

FIXTURE_PDF = Path(__file__).parent / "fixtures" / "teste.pdf"


def test_deve_responder_health_check():
    resposta = client.get("/health")

    assert resposta.status_code == 200
    assert resposta.json() == {"status": "ok"}


def test_deve_extrair_texto_de_um_pdf_real():
    with FIXTURE_PDF.open("rb") as arquivo:
        resposta = client.post("/extrair", files={"arquivo": ("teste.pdf", arquivo, "application/pdf")})

    assert resposta.status_code == 200
    texto = resposta.json()["texto"]
    assert "DivergIA" in texto
    assert "extraido corretamente" in texto


def test_deve_rejeitar_arquivo_vazio():
    resposta = client.post("/extrair", files={"arquivo": ("vazio.pdf", b"", "application/pdf")})

    assert resposta.status_code == 400


def test_deve_rejeitar_arquivo_com_conteudo_nao_reconhecido():
    resposta = client.post(
        "/extrair", files={"arquivo": ("nao-e-um-documento.bin", b"\x00\x01\x02conteudo aleatorio", "application/octet-stream")}
    )

    assert resposta.status_code == 422
