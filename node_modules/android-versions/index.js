/* jshint node: true */
"use strict";

var semver = require("semver");

/**
 * A node module to get Android versions by API level, semantic version, or version name.
 *
 * Versions are referenced from here:
 * {@link https://source.android.com/source/build-numbers.html#platform-code-names-versions-api-levels-and-ndk-releases}
 * {@link https://github.com/android/platform_frameworks_base/blob/master/core/java/android/os/Build.java}
 *
 * The version for "Current Development Build" ("CUR_DEVELOPMENT") is not included.
 *
 * @module android-versions
 */

var VERSIONS = {
  BASE:                   { api: 1,     semver: "1.0",               name: "(no code name)",     releaseDate: "23 Sep 2008" },
  BASE_1_1:               { api: 2,     semver: "1.1",               name: "(no code name)",     releaseDate: "09 Feb 2009" },
  CUPCAKE:                { api: 3,     semver: "1.5",               name: "Cupcake",            releaseDate: "27 Apr 2009" },
  DONUT:                  { api: 4,     semver: "1.6",               name: "Donut",              releaseDate: "15 Sep 2009" },
  ECLAIR:                 { api: 5,     semver: "2.0",               name: "Eclair",             releaseDate: "27 Oct 2009" },
  ECLAIR_0_1:             { api: 6,     semver: "2.0.1",             name: "Eclair",             releaseDate: "03 Dec 2009" },
  ECLAIR_MR1:             { api: 7,     semver: "2.1",               name: "Eclair",             releaseDate: "11 Jan 2010" },
  FROYO:                  { api: 8,     semver: "2.2.x",             name: "Froyo",              releaseDate: "20 May 2010" },
  GINGERBREAD:            { api: 9,     semver: "2.3.0 - 2.3.2",     name: "Gingerbread",        releaseDate: "06 Dec 2010" },
  GINGERBREAD_MR1:        { api: 10,    semver: "2.3.3 - 2.3.7",     name: "Gingerbread",        releaseDate: "09 Feb 2011" },
  HONEYCOMB:              { api: 11,    semver: "3.0",               name: "Honeycomb",          releaseDate: "22 Feb 2011" },
  HONEYCOMB_MR1:          { api: 12,    semver: "3.1",               name: "Honeycomb",          releaseDate: "10 May 2011" },
  HONEYCOMB_MR2:          { api: 13,    semver: "3.2.x",             name: "Honeycomb",          releaseDate: "15 Jul 2011" },
  ICE_CREAM_SANDWICH:     { api: 14,    semver: "4.0.1 - 4.0.2",     name: "Ice Cream Sandwich", releaseDate: "18 Oct 2011" },
  ICE_CREAM_SANDWICH_MR1: { api: 15,    semver: "4.0.3 - 4.0.4",     name: "Ice Cream Sandwich", releaseDate: "16 Dec 2011" },
  JELLY_BEAN:             { api: 16,    semver: "4.1.x",             name: "Jellybean",          releaseDate: "09 Jul 2012" },
  JELLY_BEAN_MR1:         { api: 17,    semver: "4.2.x",             name: "Jellybean",          releaseDate: "13 Nov 2012" },
  JELLY_BEAN_MR2:         { api: 18,    semver: "4.3.x",             name: "Jellybean",          releaseDate: "24 Jul 2013" },
  KITKAT:                 { api: 19,    semver: "4.4.0 - 4.4.4",     name: "KitKat",             releaseDate: "31 Oct 2013" },
  KITKAT_WATCH:           { api: 20,    semver: "4.4",               name: "KitKat Watch",       releaseDate: "25 Jun 2014" },
  LOLLIPOP:               { api: 21,    semver: "5.0",               name: "Lollipop",           releaseDate: "04 Nov 2014" },
  LOLLIPOP_MR1:           { api: 22,    semver: "5.1",               name: "Lollipop",           releaseDate: "02 Mar 2015" },
  M:                      { api: 23,    semver: "6.0",               name: "Marshmallow",        releaseDate: "02 Oct 2015" },
  N:                      { api: 24,    semver: "7.0",               name: "Nougat",             releaseDate: "22 Aug 2016" },
  N_MR1:                  { api: 25,    semver: "7.1",               name: "Nougat",             releaseDate: "04 Oct 2016" },
  O:                      { api: 26,    semver: "8.0.0",             name: "Oreo",               releaseDate: "21 Aug 2017" },
  O_MR1:                  { api: 27,    semver: "8.1.0",             name: "Oreo",               releaseDate: "05 Dec 2017" },
  P:                      { api: 28,    semver: "9",                 name: "Pie",                releaseDate: "06 Aug 2018" },
  Q:                      { api: 29,    semver: "10",                name: "Android10",          releaseDate: "03 Sep 2019" },
  R:                      { api: 30,    semver: "11",                name: "Android11",          releaseDate: "08 Sep 2020" },
  S:                      { api: 31,    semver: "12",                name: "Android12",          releaseDate: "04 Oct 2021" },
  S_V2:                   { api: 32,    semver: "12",                name: "Android12L",         releaseDate: "07 Oct 2022" },
  TIRAMISU:               { api: 33,    semver: "13",                name: "Android13",          releaseDate: "15 Oct 2022" },
  UPSIDE_DOWN_CAKE:       { api: 34,    semver: "14",                name: "Android14",          releaseDate: "04 Oct 2023" }
}

// Add a key to each version of Android for the "versionCode".
// This is the same key we use in the VERSIONS map above.
Object.keys(VERSIONS).forEach(function(version) {
  VERSIONS[version].versionCode = version
})

// semver format requires <major>.<minor>.<patch> but we allow just <major>.<minor> format.
// Coerce <major>.<minor> to <major>.<minor>.0
function formatSemver(semver) {
  if (semver.match(/^\d+.\d+$/)) {
    return semver + '.0'
  } else {
    return semver
  }
}

// The default predicate compares against API level, semver, name, or code.
function getFromDefaultPredicate(arg) {
  // Coerce arg to string for comparisons below.
  arg = arg.toString()

  return getFromPredicate(function(version) {
    // Check API level before all else.
    if (arg === version.api.toString()) {
      return true
    }

    var argSemver = formatSemver(arg)
    if (semver.valid(argSemver) && semver.satisfies(argSemver, version.semver)) {
      return true
    }

    // Compare version name and code.
    return arg === version.name || arg === version.versionCode
  })
}

// The function to allow passing a predicate.
function getFromPredicate(predicate) {
  if (predicate === null) {
    return null
  }

  return Object.keys(VERSIONS).filter(function(version) {
    return predicate(VERSIONS[version])
  }).map(function(key) { return VERSIONS[key] })
}

/**
 * The Android version codes available as keys for easier look-up.
 */
Object.keys(VERSIONS).forEach(function(name) {
  exports[name] = VERSIONS[name]
})

/**
 * The complete reference of Android versions for easier look-up.
 */
exports.VERSIONS = VERSIONS

/**
 * Retrieve a single Android version.
 *
 * @param {object | Function} arg - The value or predicate to use to retrieve values.
 *
 * @return {object} An object representing the version found or null if none found.
 */
exports.get = function(arg) {
  var result = exports.getAll(arg)

  if (result === null || result.length === 0) {
    return null
  }

  return result[0]
}

/**
 * Retrieve all Android versions that meet the criteria of the argument.
 *
 * @param {object | Function} arg - The value or predicate to use to retrieve values.
 *
 * @return {object} An object representing the version found or null if none found.
 */
exports.getAll = function(arg) {
  if (arg === null) {
    return null
  }

  if (typeof arg === "function") {
    return getFromPredicate(arg)
  } else {
    return getFromDefaultPredicate(arg)
  }
}