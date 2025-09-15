# Phone surveillance system

This system allows for multiple phones to connect to a server which is bundled in this repository. 
All of the picture streams coming from the cameras converge into the Java server; the Java server manages the sent pictures with a MySQL database.
Once a certain pictures-per-video parameter is triggered (a prefixed number of frames initially comunicated by the camera), a 24fps video is generated, and the original pictures are deleted from the storage.
The system then allows access to the cameras using a web server (for example Apache+PHP) which communicates with the database (to give access to the recorded videos) and the Java server (to provide a live-camera feed).