include_defs('//bucklets/gerrit_plugin.bucklet')

gerrit_plugin(
  name = 'zoektconnector',
  srcs = glob(['src/main/java/**/*.java']),
  resources = glob(['src/main/resources/**/*']),
  manifest_entries = [
    'Gerrit-PluginName: zoektconnector',
    'Gerrit-ApiType: plugin',
    'Gerrit-ApiVersion: 2.14',
    'Gerrit-HttpModule: com.googlesource.gerrit.plugins.gerritzoektconnector.ZoektConnectorModule',
  ],
)

# this is required for bucklets/tools/eclipse/project.py to work
java_library(
  name = 'classpath',
  deps = [':zoektconnector__plugin'],
)

