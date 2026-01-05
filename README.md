# RdfsGenerator

Projet d'intégration multi-sources de données de films (Amazon, Netflix, IMDB) avec raisonnement RDFS.

## Structure

```
├── queries/           # Requêtes SPARQL
├── scripts/           # Scripts d'exécution
├── result/            # Fichiers générés (.ttl, .jar)
└── src/              # Code source Java
```

## Utilisation

### Générer les fichiers TTL
```bash
cd scripts
./generate-ttl.sh
```

### Exécuter les requêtes SPARQL
```bash
cd scripts
./run-queries.sh
```

## Requêtes disponibles

- `aggregation-request.sparql` - Statistiques par genre avec raisonnement RDFS
- `optional-request.sparql` - Intégration multi-sources avec données optionnelles
- `minus-request.sparql` - Exclusion de films Netflix et contenu court
- `complex-request.sparql` - Combinaison des 3 sources
- `path-request.sparql` - Démonstration du raisonnement RDFS
- `sources-request.sparql` - Analyse cross-platform
- `federated-request.sparql` - Requête fédérée avec DBpedia
- `not-exists-request.sparql` - FILTER NOT EXISTS

## Build

```bash
mvn clean package
```