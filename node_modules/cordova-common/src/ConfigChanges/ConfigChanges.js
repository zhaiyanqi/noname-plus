/**
    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.
*/

/*
 * This module deals with shared configuration / dependency "stuff". That is:
 * - XML configuration files such as config.xml, AndroidManifest.xml or WMAppManifest.xml.
 * - plist files in iOS
 * Essentially, any type of shared resources that we need to handle with awareness
 * of how potentially multiple plugins depend on a single shared resource, should be
 * handled in this module.
 *
 * The implementation uses an object as a hash table, with "leaves" of the table tracking
 * reference counts.
 */

const path = require('path');
const et = require('elementtree');
const ConfigKeeper = require('./ConfigKeeper');
const events = require('../events');
const mungeutil = require('./munge-util');
const xml_helpers = require('../util/xml-helpers');

exports.process = (plugins_dir, project_dir, platform, platformJson, pluginInfoProvider) => {
    const munger = new PlatformMunger(platform, project_dir, platformJson, pluginInfoProvider);
    munger.process(plugins_dir);
    munger.save_all();
};

/******************************************************************************
* PlatformMunger class
*
* Can deal with config file of a single project.
* Parsed config files are cached in a ConfigKeeper object.
******************************************************************************/
class PlatformMunger {
    constructor (platform, project_dir, platformJson, pluginInfoProvider) {
        this.platform = platform;
        this.project_dir = project_dir;
        this.config_keeper = new ConfigKeeper(project_dir);
        this.platformJson = platformJson;
        this.pluginInfoProvider = pluginInfoProvider;
    }

    // Write out all unsaved files.
    save_all () {
        this.config_keeper.save_all();
        this.platformJson.save();
    }

    // Apply a munge object to a single config file.
    // The remove parameter tells whether to add the change or remove it.
    apply_file_munge (file, munge, remove) {
        for (const selector in munge.parents) {
            for (const mungeElem of munge.parents[selector]) {
                // this xml child is new, graft it (only if config file exists)
                const config_file = this.config_keeper.get(this.project_dir, this.platform, file);

                if (config_file.exists) {
                    const operation = remove ? 'prune_child' : 'graft_child';
                    config_file[operation](selector, mungeElem);
                } else {
                    events.emit('warn', `config file ${file} requested for changes not found at ${config_file.filepath}, ignoring`);
                }
            }
        }
    }

    remove_plugin_changes (pluginInfo, is_top_level) {
        const platform_config = this.platformJson.root;
        const plugin_vars = is_top_level
            ? platform_config.installed_plugins[pluginInfo.id]
            : platform_config.dependent_plugins[pluginInfo.id];

        const edit_config_changes = this._getChanges(pluginInfo, 'EditConfig');

        // get config munge, aka how did this plugin change various config files
        const config_munge = this.generate_plugin_config_munge(pluginInfo, plugin_vars, edit_config_changes);

        this._munge_helper(config_munge, { remove: true });

        // Remove from installed_plugins
        this.platformJson.removePlugin(pluginInfo.id, is_top_level);

        return this;
    }

    add_plugin_changes (pluginInfo, plugin_vars, is_top_level, should_increment, plugin_force) {
        const edit_config_changes = this._getChanges(pluginInfo, 'EditConfig');

        const { configConflicts, pluginConflicts } = this._is_conflicting(edit_config_changes);
        if (Object.keys(configConflicts.files).length > 0) {
            // plugin.xml cannot overwrite config.xml changes even if --force is used
            throw new Error(`${pluginInfo.id} cannot be added. <edit-config> changes in this plugin conflicts with <edit-config> changes in config.xml. Conflicts must be resolved before plugin can be added.`);
        }
        if (plugin_force) {
            events.emit('warn', '--force is used. edit-config will overwrite conflicts if any. Conflicting plugins may not work as expected.');

            // remove conflicting munges, if any
            this._munge_helper(pluginConflicts, { remove: true });
        } else if (Object.keys(pluginConflicts.files).length > 0) {
            // plugin cannot overwrite other plugin changes without --force
            const witness = Object.values(Object.values(pluginConflicts.files)[0].parents)[0][0];
            const conflictingPlugin = witness.plugin;
            throw new Error(`There was a conflict trying to modify attributes with <edit-config> in plugin ${pluginInfo.id}. The conflicting plugin, ${conflictingPlugin}, already modified the same attributes. The conflict must be resolved before ${pluginInfo.id} can be added. You may use --force to add the plugin and overwrite the conflicting attributes.`);
        }

        // get config munge, aka how should this plugin change various config files
        const config_munge = this.generate_plugin_config_munge(pluginInfo, plugin_vars, edit_config_changes);
        this._munge_helper(config_munge, { should_increment });

        // Move to installed/dependent_plugins
        this.platformJson.addPlugin(pluginInfo.id, plugin_vars || {}, is_top_level);

        return this;
    }

    // Handle edit-config changes from config.xml
    add_config_changes (config, should_increment) {
        const changes = [
            ...this._getChanges(config, 'EditConfig'),
            ...this._getChanges(config, 'ConfigFile')
        ];

        const { configConflicts, pluginConflicts } = this._is_conflicting(changes);
        if (Object.keys(pluginConflicts.files).length !== 0) {
            events.emit('warn', 'Conflict found, edit-config changes from config.xml will overwrite plugin.xml changes');
        }
        // remove conflicting config.xml & plugin.xml munges, if any
        for (const conflict_munge of [configConflicts, pluginConflicts]) {
            this._munge_helper(conflict_munge, { remove: true });
        }

        // Add config.xml edit-config and config-file munges
        const config_munge = this.generate_config_xml_munge(config, changes, 'config.xml');
        this._munge_helper(config_munge, { should_increment });

        // Move to installed/dependent_plugins
        return this;
    }

    /** @private */
    _munge_helper (config_munge, { should_increment = true, remove = false } = {}) {
        // global munge looks at all changes to config files
        // TODO: The should_increment param is only used by cordova-cli and is going away soon.
        // If should_increment is set to false, avoid modifying the global_munge (use clone)
        // and apply the entire config_munge because it's already a proper subset of the global_munge.

        const platform_config = this.platformJson.root;
        const global_munge = platform_config.config_munge;

        const method = remove ? 'decrement_munge' : 'increment_munge';
        const munge = should_increment
            ? mungeutil[method](global_munge, config_munge)
            : config_munge;

        for (const file in munge.files) {
            this.apply_file_munge(file, munge.files[file], remove);
        }

        return this;
    }

    // Load the global munge from platform json and apply all of it.
    // Used by cordova prepare to re-generate some config file from platform
    // defaults and the global munge.
    reapply_global_munge () {
        const platform_config = this.platformJson.root;
        const global_munge = platform_config.config_munge;
        for (const file in global_munge.files) {
            this.apply_file_munge(file, global_munge.files[file]);
        }

        return this;
    }

    // Generate the munge object from config.xml
    generate_config_xml_munge (config, config_changes, type) {
        const originInfo = { id: type === 'config.xml' ? type : config.id };
        return this._generateMunge(config_changes, originInfo);
    }

    // Generate the munge object from plugin.xml + vars
    generate_plugin_config_munge (pluginInfo, vars, edit_config_changes) {
        const changes = pluginInfo.getConfigFiles(this.platform);
        if (edit_config_changes) {
            Array.prototype.push.apply(changes, edit_config_changes);
        }
        const filteredChanges = changes.filter(({ mode }) => mode !== 'remove');

        const originInfo = { plugin: pluginInfo.id };
        return this._generateMunge(filteredChanges, originInfo, vars || {});
    }

    /** @private */
    _generateMunge (changes, originInfo, vars = {}) {
        const munge = { files: {} };

        changes.forEach(change => {
            const [file, selector, rest] = change.mode
                ? [change.file, change.target, { mode: change.mode, ...originInfo }]
                : [change.target, change.parent, { after: change.after }];

            change.xmls.forEach(xml => {
                // 1. stringify each xml
                let stringified = (new et.ElementTree(xml)).write({ xml_declaration: false });

                // interpolate vars, if any
                Object.keys(vars).forEach(key => {
                    const regExp = new RegExp(`\\$${key}`, 'g');
                    stringified = stringified.replace(regExp, vars[key]);
                });

                // 2. add into munge
                mungeutil.deep_add(munge, file, selector, { xml: stringified, count: 1, ...rest });
            });
        });

        return munge;
    }

    /** @private */
    _getChanges (cfg, changeType) {
        const method = `get${changeType}s`;
        return (cfg[method] && cfg[method](this.platform)) || [];
    }

    /** @private */
    _is_conflicting (editchanges) {
        const platform_config = this.platformJson.root;
        const { files } = platform_config.config_munge;

        const configConflicts = { files: {} }; // config.xml edit-config conflicts
        const pluginConflicts = { files: {} }; // plugin.xml edit-config conflicts

        const registerConflict = (file, selector) => {
            const witness = files[file].parents[selector][0];
            const conflictMunge = witness.id === 'config.xml' ? configConflicts : pluginConflicts;
            mungeutil.deep_add(conflictMunge, file, selector, witness);
        };

        editchanges.forEach(({ file, target }) => {
            if (!files[file]) return;
            const { parents: changesBySelector } = files[file];

            const conflicts = changesBySelector[target] || [];
            if (conflicts.length > 0) return registerConflict(file, target);

            const targetFile = this.config_keeper.get(this.project_dir, this.platform, file);

            // For non-XML files (e.g. plist), the selector in editchange.target uniquely identifies its target.
            // Thus we already know that we have no conflict if we are not dealing with an XML file here.
            if (targetFile.type !== 'xml') return;

            // For XML files, the selector does NOT uniquely identify its target. So we resolve editchange.target
            // and any existing selectors to their matched elements and compare those for equality.
            const resolveEditTarget = xml_helpers.resolveParent(targetFile.data, target);
            if (!resolveEditTarget) return;

            const selector = Object.keys(changesBySelector).find(parent =>
                resolveEditTarget === xml_helpers.resolveParent(targetFile.data, parent)
            );
            if (selector) return registerConflict(file, selector);
        });

        return {
            configConflicts,
            pluginConflicts
        };
    }

    // Go over the prepare queue and apply the config munges for each plugin
    // that has been (un)installed.
    process (plugins_dir) {
        const platform_config = this.platformJson.root;

        // Uninstallation first
        platform_config.prepare_queue.uninstalled.forEach(u => {
            const pluginInfo = this.pluginInfoProvider.get(path.join(plugins_dir, u.plugin));
            this.remove_plugin_changes(pluginInfo, u.topLevel);
        });

        // Now handle installation
        platform_config.prepare_queue.installed.forEach(u => {
            const pluginInfo = this.pluginInfoProvider.get(path.join(plugins_dir, u.plugin));
            this.add_plugin_changes(pluginInfo, u.vars, u.topLevel, true, u.force);
        });

        // Empty out installed/ uninstalled queues.
        platform_config.prepare_queue.uninstalled = [];
        platform_config.prepare_queue.installed = [];
    }
}

exports.PlatformMunger = PlatformMunger;
