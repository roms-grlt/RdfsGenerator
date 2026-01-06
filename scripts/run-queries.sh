#!/bin/bash

OUTPUT_FILE="../result/resultats_requetes.txt"

echo "==================================="
echo "Exécution des Requêtes SPARQL"
echo "==================================="
echo ""

> "$OUTPUT_FILE"

{
    echo "=========================================="
    echo "   RÉSULTATS DES REQUÊTES SPARQL"
    echo "=========================================="
    echo ""
} >> "$OUTPUT_FILE"

# Liste des requêtes
queries=(
    "aggregation-request:Agrégation (GROUP BY, HAVING)"
    "optional-request:Données optionnelles (OPTIONAL)"
    "minus-request:Exclusion (MINUS)"
    "not-exists-request:Filter NOT EXISTS"
    "path-request:Expressions de chemin"
    "class-hierarchy-request:Hiérarchie de classes"
    "complex-request:Requête complexe"
    "federated-request:Requête fédérée (SERVICE)"
)

for query in "${queries[@]}"; do
    IFS=':' read -r file desc <<< "$query"
    
    echo "Exécution: $desc"
    
    {
        echo ""
        echo "=========================================="
        echo "REQUÊTE: $desc"
        echo "Fichier: ../queries/${file}.sparql"
        echo "=========================================="
        echo ""

        # Affiche la requête
        echo "--- Code SPARQL ---"
        cat "../queries/${file}.sparql"
        echo ""
        echo "--- Résultats ---"

        # Exécute et capture les résultats
        java -jar ../target/rdfs-generator.jar query "../queries/${file}.sparql" ../data/integrated.ttl 2>&1
        
        echo ""
        echo "=========================================="
        echo ""
    } >> "$OUTPUT_FILE"
done

echo ""
echo "Résultats sauvegardés dans: $OUTPUT_FILE"