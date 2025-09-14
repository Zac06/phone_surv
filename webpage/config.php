<?php
// internal loader: reads the .properties/.ini file once
function loadDbConfig(): array {
    static $config = null;

    if ($config === null) {
        $config = parse_ini_file('/home/zaccaria/eclipse-workspace/IPCameraReceiver/.properties');
        if ($config === false) {
            throw new RuntimeException('Unable to load DB properties file');
        }
    }

    return $config;
}

// small helpers:
function getDbHost(): string {
    $cfg = loadDbConfig();
    return $cfg['dbHost'];
}

function getDbUsername(): string {
    $cfg = loadDbConfig();
    return $cfg['dbUsername'];
}

function getDbPass(): string {
    $cfg = loadDbConfig();
    return $cfg['dbPassword'];
}

function getDbName(): string {
    $cfg = loadDbConfig();
    return $cfg['dbName'];
}

function getDataPath(): string{
    $cfg = loadDbConfig();
    return $cfg['basePath'].'/'.$cfg['dataPath'].'/';
}

?>