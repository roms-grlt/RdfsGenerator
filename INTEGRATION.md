# Documentation de l'Intégration de Données RDF

## Vue d'ensemble

Ce document décrit l'implémentation de la partie **intégration de données** du projet RDF, conformément aux consignes du projet (section 2 du CLAUDE.md).

## Objectif

L'intégration de données vise à :
1. **Combiner plusieurs jeux de données RDF** (Amazon, Netflix, IMDB)
2. **Créer des relations entre les ressources** via `owl:sameAs` pour identifier les films identiques
3. **Définir une ontologie unifiée** qui harmonise les classes et propriétés des différents datasets

## Architecture

### Classe principale : `DataIntegrator.java`

Localisation : `src/main/java/org/example/service/DataIntegrator.java`

Cette classe implémente les fonctionnalités suivantes :

#### 1. Détection de Films Similaires

La méthode `areFilmsSimilar()` compare deux films en utilisant :
- **Extraction de titre** : récupère les propriétés `title` ou `name` selon le dataset
- **Normalisation** : convertit en minuscules, supprime la ponctuation
- **Distance de Levenshtein** : calcule la similarité entre les titres
- **Seuil de similarité** : 85% de correspondance pour considérer deux films identiques

```java
// Exemple de normalisation
"The Lord of the Rings: The Fellowship"
→ "the lord of the rings the fellowship"
```

#### 2. Création de Relations `owl:sameAs`

Pour chaque paire de datasets (Amazon-Netflix, Amazon-IMDB, Netflix-IMDB), le système :
1. Extrait tous les films de chaque dataset
2. Compare chaque film du premier dataset avec ceux du second
3. Ajoute une relation `owl:sameAs` si les titres sont similaires

Exemple de triple généré :
```turtle
amazon:42 owl:sameAs netflix:17 .
```

#### 3. Ontologie Unifiée

L'ontologie unifiée crée une hiérarchie de classes et propriétés :

**Classes :**
```turtle
unified:Film a rdfs:Class ;
    rdfs:label "Film" ;
    rdfs:comment "Unified film class integrating Amazon, Netflix, and IMDB data" .

amazon:AmazonFilm rdfs:subClassOf unified:Film .
netflix:NetflixFilm rdfs:subClassOf unified:Film .
imdb:ImdbFilm rdfs:subClassOf unified:Film .
```

**Propriétés unifiées :**
- `unified:title` : titre du film
- `unified:rating` : note/score
- `unified:releaseYear` : année de sortie
- `unified:genre` : genre(s) du film
- `unified:runtime` : durée en minutes

**Mappings des propriétés :**
```turtle
# Mapping des titres
amazon:title rdfs:subPropertyOf unified:title .
netflix:title rdfs:subPropertyOf unified:title .
imdb:name rdfs:subPropertyOf unified:title .

# Mapping des notes
amazon:rating rdfs:subPropertyOf unified:rating .
netflix:imdbScore rdfs:subPropertyOf unified:rating .
imdb:rate rdfs:subPropertyOf unified:rating .

# Mapping des années
amazon:releaseYear rdfs:subPropertyOf unified:releaseYear .
imdb:date rdfs:subPropertyOf unified:releaseYear .

# Mapping des genres
netflix:genre rdfs:subPropertyOf unified:genre .
imdb:genre rdfs:subPropertyOf unified:genre .

# Mapping des durées
netflix:runtime rdfs:subPropertyOf unified:runtime .
imdb:duration rdfs:subPropertyOf unified:runtime .
```

#### 4. Relations Cross-Dataset Additionnelles

Le système crée également des propriétés personnalisées pour faciliter les requêtes :
- `unified:hasSimilarRating` : relie des films avec des notes similaires
- `unified:sharesSameGenre` : relie des films du même genre

## Utilisation

### Commande d'intégration

```bash
java -jar target/rdfs-generator.jar integrate <output_file> \
    <dataset1_name> <dataset1_file> \
    <dataset2_name> <dataset2_file> \
    <dataset3_name> <dataset3_file> \
    [--no-ontology]
```

### Exemple avec les trois datasets

```bash
java -jar target/rdfs-generator.jar integrate \
    result/integrated-films.ttl \
    amazon result/amazon-csv.ttl \
    netflix result/netflix-csv.ttl \
    imdb result/imbd-csv.ttl
```

### Script de régénération complète

Un script `regenerate-and-integrate.sh` est fourni pour :
1. Régénérer les fichiers TTL depuis les CSV
2. Lancer l'intégration automatiquement

```bash
chmod +x regenerate-and-integrate.sh
./regenerate-and-integrate.sh
```

## Résultats de l'Intégration

### Statistiques

Après l'exécution, le système affiche :
```
=== Integration Statistics ===
amazon dataset: 25764 triples
imdb dataset: 93241 triples
netflix dataset: 4107 triples
Combined model: 123154 triples
owl:sameAs relationships: <nombre>
Total classes: 1
Total properties: 7
```

### Structure du fichier de sortie

Le fichier `result/integrated-films.ttl` contient :

1. **Préfixes** :
```turtle
@prefix amazon:  <http://www.amazon.org/> .
@prefix imdb:    <http://www.imdb.org/> .
@prefix netflix: <http://www.netflix.org/> .
@prefix owl:     <http://www.w3.org/2002/07/owl#> .
@prefix rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns> .
@prefix rdfs:    <http://www.w3.org/2000/01/rdf-schema> .
@prefix unified: <http://www.filmdata.org/> .
```

2. **Ontologie unifiée** : classes et propriétés avec leurs hiérarchies

3. **Données des trois datasets** : tous les triplets originaux

4. **Relations owl:sameAs** : liens entre films identiques

5. **Propriétés de relations cross-dataset**

## Corrections Techniques Apportées

### 1. Correction du TtlWriter (ligne 83)
**Problème** : Le domaine des propriétés n'incluait pas le préfixe
```java
// Avant
writer.write(String.format("rdfs:domain %s ;\n\t", className));

// Après
writer.write(String.format("rdfs:domain %s:%s ;\n\t", prefix, className));
```

### 2. Échappement des caractères spéciaux (ligne 61-79)
**Problème** : Les guillemets dans les données cassaient la syntaxe Turtle

```java
String escapedValue = value.toString()
    .replace("\\", "\\\\")  // Escape backslashes first
    .replace("\"", "\\\"")  // Escape double quotes
    .replace("\n", "\\n")   // Escape newlines
    .replace("\r", "\\r")   // Escape carriage returns
    .replace("\t", "\\t");  // Escape tabs
```

## Avantages de cette Approche

1. **Flexibilité** : Fonctionne avec n'importe quel nombre de datasets
2. **Extensibilité** : Facile d'ajouter de nouvelles propriétés unifiées
3. **Tolérance aux variations** : La distance de Levenshtein gère les petites différences
4. **Conformité RDFS** : Utilise les mécanismes standards (`rdfs:subClassOf`, `rdfs:subPropertyOf`)
5. **Performance** : Algorithme O(n²) acceptable pour des datasets de taille moyenne

## Limitations et Améliorations Possibles

### Limitations actuelles :
- La comparaison de titres ne détecte pas les titres dans différentes langues
- Le seuil de 85% est fixe (pourrait être paramétrable)
- Pas de désambiguïsation pour les homonymes (films différents avec le même titre)

### Améliorations futures :
1. **Matching multi-critères** : utiliser année + titre + durée pour améliorer la précision
2. **Normalisation avancée** : gérer les articles ("The", "A", "Le", "La")
3. **Détection de traductions** : API de traduction pour comparer titres internationaux
4. **Relations sémantiques** : ajouter des liens basés sur réalisateurs, acteurs communs
5. **Liens vers DBpedia/Wikidata** : enrichir avec des sources externes

## Exemples de Requêtes SPARQL

### 1. Trouver les films présents dans plusieurs datasets

```sparql
PREFIX owl: <http://www.w3.org/2002/07/owl#>

SELECT ?film1 ?film2
WHERE {
    ?film1 owl:sameAs ?film2 .
}
```

### 2. Utiliser l'ontologie unifiée pour interroger tous les datasets

```sparql
PREFIX unified: <http://www.filmdata.org/>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema>

SELECT ?film ?title ?rating
WHERE {
    ?film a ?filmClass .
    ?filmClass rdfs:subClassOf unified:Film .

    ?film ?titleProp ?title .
    ?titleProp rdfs:subPropertyOf unified:title .

    OPTIONAL {
        ?film ?ratingProp ?rating .
        ?ratingProp rdfs:subPropertyOf unified:rating .
    }
}
```

### 3. Comparer les notes d'un même film sur différentes plateformes

```sparql
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX amazon: <http://www.amazon.org/>
PREFIX imdb: <http://www.imdb.org/>
PREFIX netflix: <http://www.netflix.org/>

SELECT ?title ?amazonRating ?imdbRating ?netflixRating
WHERE {
    ?amazonFilm a amazon:AmazonFilm ;
                amazon:title ?title ;
                amazon:rating ?amazonRating .

    OPTIONAL {
        ?amazonFilm owl:sameAs ?imdbFilm .
        ?imdbFilm imdb:rate ?imdbRating .
    }

    OPTIONAL {
        ?amazonFilm owl:sameAs ?netflixFilm .
        ?netflixFilm netflix:imdbScore ?netflixRating .
    }
}
```

## Conformité avec les Consignes

Cette implémentation répond aux exigences de la section 2 du projet :

✅ **Conversion en RDF** : Tous les datasets sont en format Turtle
✅ **Jeu de données unique intégré** : Fichier `integrated-films.ttl`
✅ **Relations owl:sameAs** : Implémentées via matching de titres
✅ **Triplets additionnels** : Propriétés `hasSimilarRating` et `sharesSameGenre`
✅ **Utilisation de Jena** : API Jena pour lire, manipuler et écrire RDF
✅ **Documentation** : Ce fichier décrit tout le processus

## Fichiers Modifiés/Créés

1. **Nouveaux fichiers** :
   - `src/main/java/org/example/service/DataIntegrator.java` : classe principale
   - `regenerate-and-integrate.sh` : script d'automatisation
   - `INTEGRATION.md` : cette documentation

2. **Fichiers modifiés** :
   - `src/main/java/org/example/Main.java` : ajout de la commande `integrate`
   - `src/main/java/org/example/service/TtlWriter.java` : corrections syntaxe Turtle

3. **Fichiers générés** :
   - `result/integrated-films.ttl` : dataset intégré final
