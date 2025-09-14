<?php
    require 'config.php';

    // Directory where your videos are stored (outside webroot recommended)
    $videoDir = getDataPath();

    // Allowed video extensions
    $allowedExtensions = ['mp4', 'webm', 'ogg', 'mkv', 'avi'];

    // Check if file parameter is provided
    if (!isset($_GET['nomefile_v'])) {
        http_response_code(400);
        die("Missing file parameter.");
    }

    // Normalize filename
    $file = basename($_GET['nomefile_v']);
    $ext = strtolower(pathinfo($file, PATHINFO_EXTENSION));

    // Check extension
    if (!in_array($ext, $allowedExtensions)) {
        http_response_code(403);
        die("Forbidden file type.");
    }

    // Resolve the real path and ensure it's inside $videoDir
    $path = realpath($videoDir . $file);
    if ($path === false || strpos($path, $videoDir) !== 0) {
        http_response_code(403);
        die("Invalid path.");
    }

    // Ensure file exists
    if (!file_exists($path)) {
        http_response_code(404);
        die("File not found.");
    }

    // Detect MIME type
    $finfo = finfo_open(FILEINFO_MIME_TYPE);
    $mime = finfo_file($finfo, $path);
    finfo_close($finfo);

    // Validate it's actually a video
    if (strpos($mime, "video/") !== 0) {
        http_response_code(403);
        die("Invalid file: not a video.");
    }

    $size = filesize($path);
    $length = $size;
    $start = 0;
    $end = $size - 1;

    // Handle byte ranges (for seeking)
    if (isset($_SERVER['HTTP_RANGE'])) {
        if (preg_match('/bytes=(\d+)-(\d*)/', $_SERVER['HTTP_RANGE'], $matches)) {
            $start = intval($matches[1]);
            if (!empty($matches[2])) {
                $end = intval($matches[2]);
            }
        }
        $length = $end - $start + 1;
        http_response_code(206);
        header("Content-Range: bytes $start-$end/$size");
    } else {
        http_response_code(200);
    }

    // Send headers
    header("Content-Type: $mime");
    header("Content-Length: $length");
    header("Accept-Ranges: bytes");

    // Stream the file
    $fp = fopen($path, 'rb');
    fseek($fp, $start);
    $bufferSize = 1024 * 8;
    while (!feof($fp) && ($pos = ftell($fp)) <= $end) {
        if ($pos + $bufferSize > $end) {
            $bufferSize = $end - $pos + 1;
        }
        echo fread($fp, $bufferSize);
        flush();
    }
    fclose($fp);
?>