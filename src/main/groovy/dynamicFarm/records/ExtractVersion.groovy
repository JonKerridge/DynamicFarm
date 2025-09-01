package dynamicFarm.records

class ExtractVersion {
  static boolean extractVersion(String version) {
      String packageName = VersionControl.packageName
      String userHome = System.getProperty("user.home")
      String jarLocation = "${userHome}\\.m2\\repository\\jonkerridge\\${packageName}"
      String gradleLocation = "${userHome}\\.gradle\\caches\\modules-2\\files-2.1\\jonkerridge\\${packageName}"
      String folder = gradleLocation + "\\$version"
      if (new File(folder).isDirectory())
        return true
      else {
        folder = jarLocation + "\\$version"
        if (new File(folder).isDirectory())
          return true
        else
          return false
      }
  }

  static void main(String[] args) {
    String version = VersionControl.versionTag
    if (!extractVersion(version)) println "dynamicFarm: Version $version needs to downloaded, please modify the gradle.build file"
    else println "Correct version is available: $version"
  }
}
