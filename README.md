# RdfsGenerator

Projet d'intégration multi-sources de données de films (Amazon, Netflix, IMDB) avec raisonnement RDFS.

## Prérequis
- Java 11+
- Maven

## Récupération des données

Les données proviennent de trois sources Kaggle :

### 1. Netflix Original Films
- **Source** : https://www.kaggle.com/datasets/luiscorter/netflix-original-films-imdb-scores
- **Fichier** : Télécharger `netflix_originals.csv`
- **Destination** : `data/csv/convert-netflix/netflix.csv`

### 2. Amazon Movies and Films
- **Source** : https://www.kaggle.com/datasets/muhammadawaistayyab/amazon-movies-and-films
- **Fichier** : Télécharger `amazon_movies.csv`
- **Destination** : `data/csv/convert-amazon/amazon.csv`

### 3. IMDB Most Popular Films
- **Source** : https://www.kaggle.com/datasets/mazenramadan/imdb-most-popular-films-and-series
- **Fichier** : Télécharger le CSV
- **IMPORTANT** : Lors du téléchargement, **bien sélectionner les 14 colonnes** :
  - name, date, rate, votes, genre, duration, type, certificate, episodes, nudity, violence, profanity, alcohol, frightening
- **Destination** : `data/csv/convert-imdb/imdb.csv`

Une fois les fichiers CSV téléchargés et placés dans les répertoires appropriés, vous pouvez procéder à la compilation.

## Compilation
```bash
mvn clean package
```

## Structure

```
├── data/               # Données et ontologie
│   ├── csv/            # CSV sources et classes de conversion
│   ├── ttl/            # Fichiers TTL générés par source
│   ├── integrated.ttl  # TTL intégré (auto-généré)
│   ├── ontology.ttl    # Ontologie RDFS de base
│   ├── to_add_ontology.ttl  # Ontologie enrichie à merger
│   └── final_file.ttl  # Fichier final avec ontologie enrichie
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

#### Intégrer plusieurs fichiers TTL
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

#### Fusionner avec l'ontologie enrichie
```bash
java -jar target/rdfs-generator.jar merge <input_file> <ontology_file> <output_file>

# Exemple
java -jar target/rdfs-generator.jar merge \
  data/integrated.ttl \
  data/to_add_ontology.ttl \
  data/final_file.ttl
```

#### Exécuter une requête SPARQL
```bash
java -jar target/rdfs-generator.jar query <query_file> <ttl_file>

# Exemple
java -jar target/rdfs-generator.jar query \
  queries/aggregation-request.sparql \
  data/integrated.ttl
```

## Ontologie enrichie

Le fichier `to_add_ontology.ttl` contient l'ontologie RDFS enrichie avec :

### Propriétés unifiées
- `unified:title`, `unified:rating`, `unified:genre` - Propriétés de base
- `unified:duration` - Unifie `netflix:runtime` et `imdb:duration`
- `unified:releaseDate` - Unifie `amazon:releaseYear`, `imdb:date`, `netflix:premiere`
- `unified:contentRating` - Unifie `amazon:mpaaRating` et `imdb:certificate`
- `unified:contentAdvisory` - Super-propriété pour les avertissements IMDB (violence, nudity, profanity, alcohol, frightening)

### Hiérarchie de classes
- `unified:film` - Classe parente
- `amazon:AmazonFilm`, `netflix:NetflixFilm`, `imdb:ImdbFilm` - Sous-classes

Le raisonnement RDFS permet d'interroger les données via les propriétés unifiées en utilisant `rdfs:subPropertyOf*` dans les requêtes SPARQL.

## Requêtes disponibles

- `aggregation-request.sparql` - Statistiques par genre avec raisonnement RDFS
- `optional-request.sparql` - Intégration multi-sources avec données optionnelles
- `minus-request.sparql` - Exclusion de films Netflix et contenu court
- `not-exists-request.sparql` - FILTER NOT EXISTS
- `path-request.sparql` - Expressions de chemin RDFS (subPropertyOf*)
- `class-hierarchy-request.sparql` - Hiérarchie de classes RDFS (subClassOf)
- `enriched-ontology-request.sparql` - Démonstration des propriétés unifiées enrichies
- `complex-request.sparql` - Combinaison des 3 sources
- `federated-request.sparql` - Requête fédérée avec DBpedia