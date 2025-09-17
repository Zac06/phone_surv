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
    <title><?php echo $dic['Guarda live'] ?></title>
</head>

<body>
    <?php
    if (!isset($_GET['nome'])) {
        echo "<h2>".$dic['Per favore fornire il nome della camera da guardare in diretta.']."</h2>";
        die();
    }

    $cam = htmlspecialchars($_GET['nome'], ENT_QUOTES);
    ?>

    <h1><?php echo $dic['Stai guardando la telecamera'] ?> <?php echo $cam; ?></h1>

    <img id="live_image" alt="Stream live" style="max-width:100%; border:1px solid #cccccc;">

    <div id="status"><?php echo $dic['Connessione non aperta'] ?></div>

    <script>
        const cameraName = <?php echo json_encode($cam); ?>;
        const ws = new WebSocket("ws://<?php echo $_SERVER['SERVER_NAME']; ?>:55556");

        ws.binaryType = "arraybuffer"; // vogliamo ricevere binari
        ws.onopen = () => {
            document.getElementById('status').innerHTML='Connessione aperta';
            ws.send("camera:" + cameraName);
        };

        // ad ogni frame ricevuto
        ws.onmessage = (event) => {

            const blob = new Blob([event.data], { type: "image/jpeg" });
            const url = URL.createObjectURL(blob);

            // metti il blob nel tag img
            const img = document.getElementById("live_image");
            img.src = url;

            // libera la vecchia URL per non accumulare memoria
            img.onload = () => URL.revokeObjectURL(url);
        };

        ws.onclose = (event) => {
            document.getElementById('status').innerHTML='<?php echo $dic['Connessione terminata']?>.'+(event.reason==='' ? '' : ' <?php echo $dic['Motivo']?>: '+event.reason);
            
            img.src='';
        };
        ws.onerror = (err) => console.error("<?php echo $dic['Errore di connessione'] ?>", err);
    </script>
</body>

</html>
