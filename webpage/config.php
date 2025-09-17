<?php
// internal loader: reads the .properties/.ini file once
function loadDbConfig(): array {
    static $config = null;

    if ($config === null) {
        $config = parse_ini_file('.properties');
        if ($config === false) {
            throw new RuntimeException('Unable to load DB properties file');
        }
    }

    return $config;
}

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

function isAbsolutePath(string $path): bool {
    if ($path === '') return false;

    // Unix absolute
    if ($path[0] === '/' || $path[0] === '\\') return true;

    // Windows drive letter absolute (C:\ or C:/)
    if (preg_match('#^[A-Za-z]:[\\\\/]#', $path)) return true;

    // UNC path (\\server\share)
    if (substr($path, 0, 2) === '\\\\') return true;

    return false;
}

function getDataPath(): string{
    $cfg = loadDbConfig();
    if(isAbsolutePath($cfg['dataPath'])){
        return $cfg['dataPath'];
    }
    return $cfg['basePath'].'/'.$cfg['dataPath'].'/';
}

function getLang() : string{
    $cfg = loadDbConfig();
    if(!isset($cfg['lang'])){
        return 'en';
    }
    
    return $cfg['lang'];
}

?>
