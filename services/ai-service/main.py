from fastapi import FastAPI
from pydantic import BaseModel
from sentence_transformers import SentenceTransformer

app = FastAPI()
model = SentenceTransformer("paraphrase-multilingual-MiniLM-L12-v2")

class Req(BaseModel):
    texts: list[str]

@app.post("/embed")
def embed(req: Req):
    return {"vectors": model.encode(req.texts, normalize_embeddings=True).tolist()}