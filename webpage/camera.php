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
    <?php
        if(!isset($_GET['nome'])||$_GET['nome']==''){
            echo "<h2>".$dic['Per favore, dare come parametro il nome di una telecamera']."</h2>";
            die();
        }else{
            $nome_cam=$_GET['nome'];
        }
    ?>

    <h1><?php if(isset($_GET["nome"])) echo htmlspecialchars($_GET["nome"]);?></h1>

    <?php
        try {
            $mysqli=new mysqli(getDbHost(),getDbUsername(),getDbPass(),getDbName());
            $mysqli->set_charset('utf8mb4');
            if($mysqli->connect_errno) {
                throw new Exception("Error connecting to database: ".$mysqli->connect_error);
            }

        ?>
        
        <h2><?php echo $dic['Seleziona un video registrato da visualizzare']?></h2>

        <table>
            <tr>
                <th><?php echo $dic['Inizio video'] ?></th>
                <th><?php echo $dic['Fine del video'] ?></th>
                <th><?php echo $dic['Vai al video'] ?></th>
            </tr>

            <?php
                //$result=$mysqli->query("select inizio, fine, nomefile_v from intervallo join telecamera using(id_cam) join video using(id_int) where nome='$nome_cam' and fine is not null");
                $stmt = $mysqli->prepare("select inizio, fine, nomefile_v from intervallo join telecamera USING(id_cam) join video using(id_int) where nome=? and fine is not null");
                $stmt->bind_param("s", $nome_cam);
                $stmt->execute();
                $result = $stmt->get_result();
                
                if($result->num_rows==0){
                    echo "<tr><td colspan=\"3\">".$dic['Nessun video ancora registrato da questa telecamera']."</td></tr>";
                }

                while($row=$result->fetch_assoc()) {
                    echo "<tr>";
                    echo "<td>".htmlspecialchars($row["inizio"])."</td>";
                    echo "<td>".htmlspecialchars($row["fine"])."</td>";
                    echo "<td><a target='_blank' href=watch.php?nomefile_v=".htmlspecialchars($row["nomefile_v"]).">".$dic['Vai al video']."</a></td>";
                    echo "</tr>";
                }

                $stmt->close();
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
