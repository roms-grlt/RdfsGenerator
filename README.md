# RdfsGenerator

Projet d'intégration multi-sources de données de films (Amazon, Netflix, IMDB) avec raisonnement RDFS.

## Prérequis
- Java 11+
- Maven

## Compilation
```bash
mvn clean package
```

## Structure

```
├── data/               # Données et ontologie
│   ├── csv/            # CSV sources et classes de conversion
│   ├── ttl/            # Fichiers TTL générés par source
│   ├── integrated.ttl  # TTL intégré final
│   └── ontology.ttl    # Ontologie RDFS
├── queries/            # Requêtes SPARQL
├── scripts/            # Scripts d'exécution
├── result/             # Résultats des requêtes
├── target/             # JAR compilé
│   └── rdfs-generator.jar
└── src/                # Code source Java
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

### Commandes manuelles

#### Générer un fichier TTL depuis un CSV
```bash
java -jar target/rdfs-generator.jar csv <csv_file> <class_file> <output_ttl> <prefix> <namespace>

# Exemple pour IMDB
java -jar target/rdfs-generator.jar csv \
  data/csv/convert-imdb/imdb.csv \
  data/csv/convert-imdb/ImdbFilm.java \
  data/ttl/imbd-csv.ttl \
  imdb \
  http://example.org/imdb/
```

#### Intégrer plusieurs fichiers TTL (prend un peu de temps à cause du raisonnement RDFS)
```bash
java -jar target/rdfs-generator.jar integrate <output_file> <nb_sources> \
  <prefix1> <file1> <prefix2> <file2> ... \
  <property> <class_suffix> <class1> <class2> ...

# Exemple
java -jar target/rdfs-generator.jar integrate data/integrated.ttl 3 \
  imdb data/ttl/imbd-csv.ttl \
  amazon data/ttl/amazon-csv.ttl \
  netflix data/ttl/netflix-csv.ttl \
  title film NetflixFilm AmazonFilm ImdbFilm
```

#### Exécuter une requête SPARQL
```bash
java -jar target/rdfs-generator.jar query <query_file> <ttl_file>

# Exemple
java -jar target/rdfs-generator.jar query \
  queries/aggregation-request.sparql \
  data/integrated.ttl
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