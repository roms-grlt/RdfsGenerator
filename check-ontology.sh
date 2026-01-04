#!/bin/bash

# Script to check the integrated ontology structure

echo "=== Unified Ontology Structure ==="
echo ""

echo "1. Unified Film Class:"
grep -A 3 'unified:Film  a' result/integrated-films.ttl | head -5
echo ""

echo "2. Class Hierarchy (subClassOf):"
grep -B 1 'subClassOf.*unified:Film' result/integrated-films.ttl | grep -v '^--$' | head -10
echo ""

echo "3. Property Hierarchy - Title mappings:"
grep -B 1 'subPropertyOf.*unified:title' result/integrated-films.ttl | grep -v '^--$'
echo ""

echo "4. Property Hierarchy - Rating mappings:"
grep -B 1 'subPropertyOf.*unified:rating' result/integrated-films.ttl | grep -v '^--$'
echo ""

echo "5. Property Hierarchy - Year mappings:"
grep -B 1 'subPropertyOf.*unified:releaseYear' result/integrated-films.ttl | grep -v '^--$'
echo ""

echo "6. owl:sameAs relationships:"
grep 'owl:sameAs' result/integrated-films.ttl | wc -l | xargs echo "Count:"
grep 'owl:sameAs' result/integrated-films.ttl | head -5
echo ""

echo "7. Sample Films from each dataset:"
echo "Amazon:"
grep -A 3 'amazon:1 ' result/integrated-films.ttl | head -5
echo ""
echo "Netflix:"
grep -A 3 'netflix:1 ' result/integrated-films.ttl | head -5
echo ""
echo "IMDB:"
grep -A 3 'imdb:1 ' result/integrated-films.ttl | head -5
