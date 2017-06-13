Optimizely Full Stack Java SDK Reference Implementation

----------------------
Building
----------------------
This reference implementation is built using maven.  To build execute:
mvn package 
from the root folder.

----------------------
Executing
----------------------
The main file is specified by the manifest so to run execute:

java -jar target/optimizely-1.0-SNAPSHOT-jar-with-dependencies.jar <datafile location>

Where <datafile location> can be an local reference or pointing to Optimizely CDN.

Local Example:
/Users/<User>/workspace/Optimizely/datafile.json
java -jar target/optimizely-1.0-SNAPSHOT-jar-with-dependencies.jar /Users/<User>/workspace/Optimizely/datafile.json

CDN Example:
https://cdn.optimizely.com/json/8410977336.json
java -jar target/optimizely-1.0-SNAPSHOT-jar-with-dependencies.jar https://cdn.optimizely.com/json/8410977336.json