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
    <h1>Sorveglianza con telefono</h1>

    <?php
        require 'config.php';
        
        try {
            $mysqli=new mysqli(getDbHost(),getDbUsername(),getDbPass(),getDbName());
            if($mysqli->connect_errno) {
                throw new Exception("Error connecting to database: ".$mysqli->connect_error);
            }

        ?>
        
        <h2>Seleziona una telecamera per vedere i video registrati</h2>

        <table>
            <tr>
                <th>Zona</th>
                <th>Vai ai video</th>
                <th>Diretta video</th>
            </tr>

            <?php
                $result=$mysqli->query("select nome from telecamera");
                if($result->num_rows==0) {
                    echo "<tr><td colspan=\"3\">Nessuna telecamera registrata</td></tr>";
                } 

                while($row=$result->fetch_assoc()) {
                    echo "<tr>";
                    echo "<td>".$row["nome"]."</td>";
                    echo "<td><a href=\"camera.php?nome=".$row["nome"]."\">Vai</a></td>";
                    echo "<td><a href=\"live.php?nome=".$row["nome"]."\">Vai</a></td>";
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
