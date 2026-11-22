# catalogue-service (port 8082)

Base : PostgreSQL `catalogue_db` (a creer : `CREATE DATABASE catalogue_db;`). Tables creees automatiquement.
Au premier demarrage, si la table est vide, `data/courses.csv` (96 formations) est importe.

| Methode | URL | Role |
|---|---|---|
| GET | /api/courses?q=&level=&category=&minRating=&page=0&size=12&sort=rating,desc | recherche + pagination |
| GET | /api/courses/{id} | detail |
| GET | /api/courses/all | tout le catalogue (pour reco-service) |
| POST / PUT / DELETE | /api/courses, /api/courses/{id} | admin |
| POST | /api/courses/import (multipart `file`) | import CSV (colonnes maison ou Coursera) |
| POST | /api/courses/{id}/image (multipart `file`) | upload JPG/PNG/WebP, max 5 Mo |
| GET | /api/images/{fichier} | affichage de l'image |

Niveaux Coursera -> interface : Beginner=Licence, Intermediate=Master, Advanced=Doctorat, Conditionally Mixed=Certifications.
Pas encore de securite : le JWT sera ajoute avec auth-service.
