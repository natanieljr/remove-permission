package org.droidmate.analyzer

import com.beust.jcommander.JCommander
import com.beust.jcommander.ParameterException
import org.apache.commons.io.FileUtils
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.runners.MethodSorters
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**s
 * Main system test
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(JUnit4::class)
class MainTest{
    /*fun createConfig() : Configuration{
        val args = arrayOf("-input", "test_apk",
                "-device", "0",
                "-output", "test_data",
                "-evalStrategy", "AlwaysValid",
                "-apiPolicy", "Mock")

        val cfg = Configuration()
        val jc = JCommander(cfg)
        try {
            jc.parse(*args)

            return cfg
        } catch (e: ParameterException) {
            System.err.println(e.message)
            throw UnsupportedOperationException(e.message)
        }
    }

    fun recreateDir(dirName : String ) : Path {
        val dir = Paths.get(dirName)
        if (Files.exists(dir))
            FileUtils.deleteDirectory(dir.toFile())
        Files.createDirectories(dir)

        return dir
    }*/

    // Coomented for
    @Test
    fun MainTest(){
        /*val testApkDir = this.recreateDir("test_apk")
        this.recreateDir("test_data")

        val origApkName = Paths.get("net.zedge.android_5.4.6", "net.zedge.android-inlined.apk")
        val origApk = ResourceManager().loadResourceFile(origApkName.toString())
        val dstApk = testApkDir.resolve(origApk.fileName)
        Files.copy(origApk, dstApk)

        assert(Files.exists(dstApk))

        val cfg = this.createConfig()

        val storedExpl = ResourceManager().loadResourceFile("net.zedge.android_5.4.6")
        val boxMateStub : BoxMateWrapperStub = BoxMateWrapperStub(storedExpl)
        val appUnderTest = AppUnderTest(cfg, dstApk, boxMateStub)

        val reporter = ReportGenerator()
        val analyzer = AppAnalyzer(cfg)

        analyzer.analyze(appUnderTest, reporter)

        assert(appUnderTest.scenarios.size == 38)
        assert(appUnderTest.successfulScenarios.size == 38)
        assert(appUnderTest.failScenarios.isEmpty())*/
    }
}