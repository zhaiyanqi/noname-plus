Android Versions
================

A node module to get Android versions by API level, semantic version, or version name.

Versions are referenced from [source.android.com/docs/setup/reference/build-numbers](https://source.android.com/docs/setup/reference/build-numbers#platform-code-names-versions-api-levels-and-ndk-releases). The version for "Current Development Build" (`"CUR_DEVELOPMENT"`) is not included in the list of `VERSIONS`.

Release dates are referenced from [https://en.wikipedia.org/wiki/Android_version_history](https://en.wikipedia.org/wiki/Android_version_history).

[![NPM version][npm-image]][npm-url]
[![CircleCI](https://dl.circleci.com/status-badge/img/gh/dvoiss/android-versions/tree/master.svg?style=svg)](https://dl.circleci.com/status-badge/redirect/gh/dvoiss/android-versions/tree/master)

[npm-image]: https://img.shields.io/npm/v/android-versions.svg?style=flat-square
[npm-url]: https://npmjs.org/package/android-versions

## Install

```bash
# NPM
npm install android-versions --save
# YARN
yarn add android-versions
```

## Usage

View the tests for more advanced usage.

```javascript
const android = require('android-versions')
```

#### Get by API level:

```javascript
console.log(android.get(23))

=> { api: 23, semver: "6.0", name: "Marshmallow", versionCode: "M" }
```

#### Get by version:

```javascript
console.log(android.get("2.3.3"))

=> { api: 10, semver: "2.3.3", name: "Gingerbread", versionCode: "GINGERBREAD_MR1" }
```

#### Get all by predicate:

```
android.getAll((version) => {
  return version.api >= 12 && version.api < 15
}).map((version) => version.versionCode)

=> [ "HONEYCOMB_MR1", "HONEYCOMB_MR2", "ICE_CREAM_SANDWICH" ]
```

#### Access a specific version with all info:

```
android.LOLLIPOP

=> { api: 21, semver: "5.0", name: "Lollipop", versionCode: "LOLLIPOP" }
```

#### Access the complete reference of Android versions with all info:

```javascript
android.VERSIONS

=> {
  BASE:    { api: 1,  semver: "1.0", name: "(no code name)", versionCode: "BASE", releaseDate: "23 Sep 2008" },
  ...
  N:       { api: 24, semver: "7.0", name: "Nougat",         versionCode: "N",    releaseDate: "22 Aug 2016" }
  ...
}
```

## Test

```bash
npm run test
```

## License

MIT
