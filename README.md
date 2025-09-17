Phone Surveillance System

This project provides a complete surveillance platform where multiple phones act as cameras and stream their images to a central server.

The bundled Java server receives the incoming image streams and manages them using a MySQL database. Once a predefined pictures-per-video threshold is reached (a set number of frames communicated by each camera), the server automatically compiles the frames into a 24 fps video and removes the original images from storage to save space.

A web server (e.g. Apache + PHP) can be used alongside the Java server to provide two main features:

- Recorded video access — The web server interacts with the MySQL database to serve previously recorded videos.

- Live camera feeds — The web server communicates with the Java server to deliver real-time camera streams.

This architecture makes it easy to integrate multiple phones, handle large amounts of image data, and give users secure access to both live and recorded footage.