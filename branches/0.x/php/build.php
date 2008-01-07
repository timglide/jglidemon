<?php
// JGlideMon - A Java based remote monitor for MMO Glider
// Copyright (C) 2007 - 2008 Tim
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, write to the Free Software Foundation, Inc.,
// 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.


// builds phpglidemon.php by encoding/appending the necessary files
// and doing any required replacements


ini_set('display_errors', 'on');
error_reporting(E_ALL);

echo "---- Building PHPGlideMon ----\n\n";

$version = $_SERVER['argc'] > 1 ? $_SERVER['argv'][1] : '0.0';

$here = dirname(__FILE__);
$outFile = $here . '/phpglidemon.php';
$tmpFile = $outFile . '.tmp';
$inFile = $outFile . '.in';

$resourcesDir = $here . '/../jgm/resources/httpd/static/';

if (!file_exists($inFile)) {
	echo "Unable to find $inFile\n";
	exit (1);
}

$files = array();
$exts = array(
	'html' => 'text/html',
	'css' => 'text/css',
	'js' => 'text/javascript',
	'png' => 'image/png',
	'gif' => 'image/gif',
	'jpg' => 'image/jpeg'
);



///  MAIN PART HERE

// get file list
doDir();

$fp = fopen($tmpFile, 'wb');

if (!$fp) {
	echo 'Unable to open ' . $tmpFile;
	exit (1);
}

$totalLength = 0;

$replaceThese = array(
	'index.html', 'main.css', 'js/ajaxsettings.js'
);

echo "  Compressing Files... ";

// compress/encode each file and append it to the temp file
foreach (array_keys($files) as $key) {
	$data = file_get_contents($resourcesDir . $key);

	// replace urls if necessary
	if (in_array($key, $replaceThese))
		$data = replaceUrls($data);
	
	$data = base64_encode(gzdeflate($data));
	$files[$key][0] = $totalLength;
	$files[$key][1] = strlen($data);
	$totalLength += $files[$key][1];

	fwrite($fp, $data);
}

fclose($fp);

echo "Done\n";

@unlink($outFile);
copy($inFile, $outFile);

$fp = fopen($inFile, 'rb');

if (!$fp) {
	echo "Unable to open $inFile\n";
	exit (1);
}

$fpout = fopen($outFile, 'wb');

if (!$fp) {
	echo "Unable to opne $outFile\n";
	exit (1);
}


echo "  Inserting Files Array...\n";

while (!feof($fp) && false !== ($line = rtrim(fgets($fp)))) {
	if (false !== strpos($line, '@@version@@'))
		$line = str_replace('@@version@@', $version, $line);

	if ($line != '')
		fwrite($fpout, $line);

	if (false !== strpos($line, '__halt_compiler();'))
		break; // don't add extra newlines or anything 
			
	fwrite($fpout, "\n");

	if (false !== strpos($line, '//----FILE_LIST_START----')) {
		fwrite($fpout, "\t" . '$files = ' . var_export($files, true) . ';' . "\n");
	}
}


fclose($fp);


echo "  Inserting Compressed Files...\n";

$fp = fopen($tmpFile, 'rb');

if (!$fp) {
	echo "Unable to open $tmpFile\n";
	exit(1);
}


while (!feof($fp) && false !== ($buff = fread($fp, 8192))) {
	fwrite($fpout, $buff);
}

fclose($fp);
fclose($fpout);

@unlink($tmpFile);

echo "\n---- Done Building PHPGlideMon ---- \n";

die();

/// END MAIN PART



function doDir($path = '') {
	global $resourcesDir, $files, $exts;

	$d = dir($resourcesDir . $path);

	while (false !== ($file = $d->read())) {
		if ($file{0} == '.') continue; // ignore ., .., .files

		$cur = ($path != '' ? $path . '/' : '') . $file;
		$realCur = $resourcesDir . $cur;
		if (is_dir($realCur)) {
			doDir($cur);
			continue;
		}

		$ext = getExt($cur);
		if (!isset($exts[$ext])) continue;

		$files[$cur] = array(0, filesize($realCur), $exts[$ext]);
	}
}


function replaceUrls($str) {
	static $find = array(
		'/static/',
		'/command?',
		'/ajax/',
		'/screenshot?'
	);

	static $replace = array(
		'$PHP_SELF?url=/static/',
		'/command&',
		'$PHP_SELF?url=/ajax/',
		'$PHP_SELF?url=/screenshot&'
	);

	return str_replace($find, $replace, $str);
}


function getExt($str) {
	$i = strrpos($str, '.');

	// not found or the last char is '.'
	if ($i === false || strlen($str) - 1 == $i) return '';

	return substr($str, $i + 1);
}
?>
