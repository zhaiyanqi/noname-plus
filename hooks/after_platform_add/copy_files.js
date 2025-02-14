#!/usr/bin/env node

const fs = require('fs-extra');
const path = require('path');

// 源资源文件夹路径（相对于项目根目录）
const sourceResDir = './res';

// 目标平台资源文件夹路径（相对于 platforms/android 目录）
const targetResDir = path.join('./platforms', 'android', 'app', 'src', 'main', 'res');

module.exports = function (context) {
	// const { cordova } = context.requireCordovaLib;

	return Promise.resolve()
		.then(() => {
			if (!fs.existsSync(sourceResDir)) {
				console.log(`Source res directory "${sourceResDir}" does not exist. Skipping resource copying.`);
				return;
			}

			if (!fs.existsSync(targetResDir)) {
				console.warn(`Target res directory "${targetResDir}" does not exist. Unable to copy resources.`);
				return;
			}
			console.log(`Copying res files from "${sourceResDir}" to "${targetResDir}"`);
			return fs.promises.readdir(targetResDir).then(files => {
				// 读取目标文件夹，删除mipmap
				files.forEach((file) => {
					const sourcePath = path.join(sourceResDir, file);
					const targetPath = path.join(targetResDir, file);
					if (fs.statSync(targetPath).isDirectory() && file.startsWith('mipmap')) {
						console.log(`Removing existing directory "${targetPath}"`);
						fs.removeSync(targetPath);
					}
				});
			}).then(() => {
				// 读取根目录下的res文件夹，把mipmap、drawable和layout复制过去
				return fs.promises.readdir(sourceResDir).then(files => {
					files.forEach((file) => {
						const sourcePath = path.join(sourceResDir, file);
						const targetPath = path.join(targetResDir, file);
						if (fs.statSync(sourcePath).isDirectory() && ['mipmap', 'drawable', 'layout'].some(v => file.startsWith(v))) {
							console.log(`Copying existing directory "${sourcePath}"`);
							fs.copySync(sourcePath, targetPath, { overwrite: true });
						}
					});
				});
			}).then(() => {
			    // 覆盖NonameCore代码
                console.log(`Copying module files from "./NonameCore" to "./platforms/android/NonameCore"`);
                fs.copySync('./NonameCore', path.join('./platforms', 'android', 'NonameCore'), { overwrite: true });
                // 覆盖res/values代码
				console.log(`Copying values files from "./res/values" to "./platforms/android/app/src/main/res/values"`);
				fs.copySync('./res/values', path.join('./platforms', 'android', 'app', 'src', 'main', 'res', 'values'), { overwrite: true });
				// 覆盖res/values-v27代码
				console.log(`Copying values-v27 files from "./res/values" to "./platforms/android/app/src/main/res/values"`);
				fs.copySync('./res/values-v27', path.join('./platforms', 'android', 'app', 'src', 'main', 'res', 'values-v27'), { overwrite: true });
				// 覆盖res/xml代码
				console.log(`Copying xml files from "./res/values" to "./platforms/android/app/src/main/res/xml"`);
				fs.copySync('./res/xml', path.join('./platforms', 'android', 'app', 'src', 'main', 'res', 'xml'), { overwrite: true });	
				// 覆盖java代码
				console.log(`Copying java files from "./java" to "./platforms/android/app/src/main/java"`);
				fs.copySync('./java', path.join('./platforms', 'android', 'app', 'src', 'main', 'java'), { overwrite: true });
				// 覆盖captures
				console.log(`Copying captures files from "./captures" to "./platforms/android/captures"`);
				fs.copySync('./captures', path.join('./platforms', 'android', 'captures'), { overwrite: true });
				// 覆盖common
				console.log(`Copying common files from "./common" to "./platforms/android/common"`);
				fs.copySync('./common', path.join('./platforms', 'android', 'common'), { overwrite: true });
				// 覆盖FunctionAbout
				console.log(`Copying FunctionAbout files from "./FunctionAbout" to "./platforms/android/FunctionAbout"`);
				fs.copySync('./FunctionAbout', path.join('./platforms', 'android', 'FunctionAbout'), { overwrite: true });
				// 覆盖FunctionGameShell
				console.log(`Copying FunctionGameShell files from "./FunctionGameShell" to "./platforms/android/FunctionGameShell"`);
				fs.copySync('./FunctionGameShell', path.join('./platforms', 'android', 'FunctionGameShell'), { overwrite: true });
				// 覆盖FunctionServer
				console.log(`Copying FunctionServer files from "./FunctionServer" to "./platforms/android/FunctionServer"`);
				fs.copySync('./FunctionServer', path.join('./platforms', 'android', 'FunctionServer'), { overwrite: true });
				// 覆盖FunctionVersion
				console.log(`Copying FunctionVersion files from "./FunctionVersion" to "./platforms/android/FunctionVersion"`);
				fs.copySync('./FunctionVersion', path.join('./platforms', 'android', 'FunctionVersion'), { overwrite: true });
				// 覆盖FunctionServer
				console.log(`Copying FunctionLibrary files from "./FunctionLibrary" to "./platforms/android/FunctionLibrary"`);
				fs.copySync('./FunctionLibrary', path.join('./platforms', 'android', 'FunctionLibrary'), { overwrite: true });
				// 覆盖FunctionServer
				console.log(`Copying NonameUI files from "./NonameUI" to "./platforms/android/NonameUI"`);
				fs.copySync('./NonameUI', path.join('./platforms', 'android', 'NonameUI'), { overwrite: true });
				// 覆盖cordovaLib库
				console.log(`Copying java files from "./cordovaLib" to "./platforms/android/cordovaLib"`);
				fs.copySync('./cordovaLib', path.join('./platforms', 'android', 'cordovaLib'), { overwrite: true });
				// 覆盖libs库
				console.log(`Copying java files from "./libs" to "./platforms/android/app/src/main/libs"`);
				fs.copySync('./libs', path.join('./platforms', 'android', 'app', 'src', 'main', 'libs'), { overwrite: true });
				// 覆盖assets
				console.log(`Copying assets files from "./assets" to "./platforms/android/app/src/main/assets"`);
				fs.copySync('./assets', path.join('./platforms', 'android', 'app', 'src', 'main', 'assets'), { overwrite: true });
				// 覆盖gradle
				console.log(`Copying gradle files from "./gradle" to "./platforms/android/gradle"`);
				fs.copySync('./gradle', path.join('./platforms', 'android', 'gradle'), { overwrite: true });
			})
		})
		.catch((err) => {
			console.error('An error occurred while copying files:', err);
			process.exitCode = 1;
		});
};