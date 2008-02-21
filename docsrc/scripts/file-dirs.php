<?php
/*  
  +----------------------------------------------------------------------+
  | PHP Version 4                                                        |
  +----------------------------------------------------------------------+
  | Copyright (c) 1997-2004 The PHP Group                                |
  +----------------------------------------------------------------------+
  | This source file is subject to version 3.0 of the PHP license,       |
  | that is bundled with this package in the file LICENSE, and is        |
  | available through the world-wide-web at the following url:           |
  | http://www.php.net/license/3_0.txt.                                  |
  | If you did not receive a copy of the PHP license and are unable to   |
  | obtain it through the world-wide-web, please send a note to          |
  | license@php.net so we can mail you a copy immediately.               |
  +----------------------------------------------------------------------+
  | Authors:    Hartmut Holzgraefe <hholzgra@php.net>                    |
  |             Gabor Hojtsy <goba@php.net>                              |
  +----------------------------------------------------------------------+
  
  Orig Id: file-entities.php.in,v 1.22 2004/09/02 22:28:30 goba Exp

  MathCS $Id: file-entities.php,v 1.1 2005/01/02 21:38:37 tmullin Exp $
*/

/**
 *
 * Create entities/file-entities.ent with respect
 * to all the specialities needed:
 *
 * Also take in account, that if XSLT style sheets are used,
 * special file:/// prefixed path values are needed.
 *
 */

// Always flush output
ob_implicit_flush();
// This script runs for a long time
set_time_limit(0);

//$WORKDIR = 'D:/www/Apache/htdocs/mathcs/docsrc';
$WORKDIR = '.';
$SRCDIR  = "$WORKDIR/xml";

// ......:ARGUMENT PARSING:.....................................................
	
// The output directory, which we need to parse for windows specific
// things, and correct all problems is needed.
// Also use absolute path to have meaningful error messages
$out_dir = $WORKDIR . '/output/chunk'; //abs_path(strip_cygdrive($WORKDIR));

// this path if used for saving the ent file:
$script_out_dir = $out_dir;

// The source directory is passed in the 5th argument counting from backwards.
$srcdir = $SRCDIR; //abs_path($SRCDIR);

// ......:ENTITY CREATION:......................................................

// Put all the file entitites info $entities
$dirs = array();
file_dirs($srcdir, $out_dir, $dirs);

echo "\nCreating necessary directories...\n";

// Write out all other entities
foreach ($dirs as $dir) {
	echo "    $dir\n";
	mkdir($dir);
}

// Here is the end of the code
exit;

// ......:FUNCTION DECLARATIONS:................................................

/**
 * Generate absolute path from a relative path, taking accout
 * the current wokring directory.
 *
 * @param string $path Relative path
 * @return string Absolute path generated
 */
function abs_path($path) {

    // This is already an absolute path (begins with / or a drive letter)
    if (preg_match("!^(/|\\w:)!", $path)) { return $path; }

    // Get directory parts

    $absdir  = str_replace("\\", "/", getcwd());
    $absdirs = explode("/", preg_replace("!/scripts$!", "", $absdir));
    $dirs    = explode("/", $path);

    // Generate array representation of absolute path
    foreach ($dirs as $dir) {
        if (empty($dir) or $dir == ".") continue;
        else if ($dir == "..") array_pop($absdirs);
        else array_push($absdirs, $dir);
    }

    // Return with string
    return join("/", $absdirs);
}

/**
 * Create file entities, and put them into the array passed as the
 * last argument (passed by reference).
 *
 * @param string $work_dir English files' directory
 * @param string $orig_dir Original directory
 * @param array $entities Entities string array
 * @return boolean Success signal
 */
function file_dirs($work_dir, $out_dir, &$entities) {
    // Try to open English working directory
    $dh = opendir($work_dir);
    if (!$dh) { return FALSE; }

    // While we can read that directory
    while (($file = readdir($dh)) !== FALSE) {

        // If file name begins with . skip it
        if ($file{0} == ".") { continue; }

        // If we found a directory, and it's name is not
        // CVS, recursively go into it, and generate entities
        if (is_dir($work_dir . "/" . $file)) {
            if ($file == "CVS") { continue; }
			$entities[] = $out_dir . '/' . $file;
            file_dirs($work_dir . "/" . $file, $out_dir . '/' . $file, $entities);
        }
    } // end of readdir loop
    
    // Close directory
    closedir($dh);
} // end of funciton file_entities()

/**
 * Convert a file name (with path) to an entity name.
 *
 * Converts: _ => - and / => .
 *
 * @param string $fname File name
 * @return string Entity name
 */
function fname2entname($fname)
{
    return str_replace("_", "-", str_replace("/", ".", $fname));
}

/**
 * Return entity string with the given entityname and filename.
 * 
 * @param string $entname Entity name
 * @param string $filename Name of file
 * @return string Entity declaration string
 */
function entstr($entname, $filename)
{
    // If we have no file, than this is not a system entity
    if ($filename == "") {
        return sprintf("<!ENTITY %-28s        ''>\n", $entname);
    } else {
        return sprintf("<!ENTITY %-28s SYSTEM '%s'>\n", $entname, strip_cygdrive($filename));
    }
}

/**
 * Return windows style path for cygwin.
 * 
 * @param string $path Orginal path
 */
function strip_cygdrive($path){
	return preg_replace("!^/cygdrive/(\\w)/!", "\\1:/", $path);
}
?>
