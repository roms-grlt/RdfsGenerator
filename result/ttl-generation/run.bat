java -jar ../rdfs-generator.jar csv ./convert-imdb/imdb.csv ./convert-imdb/ImdbFilm.java imbd-csv.ttl imdb http://example.org/imdb/
java -jar ../rdfs-generator.jar csv ./convert-amazon/amazon.csv ./convert-amazon/AmazonFilm.java amazon-csv.ttl amazon http://example.org/amazon/
java -jar ../rdfs-generator.jar csv ./convert-netflix/netflix.csv ./convert-netflix/NetflixFilm.java netflix-csv.ttl netflix http://example.org/netflix/
java -jar ../rdfs-generator.jar integrate ../integrated.ttl 3 imdb imbd-csv.ttl amazon amazon-csv.ttl netflix netflix-csv.ttl title film NetflixFilm AmazonFilm ImdbFilm

pause