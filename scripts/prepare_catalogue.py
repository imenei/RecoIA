#!/usr/bin/env python3
"""
Prepare le catalogue RecoIA a partir d'un dataset Coursera (Kaggle).

Usage :
    pip install pandas
    python scripts/prepare_catalogue.py chemin/vers/coursera.csv --n 100 --out scripts/catalogue.csv

Les noms de colonnes varient selon la version du dataset : le script essaie de les
detecter automatiquement. Si une colonne n'est pas trouvee, adapte COLUMN_ALIASES.
"""
import argparse
import pandas as pd

COLUMN_ALIASES = {
    "title": ["title", "course name", "course_name", "name", "course title", "course"],
    "description": ["description", "course description", "course_description", "about", "summary"],
    "skills": ["skills", "skill", "tags", "keywords"],
    "level": ["level", "difficulty level", "difficulty", "course_difficulty"],
    "category": ["category", "subject", "domain", "topic", "course_category"],
}

# Mots-cles -> categorie (utilise si le dataset n'a pas de colonne categorie)
CATEGORY_KEYWORDS = {
    "Data Science": ["data", "machine learning", "statistic", "deep learning", "analytics", "python", "ai "],
    "Informatique": ["programming", "software", "web", "java", "cloud", "security", "network", "computer", "cyber"],
    "Business": ["business", "management", "marketing", "finance", "entrepreneur", "leadership", "strategy"],
    "Langues": ["english", "language", "spanish", "french", "writing", "communication"],
    "Sante": ["health", "medical", "nutrition", "clinical", "psychology", "care"],
    "Arts et design": ["design", "art", "music", "photography", "creative", "film"],
}
DEFAULT_CATEGORY = "Autre"


def find_column(df, key):
    lowered = {c.lower().strip(): c for c in df.columns}
    for alias in COLUMN_ALIASES[key]:
        if alias in lowered:
            return lowered[alias]
    return None


def infer_category(text):
    text = f" {str(text).lower()} "
    for cat, words in CATEGORY_KEYWORDS.items():
        if any(w in text for w in words):
            return cat
    return DEFAULT_CATEGORY


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("input")
    ap.add_argument("--n", type=int, default=100, help="nombre de formations a garder")
    ap.add_argument("--out", default="scripts/catalogue.csv")
    ap.add_argument("--seed", type=int, default=42)
    args = ap.parse_args()

    raw = pd.read_csv(args.input)
    print("Colonnes du fichier :", list(raw.columns))

    cols = {k: find_column(raw, k) for k in COLUMN_ALIASES}
    print("Colonnes detectees :", cols)
    if not cols["title"]:
        raise SystemExit("Colonne titre introuvable : adapte COLUMN_ALIASES.")

    df = pd.DataFrame({
        "title": raw[cols["title"]],
        "description": raw[cols["description"]] if cols["description"] else "",
        "skills": raw[cols["skills"]] if cols["skills"] else "",
        "level": raw[cols["level"]] if cols["level"] else "Tous niveaux",
        "category": raw[cols["category"]] if cols["category"] else None,
    })

    df = df.dropna(subset=["title"]).drop_duplicates(subset=["title"])
    df["description"] = df["description"].fillna("").astype(str)
    df["skills"] = df["skills"].fillna("").astype(str)
    df["level"] = df["level"].fillna("Tous niveaux").astype(str)

    # garder les descriptions exploitables si elles existent
    if cols["description"]:
        df = df[df["description"].str.len() > 100]

    if df["category"].isna().all():
        text = df["title"].astype(str) + " " + df["skills"] + " " + df["description"]
        df["category"] = text.map(infer_category)
    df["category"] = df["category"].fillna(DEFAULT_CATEGORY)

    # echantillon equilibre par categorie
    cats = df["category"].unique()
    per_cat = max(1, args.n // len(cats))
    parts = [g.sample(n=min(len(g), per_cat), random_state=args.seed) for _, g in df.groupby("category")]
    sample = pd.concat(parts)
    if len(sample) < args.n:  # complete avec des formations restantes
        rest = df.drop(sample.index)
        sample = pd.concat([sample, rest.sample(n=min(len(rest), args.n - len(sample)), random_state=args.seed)])
    sample = sample.head(args.n).reset_index(drop=True)

    sample["image_url"] = sample.index.map(lambda i: f"https://picsum.photos/seed/reco{i}/400/250")

    sample[["title", "description", "category", "level", "skills", "image_url"]].to_csv(args.out, index=False)
    print(f"{len(sample)} formations ecrites dans {args.out}")
    print(sample["category"].value_counts())


if __name__ == "__main__":
    main()
