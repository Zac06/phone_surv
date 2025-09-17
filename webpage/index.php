<?php
require 'config.php';

$lang = getLang();

$dic = include __DIR__ . "/lang/$lang.php";
?>

<!DOCTYPE html>
<html lang="<?php echo $lang ?>">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="style.css">
    <link rel="shortcut icon" href="res/favicon.ico" type="image/x-icon">
    <title><?php echo $dic['Sistema di sorveglianza via telefono'] ?></title>
</head>
<body>
    <h1><?php echo $dic['Sistema di sorveglianza via telefono'] ?></h1>

    <?php
        try {
            $mysqli=new mysqli(getDbHost(),getDbUsername(),getDbPass(),getDbName());
            $mysqli->set_charset('utf8mb4');
            if($mysqli->connect_errno) {
                throw new Exception("Error connecting to database: ".$mysqli->connect_error);
            }

        ?>
        
        <h2><?php echo $dic['Seleziona una telecamera per vedere i video registrati'] ?></h2>

        <table>
            <tr>
                <th><?php echo $dic['Zona'] ?></th>
                <th><?php echo $dic['Vai ai video'] ?></th>
                <th><?php echo $dic['Diretta video'] ?></th>
            </tr>

            <?php
                $result=$mysqli->query("select nome from telecamera");
                if($result->num_rows==0) {
                    echo "<tr><td colspan=\"3\">".$dic['Nessuna telecamera registrata']."</td></tr>";
                } 

                while($row=$result->fetch_assoc()) {
                    echo "<tr>";
                    echo "<td>".htmlspecialchars($row["nome"])."</td>";
                    echo "<td><a href=\"camera.php?nome=".htmlspecialchars($row["nome"])."\">".$dic['Vai']."</a></td>";
                    echo "<td><a href=\"live.php?nome=".htmlspecialchars($row["nome"])."\">".$dic['Vai']."</a></td>";
                    echo "</tr>";
                }
            ?>

        </table>
        
        <?php

            $mysqli->close();
        } catch (Exception $e) {
            echo htmlspecialchars($e->getMessage());
        }

    ?>
</body>
</html>
