<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="style.css">
    <link rel="shortcut icon" href="res/favicon.ico" type="image/x-icon">
    <title>Guarda video</title>
</head>

<body>
    
    <?php
    if (!isset($_GET['nomefile_v'])) {
        ?>
        <h2>Per favore fornire il nome del file da visualizzare.</h2>
        <?php
        die();
    }
    $file = htmlspecialchars($_GET['nomefile_v'], ENT_QUOTES); // escape for HTML
    ?>

    <h1>Riproducendo <?php echo $file; ?></h1>

    <video controls>
        <source src="video.php?nomefile_v=<?php echo urlencode($file); ?>" type="video/mp4">
        Your browser does not support HTML5 video.
    </video>
</body>

</html>
