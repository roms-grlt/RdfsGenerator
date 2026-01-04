#!/bin/bash

# Script to regenerate TTL files and perform integration

echo "=== Regenerating TTL files with corrected schema ==="

# Regenerate Amazon TTL
echo "Generating Amazon TTL..."
java -jar target/rdfs-generator.jar csv \
    result/convert-amazon/amazon.csv \
    result/convert-amazon/AmazonFilm.java \
    result/amazon-csv.ttl \
    amazon \
    "http://www.amazon.org/"

# Regenerate Netflix TTL
echo "Generating Netflix TTL..."
java -jar target/rdfs-generator.jar csv \
    result/convert-netflix/netflix.csv \
    result/convert-netflix/NetflixFilm.java \
    result/netflix-csv.ttl \
    netflix \
    "http://www.netflix.org/"

# Regenerate IMDB TTL
echo "Generating IMDB TTL..."
java -jar target/rdfs-generator.jar csv \
    result/convert-imdb/imdb.csv \
    result/convert-imdb/ImdbFilm.java \
    result/imbd-csv.ttl \
    imdb \
    "http://www.imdb.org/"

echo ""
echo "=== Starting Integration ==="

# Integrate datasets
java -jar target/rdfs-generator.jar integrate \
    result/integrated-films.ttl \
    amazon result/amazon-csv.ttl \
    netflix result/netflix-csv.ttl \
    imdb result/imbd-csv.ttl

echo ""
echo "=== Integration Complete ==="
echo "Output file: result/integrated-films.ttl"
