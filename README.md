# RecoIA : Find your next path

Application web full stack de recommandation de formations pour etudiants.
Microservices Spring (REST, GraphQL, gRPC), frontend Angular, moteur de recommandation hybride
(contenu TF-IDF + filtrage collaboratif + retours utilisateur).

## Structure

```
recoia/
├── config-repo/          configuration de chaque service (servie par config-server)
├── services/
│   ├── config-server/    [technique] :8888
│   ├── discovery-server/ [technique] :8761  (Eureka)
│   ├── api-gateway/      [technique] :8080
│   ├── auth-service/     [metier]    :8081
│   ├── catalogue-service/[metier]    :8082
│   ├── activity-service/ [metier]    :8083
│   ├── reco-service/     [metier]    :8084 + gRPC :9090
│   └── stats-service/    [metier]    :8085  (GraphQL)
├── proto/reco.proto      contrat gRPC
├── frontend/             Angular
├── scripts/              prepare_catalogue.py (dataset Coursera -> catalogue.csv)
├── docs/architecture.md  schema d'architecture
├── docker/init.sql       creation des bases
└── docker-compose.yml    PostgreSQL
```

## Prerequis

JDK 21, Maven 3.9+, Node.js LTS, Docker (ou PostgreSQL local), Git.
Versions utilisees : Spring Boot 4.0.9, Spring Cloud 2025.1.4, Angular 22.

## Demarrage (jour 1)

```bash
docker compose up -d                       # PostgreSQL + 3 bases

# Dans cet ordre, un terminal par service (ou IntelliJ) :
cd services/config-server    && mvn spring-boot:run
cd services/discovery-server && mvn spring-boot:run
cd services/api-gateway      && mvn spring-boot:run
# puis, optionnel pour tester l'enregistrement Eureka :
cd services/auth-service     && mvn spring-boot:run

# Frontend
cd frontend && npm install && npm start    # http://localhost:4200
```

Verifications :

- http://localhost:8888/api-gateway/default : doit afficher les routes de la gateway
- http://localhost:8761 : les services demarres sont listes dans Eureka
- http://localhost:4200 : page "Pour toi" (donnees fictives pour l'instant)

Le `config-server` lit `config-repo/` via le chemin relatif `../../config-repo/`
(le dossier de travail est celui du module). Sinon : `CONFIG_REPO_PATH=file:/chemin/absolu/config-repo/`.

## Etat d'avancement

| Element | Etat |
|---|---|
| config-server, discovery-server, api-gateway | squelette pret |
| auth, catalogue, activity, reco, stats | squelettes qui demarrent et s'enregistrent ; logique a ecrire |
| Angular | theme, shell, composants partages, page "Pour toi" (donnees fictives) |
| `proto/reco.proto` | contrat defini ; dependances gRPC a ajouter au jour 5 |
| JPA, Security/JWT, OpenFeign, GraphQL, gRPC | a ajouter service par service |

## Donnees

```bash
pip install pandas
python scripts/prepare_catalogue.py chemin/vers/coursera.csv --n 100
```

Ne versionne pas le dataset complet : verifie sa licence Kaggle, garde un echantillon et cite la source ici.

## Identite visuelle

Une seule famille turquoise (`#19CBD8`, `#0B7F8C`, `#D8F6F8`), marine `#0B2C45` pour le texte,
jaune uniquement pour les etoiles, rouge uniquement pour "ne me convient pas".
Variables dans `frontend/src/styles.scss`.
