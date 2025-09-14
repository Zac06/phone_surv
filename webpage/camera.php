<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="style.css">
    <link rel="shortcut icon" href="res/favicon.ico" type="image/x-icon">
    <title>Phone surveillance system</title>
</head>
<body>
    <?php
        require 'config.php';

        if(!isset($_GET['nome'])||$_GET['nome']==''){
            echo "<h2>Per favore, dare come parametro il nome di una telecamera.</h2>";
            die();
        }else{
            $nome_cam=$_GET['nome'];
        }
    ?>

    <h1><?php if(isset($_GET["nome"])) echo $_GET["nome"];?></h1>

    <?php
        try {
            $mysqli=new mysqli(getDbHost(),getDbUsername(),getDbPass(),getDbName());
            if($mysqli->connect_errno) {
                throw new Exception("Error connecting to database: ".$mysqli->connect_error);
            }

        ?>
        
        <h2>Seleziona un video registrato da visualizzare</h2>

        <table>
            <tr>
                <th>Inizio video</th>
                <th>Fine del video</th>
                <th>Vai al video</th>
            </tr>

            <?php
                $result=$mysqli->query("select inizio, fine, nomefile_v from intervallo join telecamera using(id_cam) join video using(id_int) where nome='$nome_cam' and fine is not null");
                if($result->num_rows==0){
                    echo "<tr><td colspan=\"3\">Nessun video ancora registrato da questa telecamera</td></tr>";
                }

                while($row=$result->fetch_assoc()) {
                    echo "<tr>";
                    echo "<td>".$row["inizio"]."</td>";
                    echo "<td>".$row["fine"]."</td>";
                    echo "<td><a target='_blank' href=watch.php?nomefile_v=".$row["nomefile_v"].">Vai al video</a></td>";
                    echo "</tr>";
                }
            ?>

        </table>
        
        <?php

            $mysqli->close();
        } catch (Exception $e) {
            echo $e->getMessage();
        }

    ?>
</body>
</html>
