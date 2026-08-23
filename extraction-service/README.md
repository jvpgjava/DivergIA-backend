# DivergIA — Serviço de extração de documento

Microsserviço Python (FastAPI + [Docling](https://github.com/docling-project/docling))
que extrai o texto de um arquivo (PDF, DOCX, etc.) enviado pelo backend Java.
Roda isolado, sem container, consumido pelo backend via HTTP
(`adapter/out/extraction`).

## Configuração (venv nativo, sem container)

```bash
cd extraction-service
python -m venv venv

# Linux/macOS
source venv/bin/activate
# Windows (PowerShell)
venv\Scripts\Activate.ps1

pip install -r requirements.txt
```

Para rodar os testes, instale também as dependências de desenvolvimento:

```bash
pip install -r requirements-dev.txt
```

> Na primeira execução, o Docling baixa alguns modelos de OCR (~30MB, uma
> vez só) para dentro do próprio `venv`. Isso acontece automaticamente na
> primeira chamada a `/extrair`, sem passo manual.

## Rodando o serviço

```bash
uvicorn app.main:app --host 0.0.0.0 --port 8000
```

- Health check: `GET http://localhost:8000/health`
- Extração: `POST http://localhost:8000/extrair` (multipart, campo `arquivo`)

```bash
curl http://localhost:8000/health
curl -X POST http://localhost:8000/extrair -F "arquivo=@caminho/para/documento.pdf"
```

## Rodando os testes

```bash
pytest
```
