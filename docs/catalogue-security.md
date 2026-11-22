# Securite du catalogue

`catalogue-service` verifie lui-meme les jetons JWT emis par `auth-service` (meme secret `JWT_SECRET`).

| Requete sur `/api/courses/**` et `/api/images/**` | Sans jeton | Etudiant | Administrateur |
|---|---|---|---|
| GET (lecture, recherche, images) | autorise | autorise | autorise |
| POST, PUT, PATCH, DELETE (ajout, modification, suppression, import CSV, envoi d'image) | 401 | 403 | autorise |

- Code : `services/catalogue-service/.../security/` (`AccessPolicy` = la regle, `AdminGuardInterceptor` = son application).
- `/internal/**` (appels entre services) n'est pas concerne.
- Le secret doit etre identique dans `auth-service` et `catalogue-service` : variable `JWT_SECRET`, sinon la valeur de developpement par defaut.
- `JwtCodec` est une copie de celui de `auth-service` : un jeton emis par l'un est accepte par l'autre (verifie par test).
