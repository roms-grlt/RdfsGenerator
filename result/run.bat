java -jar rdfs-generator.jar csv ./convert-csv/imdb.csv ./convert-csv/ImdbFilmCsvModel.java imbd-csv.ttl
java -jar rdfs-generator.jar request https://dbpedia.org/sparql ./import-dbpedia/request.txt dbpedia.ttl
java -jar rdfs-generator.jar convert N-TRIPLE ./import-linkedmdb/linkedmdb-clean.nt linkedmdb.ttl ./import-linkedmdb/prefixes.txt
pause