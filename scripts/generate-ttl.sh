#!/bin/bash

# Script de génération des fichiers TTL à partir des CSV

cd ../data/csv

echo "Génération du fichier TTL IMDB"
java -jar ../../target/rdfs-generator.jar csv ./convert-imdb/imdb.csv ./convert-imdb/ImdbFilm.java ../ttl/imbd-csv.ttl imdb http://example.org/imdb/

echo "Génération du fichier TTL Amazon"
java -jar ../../target/rdfs-generator.jar csv ./convert-amazon/amazon.csv ./convert-amazon/AmazonFilm.java ../ttl/amazon-csv.ttl amazon http://example.org/amazon/

echo "Génération du fichier TTL Netflix"
java -jar ../../target/rdfs-generator.jar csv ./convert-netflix/netflix.csv ./convert-netflix/NetflixFilm.java ../ttl/netflix-csv.ttl netflix http://example.org/netflix/

echo "Intégration des fichiers TTL"
cd ../ttl
java -jar ../../target/rdfs-generator.jar integrate ../integrated.ttl 3 imdb imbd-csv.ttl amazon amazon-csv.ttl netflix netflix-csv.ttl title film NetflixFilm AmazonFilm ImdbFilm

echo "Génération terminée"
