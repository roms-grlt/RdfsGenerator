# Consigne du projet RDF

Bases de Donnees Specialisees
Projet : Knowledge graphs et raisonnement
Le projet est a faire en binome. Vous soumettrez une archive ou un lien vers vos fichiers
sources (e.g. donnees RDF/S, code java, requetes SPARQL, y compris ceux necessaires a
l’extraction / generation des donnees), ainsi qu’un rapport en anglais ou francais au format
pdf, decrivant votre travail. La date limite pour soumettre ces documents sur Moodle est le
5 janvier 2026.
1. Jeux de donnees relies
Recuperez plusieurs jeux de donnees (en acces libre) qui sont susceptibles d’avoir des res-
sources en commun. Par exemple des entrees sur les pays et les villes du monde existent en
Dbpedia, en Wikidata, ainsi que dans des jeux de donnees geographiques comme Geonames
https://www.geonames.org.
Plusieurs jeux de donnees RDF sur des sujets varies sont rendus disponibles par le W3C
https://www.w3.org/wiki/DataSetRDFDumps. D’autres jeux de donnees, majoritairement
en format CSV ou Json sont disponibles egalement sur Kaggle https://www.kaggle.com/
datasets et sur Datahub https://datahub.io/. Vous pouvez egalement utiliser le moteur
de recherche pour datasets de Google https://datasetsearch.research.google.com/ en
se limitant aux jeux de donnees libres d’acces.
1.1. Extraction des donnees
Generalement il existe plusieurs facons d’extraire ces donnees
telechargement de fichiers csv,
dump RDF
interrogation de SPARQL endpoints ou autres services Web (par interface Web ou par
du logiciel)
librairies clientes (pour python, java etc.) qui facilitent l’extraction automatique (cf. par
exemple https://www.geonames.org/export/client-libraries.html pour GeoNa-
mes).
Vous pouvez egalement recuperer des donnees directement sur le web (par exemple en utili-
sant les librairies de la Python Data Science Stack).
Veillez a ce que les differents jeux de donnees aient des concepts en commun. Par exemple la
ville de Paris (comme n’importe quelle autre ville) est representee aussi bien en GeoNames par
l’URI https://sws.geonames.org/2988507/, qu’en Dbpedia https://dbpedia.org/resource/Paris,
qu’en Wikidata par http://www.wikidiata.org/entity/Q90.
2. Integration des donnees
Quel que soit le format d’origine des donnees vous les convertirez tous en RDF. Pour cela
vous pouvez vous inspirer des techniques apprises dans le TP2b, ou bien vous pouvez ecrire
votre propre programme de transformation, dans le langage de votre choix. Attention a bien
choisir la facon de modeliser les donnees en RDF. Dans le TP2b vous trouverez un exemple
de restructuration de donnees relationnelles en RDF.
Vous obtiendrez un unique jeu de donnees integrees en ajoutant des relations entres les
donnees (comme par exemple des faits owl:sameAs, comme dans le TP2b, ou des triplets
additionnelles de votre choix pour creer des liens (meme fictives) entre les donnees.
Au besoin n’oubliez pas que vous pouvez vous servir de jena pour lire, manipuler, et ecrire du
RDF, cf. la documentation Jena et en particulier https://jena.apache.org/tutorials/
rdf_api.html#ch-Reading%20RDF.
Dans le rapport decrivez bien tout le processus d’importation et de transformation des
donnees.
3. Requetes
Vous proposerez une dizaine de requetes SPARQL interrogeant votre base RDF. Les requetes
doivent le plus possible integrer des informations provenant des differents jeux de donnees
que vous avez combines (par exemple, une meme requete pourrait extraire les coordonnees
geographiques de Paris depuis GeoNames ainsi que l’histoire de l’architecture de la ville
depuis DBPedia). Les aspects suivants sont requis :
une requete federee interrogeant des sources externes a votre jeux de donnees;
une requete avec OPTIONAL;
une requete sur des graphes nommes;
une requete avec agregation;
une requete utilisant des expressions de chemin;
une requete MINUS ou FILTER NOT EXISTS.
4. Raisonnement sur les donnees RDF
Dans cette section, vous mettrez en œuvre des mecanismes de raisonnement bases sur RDFS.
Le but est de structurer vos donnees RDF en definissant un schema clair et bien organise,
puis de realiser des inferences simples pour enrichir les donnees. Ces inferences permettront
de decouvrir de nouvelles informations a partir des relations et classes deja definies dans vos
jeux de donnees integres.
4.1. Construction d’une ontologie RDFS
Definissez une ontologie RDFS pour decrire les relations entre les classes et les proprietes au
sein de votre modele RDF. Cela servira de base pour le raisonnement.
Suivez les etapes suivantes :
1. Si vos jeux de donnees possedent deja un schema (des URIs de classes, de proprietes et
des relations RDFS entre celles-ci), vous pouvez les reutiliser. Sinon identifiez les entites
principales de vos jeux de donnees et organisez-les en classes; identifiees egalement les
proprietes (predicats) utilisees.
Par exemple si un de vos jeux de donnees n’a pas de schema et possede les triplets
:london :capitalOf :UK
:paris :capitalOf :FR
:paris :surface 105.4
Vous pouvez definir un schema RDFS qui contient entre autre des proprietes :capitalOf,
:surface, etc., ansi que les classes :City, :Country, etc.
2. Definissez ensuite votre ontologie RDFS pour relier ces classes et proprietes. Les elements
suivants doivent etre pris en compte :
Classes et hierarchie de classes : Utilisez la contrainte rdfs:subClassOf pour
creer une hierarchie entre les classes. Par exemple, vous pourriez avoir une classe
:City qui est une sous-classe de :Location.
Proprietes et hierarchie de proprietes : Utilisez rdfs:subPropertyOf pour
structurer les relations entre proprietes. Par exemple, si vous avez une propriete
capitalOf pour indiquer qu’une ville est la capitale d’un pays, vous pourriez
creer une super-propriete locatedIn pour preciser que cette ville se trouve dans
ce pays.
Domaines et co-domaines : Assurez-vous de definir les rdfs:domain et rdfs:range
pour chaque propriete. Par exemple, pour la propriete locatedIn, le domaine
pourrait etre City et le co-domaine Country.
3. Definissez des contraintes comme au point precedent mais entre classes et proprietes
de differents jeux de donnees, par exemple
dbp:region rdfs:subPropertyOf :LocatedIn
geo:inCountry rdfs:subPropertyOf :LocatedIn
permet d’unifier deux proprietes, une du vocabulaire Dbpedia et une du vocabulaire
GeoNames en une unique propriete. De la meme facon il est possible d’unifier des
classes de differents jeux de donnees.
4. Ajoutez ensuite les faits de base pour peupler les nouvelles classes et proprietes, si vous
en avez (par exemple :london rdf:type :City, :FR rdf:type Country, etc.). Notez
que des ressources de differents jeux de donnees peuvent etre declarees appartenante a
la meme classe, ce qui facilite leur integration.
4.2. Raisonnement avec RDFS
Une fois le schema defini, le raisonnement peut commencer. Vous pouvez utiliser les modeles
RDF preconfigures de Jena qui appliquent automatiquement les regles de raisonnement
RDFS aux donnees RDF. Voici les etapes de base a suivre :
Importez vos donnees RDF dans un modele Jena.
Appliquez un modele de raisonnement RDFS a vos donnees.
Executez des requetes SPARQL sur le modele enrichi pour decouvrir les informations
deduites a partir des regles RDFS.
Referez-vous au TP3b et a la documentation Jena indiquee dans ce TP pour les details de
mise en oeuvre de ce point.

A l’aide des requetes SPARQL, presentez les resultats que vous obtenez apres avoir applique
les regles de raisonnement RDFS. Comparez les donnees brutes a celles qui ont ete enrichies
grace aux inferences. Par exemple, montrez comment certaines entites sont automatiquement
classees dans des categories superieures grace a rdfs:subClassOf, ou comment des relations
sont etendues grace a rdfs:subPropertyOf. Vous devez concevoir des requetes interrogeant
plusieurs jeux de donnees en meme temps, et combinant des informations enrichies par le
raisonnement.
5. Conclusion
Concluez en expliquant comment ces requetes SPARQL, combinees au raisonnement RDFS,
permettent d’exploiter efficacement vos jeux de donnees integres. Mentionnez egalement les
limites potentielles du raisonnement RDFS et discutez des eventuelles ameliorations possibles
pour votre projet, comme l’extension a des schemas plus riches ou l’integration de nouveaux
jeux de donnees.
N’oubliez pas de citer les sources academiques, documentations et tutoriels que vous avez
utilises pour realiser ce projet.