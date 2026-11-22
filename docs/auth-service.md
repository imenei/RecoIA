# auth-service (port 8081)

Comptes, connexion, roles et centres d'interet. Jetons JWT signes en HS256 (classe `JwtCodec`, sans bibliotheque).

## Donnees de test (creees au premier demarrage si la table est vide)

| Compte | Email | Mot de passe |
|---|---|---|
| Administrateur | `admin@recoia.dz` | `Admin123!` |
| 30 etudiants | `etudiant01@recoia.dz` a `etudiant30@recoia.dz` | `Student123!` |

Chaque etudiant a 2 a 5 centres d'interet, coherents avec un profil (data, web, business, design, langues, sante).

## API (via la gateway : http://localhost:8080)

| Methode | Chemin | Acces | Role |
|---|---|---|---|
| POST | `/api/auth/register` | public | cree un etudiant, renvoie `{ token, user }` |
| POST | `/api/auth/login` | public | renvoie `{ token, user }` |
| POST | `/api/auth/logout` | public | 204 (le client oublie son jeton) |
| GET | `/api/auth/me` | jeton | profil connecte |
| PUT | `/api/auth/me` | jeton | modifie nom et centres d'interet |
| GET | `/api/auth/users` | jeton ADMIN | liste des comptes |
| GET | `/internal/users/{id}`, `/internal/users/students` | interne | pour activity-service et reco-service (non routes par la gateway) |

## Tester dans PowerShell

```powershell
$body = @{ email = "etudiant01@recoia.dz"; password = "Student123!" } | ConvertTo-Json
$r = Invoke-RestMethod -Method Post -Uri http://localhost:8081/api/auth/login -ContentType "application/json" -Body $body
$r.user
Invoke-RestMethod -Uri http://localhost:8081/api/auth/me -Headers @{ Authorization = "Bearer $($r.token)" }
Invoke-RestMethod -Uri http://localhost:8081/api/auth/users -Headers @{ Authorization = "Bearer $($r.token)" }   # 403 : etudiant
```

## Tests

`mvn test` dans `services/auth-service` : `JwtCodecTest`, `SampleUsersTest`, `InterestsTest`.
