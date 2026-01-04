java -jar rdfs-generator.jar csv ./convert-imdb/imdb.csv ./convert-imdb/ImdbFilm.java imbd-csv.ttl imdb http://www.imdb.org/
java -jar rdfs-generator.jar csv ./convert-amazon/amazon.csv ./convert-amazon/AmazonFilm.java amazon-csv.ttl amazon http://www.amazon.org/
java -jar rdfs-generator.jar csv ./convert-netflix/netflix.csv ./convert-netflix/NetflixFilm.java netflix-csv.ttl netflix http://www.netflix.org/

pause