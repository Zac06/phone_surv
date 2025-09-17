# Phone Surveillance System

This project provides a complete surveillance platform where multiple phones act as cameras and stream their images to a central server.

The bundled Java server receives the incoming image streams and manages them using a MySQL database. Once a predefined pictures-per-video threshold is reached (a set number of frames communicated by each camera), the server automatically compiles the frames into a 24 fps video and removes the original images from storage to save space.

A web server (e.g. Apache + PHP) can be used alongside the Java server to provide two main features:

- Recorded video access — The web server interacts with the MySQL database to serve previously recorded videos.

- Live camera feeds — The web server communicates with the Java server to deliver real-time camera streams.

This architecture makes it easy to integrate multiple phones, handle large amounts of image data, and give users secure access to both live and recorded footage.

---

## Compiling

### Java server

From the project's root:

```bash
cd camera-source/server
mvn clean package
```

### Android application

Requirements: Flutter SDK

- Connect your Android device (with USB debugging enabled)
- From the project's root:

```bash
cd camera-source/client/android/phone_surveillance_android
flutter pub get
flutter run --release

```

The app should now be installed on your device. If not, make sure that `Developer Options -> Allow USB installation` is checked

---

## Setup the system

### Setup dependencies

> [!WARNING]
> This guide has been tested on a Raspberry Pi, so not all packages might be available on normal repositories.

- Make sure to install the following programs (package names may vary between distros):
    - `git`
    - `wget`
    - `mariadb`/`mysql`
    - `apache2`/`httpd`
    - `php8.2-mysqli`
    - `php`
    - `ffmpeg`
    - `jdk` (I used the openjdk-17-jre package)

    In my case, I ran the command:
    ```bash
    sudo apt update
    sudo apt upgrade
    sudo apt install git wget mariadb-server apache2 php php8.2-mysqli ffmpeg openjdk-17-jre
    ```


- Follow the setup by running the command 
    ```bash
    sudo mysql_secure_installation
    ```

- Edit the `/etc/apache2/apache2.conf` (this will disable resource indexing):
    ```conf
    <Directory />
        Options FollowSymLinks
        AllowOverride None
        Require all denied
    </Directory>

    # <Directory /usr/share>
    #       AllowOverride None
    #       Require all granted
    # </Directory>

    <Directory /var/www/>
            Options FollowSymLinks
            AllowOverride None
            Require all granted
    </Directory>
    ```
    Note: depending on your system, the config file might be `/etc/httpd/conf/httpd.conf`, and your server root might be `/srv/http` instead of `/var/www/html`

- Download the resources:
    - `git clone https://github.com/Zac06/phone_surv.git` into your home directory
    
- Create a new directory in a folder of your choice (I did in my home directory)
    ```bash
    mkdir phone_surv-server
    cd phone_surv-server
    ```

- Download the Java server and the Android application from [Releases](https://github.com/Zac06/phone_surv/releases) (or compile them):
    ```bash
    wget 'https://github.com/Zac06/phone_surv/releases/download/1.0/camera-server-1.0.0-RELEASE.jar'
    ```
- Create a `.properties` file in the same folder:
    ```bash
    nano .properties
    ```
    and insert the following content:
    ```bash
    dbHost=localhost
    dbPort=3306
    dbName=phone_surv
    dbUsername=phone_surv_queryusr
    dbPassword=Cornbread0-Phony2-Mobilize3-Crunching6
    dataPath=data
    basePath=/home/zaccaria/phone_surv-server
    cameraServerPort=55555
    liveServerPort=55556
    lang=en
    ```

    Make sure to replace that the password in `dbPassword` matches the one specified in the `schema/users.sql` file. Also, make sure that the `basePath` matches the one you put the Java server in.
    
    The `lang` parameters can also be set to:
    - `it`

- Go ahead and `cd` into the `phone_surv` git repo folder.
- `cd` again into `webpage/`.
- Edit the config.php file so that it picks up the `.properties` file we created earlier:
    ```bash
    nano config.php
    ```

    and edit the path at the line:
    ```php
    $config = parse_ini_file('.properties');
    ```
    into the path you put earlier at `basePath` + `.properties`, so in my case it becomes

    ```php
    $config = parse_ini_file('/home/zaccaria/phone_surv-server/.properties');
    ```
- Make sure that all the parent folders containing the server and the `.properties` file have execution rights. In this case, i had to do:
    ```bash
    sudo chmod o+x /home/zaccaria
    ```

- Save, exit and copy the content of `webpage` into your WebRoot (for example `/var/www/html`). Assuming you're already `cd`'d into the `webpage` folder, run:
    ```bash
    sudo cp -r * /var/www/html
    ```

- Start the MySQL service:
    ```bash
    sudo systemctl start mysql
    ```
- make sure to be `cd`'d into the git repository, in the `schema` folder (`phone_surv/schema`)

- edit the user credentials as your liking in the `users.sql` file

- and get into the MySQL manager:
    ```bash
    sudo mysql -u root
    ```
    or
    ```bash
    mysql -u root -p
    ```
    and inputting the password which was set up in the `mysql_secure_installation` phase.

- here, run 

    ```mysql
    source schema.sql
    source users.sql
    ```

- Create a new systemd service file (edit it according to your needs):
    ```bash
    sudo nano /etc/systemd/system/phone_surv.service
    ```
    with these contents:

    ```conf
    [Unit]
    Description=Java phone_surv server
    After=apache2.service mysql.service
    Requires=apache2.service mysql.service

    [Service]
    # User to run the jar (don't use root unless necessary)
    User=zaccaria
    Group=zaccaria

    # Path to your jar
    ExecStart=/usr/bin/java -jar /home/zaccaria/phone_surv-server/camera-server-1.0.0-RELEASE.jar

    # Working directory (optional)
    WorkingDirectory=/home/zaccaria/phone_surv-server/

    # Restart on crash
    Restart=on-failure

    # Give it a little time if needed
    RestartSec=10

    [Install]
    WantedBy=multi-user.target

    ```

- Enable the `apache2` and `mysql` services:
    ```bash
    sudo systemctl enable apache2
    sudo systemctl enable mysql
    ```

- And restart your system:

    ```bash
    sudo reboot
    ```

Now you can comfortably delete your downloaded git repo `phone_surv` (or not, if you don't want to).
