/*
 *
 *  * Copyright (c) 2022-2022  Bitshift D.O.O (http://bitshifted.co)
 *  *
 *  * This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package co.bitshifted.appforge.backstage.service.deployment.builders

import co.bitshifted.appforge.backstage.BackstageConstants
import co.bitshifted.appforge.backstage.BackstageConstants.DEPLOYMENT_INSTALLERS_DIR
import co.bitshifted.appforge.backstage.BackstageConstants.JAR_EXTENSION
import co.bitshifted.appforge.backstage.BackstageConstants.OUTPUT_CLASSPATH_DIR
import co.bitshifted.appforge.backstage.BackstageConstants.OUTPUT_LAUNCHER_DIR
import co.bitshifted.appforge.backstage.BackstageConstants.OUTPUT_MODULES_DIR
import co.bitshifted.appforge.backstage.BackstageConstants.SYNCRO_JAR_NAME
import co.bitshifted.appforge.backstage.BackstageConstants.SYNCRO_PROPERTIES_FILE
import co.bitshifted.appforge.backstage.config.SyncroConfig
import co.bitshifted.appforge.backstage.exception.BackstageException
import co.bitshifted.appforge.backstage.exception.DeploymentException
import co.bitshifted.appforge.backstage.exception.ErrorInfo
import co.bitshifted.appforge.backstage.service.ContentService
import co.bitshifted.appforge.backstage.service.ReleaseService
import co.bitshifted.appforge.backstage.service.ResourceMapping
import co.bitshifted.appforge.backstage.util.logger
import co.bitshifted.appforge.common.model.CpuArch
import co.bitshifted.appforge.common.model.JavaVersion
import co.bitshifted.appforge.common.model.OperatingSystem
import freemarker.template.Configuration
import freemarker.template.TemplateExceptionHandler
import freemarker.template.Version
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.codec.digest.MessageDigestAlgorithms
import org.apache.commons.io.FileUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import java.io.File
import java.io.FileWriter
import java.net.URI
import java.nio.file.*
import java.util.*
import kotlin.io.path.absolutePathString
import kotlin.io.path.inputStream


open class DeploymentBuilder(val builderConfig: DeploymentBuilderConfig) {

    private val logger = logger(this)
    private val argumentsFilePath = "config/embed/args.txt"
    private val jvmOptsFilePath = "config/embed/jvmopts.txt"
    private val propsFilePath = "config/embed/jvmprops.txt"
    private val cpFilePath = "config/embed/classpath.txt"
    private val modulepathFilePath = "config/embed/modulepath.txt"
    private val jarFilePath = "config/embed/jar.txt"
    private val splashFilePath = "config/embed/splash.txt"
    private val moduleFilePath = "config/embed/module.txt"
    private val mainClassFilePath = "config/embed/mainclass.txt"
    private val addModulesFilePath = "config/embed/addmodules.txt"
    private val winIconPath = "icons/launchcode.ico"
    private val syncroPropertiesTemplate = "syncro.properties.ftl"

    val freemarkerConfig  = Configuration(Version(2,3,20))
    val digester = DigestUtils(MessageDigestAlgorithms.SHA_256)

    init {
       freemarkerConfig.defaultEncoding = "UTF-8"
        freemarkerConfig.locale = Locale.ENGLISH
        freemarkerConfig.templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER
        freemarkerConfig.setClassForTemplateLoading(DeploymentBuilder::class.java, "/templates")
    }

    @Autowired
    lateinit var resourceMapping: ResourceMapping
    @Autowired
    lateinit var contentService: ContentService
    @Autowired
    lateinit var releaseService : ReleaseService
    @Value("\${server.url}")
    lateinit var serverUrl : String
    @Value("\${deployment.builddir.keep:false}")
    var keepBuildDir : Boolean = false
    @Autowired
    lateinit var syncroConfig : SyncroConfig
    lateinit var launchCodeDir: Path
    private lateinit var linuxDir: Path
    private lateinit var windowsDir: Path
    private lateinit var macDir: Path
    lateinit var installerDir : Path

    fun build(): Boolean {
        try {
            val releaseId = releaseService.initRelease(builderConfig.deploymentConfig)
            createDirectoryStructure()
            setupSyncroJar(releaseId)
            buildLaunchers()
            val linuxBuilder = LinuxDeploymentBuilder(this)
            linuxBuilder.build()
            cacheDeploymentFiles(linuxDir)
            val windowsBuilder = WindowsDeploymentBuilder(this)
            windowsBuilder.build()
            cacheDeploymentFiles(windowsDir)
            val macBuilder = MacDeploymentBuilder(this)
            macBuilder.build()
            cacheDeploymentFiles(macDir)
            releaseService.completeRelease(builderConfig.baseDir, builderConfig.deploymentConfig, releaseId)
            logger.info("Deployment created successfully!")
        } catch (ex: Throwable) {
            logger.error("Failed to build deployment", ex)
            return false
        }
        if(!keepBuildDir) {
            logger.info("Deleting build directory ${builderConfig.baseDir.parent.absolutePathString()}")
            FileUtils.deleteDirectory(builderConfig.baseDir.parent.toFile())
        }
        return true
    }

    fun getLinuxDir(arch : CpuArch) : Path {
        return linuxDir.resolve(arch.display)
    }

    fun getMacDir(arch : CpuArch) : Path {
        return macDir.resolve(arch.display)
    }

    fun getWindowsDir(arch : CpuArch) : Path {
        return windowsDir.resolve(arch.display)
    }

    private fun createDirectoryStructure() {
        logger.debug("Creating directory structure for deployment in {}", builderConfig.baseDir.absolutePathString())
        launchCodeDir = Files.createDirectories(Paths.get(builderConfig.baseDir.absolutePathString(), OUTPUT_LAUNCHER_DIR))
        logger.debug("Created Launchcode output directory at {}", launchCodeDir.absolutePathString())
        linuxDir = Files.createDirectories(Paths.get(builderConfig.baseDir.absolutePathString(), OperatingSystem.LINUX.display))
        logger.debug("Created Linux output directory at {}", linuxDir.absolutePathString())
        val linuxCpuArchs = builderConfig.deploymentConfig.applicationInfo.linux.supportedCpuArchitectures
        if(linuxCpuArchs.contains(CpuArch.X64)) {
            Files.createDirectories(linuxDir.resolve(CpuArch.X64.display))
        }
        if(linuxCpuArchs.contains(CpuArch.AARCH64)) {
            Files.createDirectories(linuxDir.resolve(CpuArch.AARCH64.display))
        }
        windowsDir =
            Files.createDirectories(Paths.get(builderConfig.baseDir.absolutePathString(), OperatingSystem.WINDOWS.display))
        logger.debug("Created Windows output directory at {}", windowsDir.absolutePathString())
        Files.createDirectories(windowsDir.resolve(CpuArch.X64.display))
        macDir = Files.createDirectories(Paths.get(builderConfig.baseDir.absolutePathString(), OperatingSystem.MAC.display))
        logger.debug("Created Mac OS X output directory at {}", macDir.absolutePathString())
        val macCpuArchs = builderConfig.deploymentConfig.applicationInfo.linux.supportedCpuArchitectures
        if(macCpuArchs.contains(CpuArch.X64)) {
            Files.createDirectories(macDir.resolve(CpuArch.X64.display))
        }
        if(macCpuArchs.contains(CpuArch.AARCH64)) {
            Files.createDirectories(macDir.resolve(CpuArch.AARCH64.display))
        }
        installerDir = Files.createDirectories(Paths.get(builderConfig.baseDir.absolutePathString(), DEPLOYMENT_INSTALLERS_DIR))
        logger.debug("Created installers output directory at {}", installerDir.absolutePathString())
    }

    private fun setupSyncroJar(releaseId : String) {
        val jarLocation = resourceMapping.getSyncroJarLocation()
        val syncroJar = builderConfig.baseDir.resolve(SYNCRO_JAR_NAME)
        Files.copy(jarLocation.toURL().openStream(), syncroJar)
        logger.debug("Created syncro.jar with URI", syncroJar.toUri())
        // create properties file
        val data = mutableMapOf<String, String>()
        data["serverUrl"] = serverUrl
        data["applicationId"] = builderConfig.deploymentConfig.applicationId
        data["releaseId"] = releaseId
        val template = freemarkerConfig.getTemplate(syncroPropertiesTemplate)
        val propFile = builderConfig.baseDir.resolve(SYNCRO_PROPERTIES_FILE)
        val writer = FileWriter(propFile.toFile())
        writer.use {
            template.process(data, writer)
        }
        // update syncro.jar
        var syncroUri = syncroJar.toUri().toString()
        if (!syncroUri.toString().startsWith("jar:")) {
            syncroUri = "jar:${syncroUri.toString()}"
        }
        val zipFileSystem = FileSystems.newFileSystem(URI(syncroUri), mapOf("create" to  "true"))
        zipFileSystem.use {
            val internalFile = it.getPath("/${SYNCRO_PROPERTIES_FILE}")
            Files.copy(propFile, internalFile, StandardCopyOption.REPLACE_EXISTING)
        }
        logger.info("Successfully updated syncro.jar in path {}", syncroJar)
        val hash = digester.digestAsHex(syncroJar.toFile())
        logger.debug("syncro.jar hash: {}", hash)
        val syncroDep = syncroConfig.toJavaDependency(hash)
        contentService.save(syncroJar.inputStream())
        builderConfig.deploymentConfig.jvmConfiguration.dependencies.add(syncroDep)
    }

    fun copyDependencies(modulesDir: Path, classpathDir: Path, os : OperatingSystem, arch : CpuArch) {
        builderConfig.deploymentConfig.jvmConfiguration.collectDependencies(os)
            .filter { !it.isPlatformSpecific || (it.isPlatformSpecific && it.supportedOs == os && it.supportedCpuArch == arch) }
            .forEach {
            var targetDIr: Path
            if (it.isModular) {
                targetDIr = modulesDir
                logger.debug("Copying dependency {}:{}:{} to {}", it.groupId, it.artifactId, it.version, targetDIr)
            } else {
                targetDIr = classpathDir
            }
            val targetPath = Paths.get(targetDIr.toFile().absolutePath, it.sha256 + JAR_EXTENSION)
            builderConfig.contentService?.get(it.sha256 ?: throw BackstageException(ErrorInfo.EMPTY_CONTENT_CHECKSUM))?.input.use {
                Files.copy(it, targetPath, StandardCopyOption.REPLACE_EXISTING)
            }
        }
    }

    fun copyResources(baseDir: Path) {
        builderConfig.deploymentConfig.resources.forEach {
            val target = baseDir.resolve(it.target)
            logger.debug("Resource target: {}", target.toFile().absolutePath)
            // create directory structure
            Files.createDirectories(target.parent)
            builderConfig.contentService?.get(it.sha256 ?: throw BackstageException(ErrorInfo.EMPTY_CONTENT_CHECKSUM))?.input.use {
                Files.copy(it, target, StandardCopyOption.REPLACE_EXISTING)
            }
        }
        // copy license if present
        val license = builderConfig.deploymentConfig.applicationInfo.license
        if(license != null) {
            val target = baseDir.resolve(license.target)
            logger.debug("License target: {}", target.toFile().absolutePath)
            // create directory structure
            Files.createDirectories(target.parent)
            builderConfig.contentService?.get(license.sha256 ?: throw BackstageException(ErrorInfo.EMPTY_CONTENT_CHECKSUM))?.input.use {
                Files.copy(it, target, StandardCopyOption.REPLACE_EXISTING)
            }
        }
    }

    fun buildJdkImage(baseDir: Path, modulesDir: Path, os : OperatingSystem, arch : CpuArch) {
        logger.info("Building JDK image for {}", os.name)
        val jvmConfig = builderConfig.deploymentConfig.jvmConfiguration
        val jreOutputDir = baseDir.resolve(BackstageConstants.OUTPUT_JRE_DIR)
        val jdkLocation =
            resourceMapping.getJdkLocation(jvmConfig.vendor, jvmConfig.majorVersion, os, arch,jvmConfig.release ?: "latest")
        if(jvmConfig.majorVersion == JavaVersion.JAVA_8) {
            FileUtils.copyDirectory(Paths.get(jdkLocation).resolve("jre").toFile(), jreOutputDir.toFile())
        } else {
            val moduleDirs = listOf(Path.of(jdkLocation).resolve(BackstageConstants.JDK_JMODS_DIR_NAME), modulesDir)
            val toolRunner = ToolsRunner(baseDir, builderConfig.deploymentConfig.jvmConfiguration)
            val jdkModules = toolRunner.getJdkModules(builderConfig.deploymentConfig.jvmConfiguration.majorVersion)
            logger.debug("JDK modules to include: {}", jdkModules)
            if (jdkModules.isEmpty()) {
                throw DeploymentException("Failed to get any JDK module")
            }
            toolRunner.createRuntimeImage(jdkModules, moduleDirs, jreOutputDir)
        }
    }

    fun generateFromTemplate(templateLocation : String, target : Path, data: Map<String, Any>) {
        val template = freemarkerConfig.getTemplate(templateLocation)
        val writer = FileWriter(target.toFile())
        writer.use {
            template.process(data, writer)
        }
    }

    fun runExternalProgram(cmdLine : List<String>, workingDirectory : File, environment : Map<String, String> = emptyMap()) {
        val pb = ProcessBuilder(*cmdLine.toTypedArray())
        logger.debug("Running command: {} in working directory {}", pb.command(), workingDirectory.absolutePath)
        pb.directory(workingDirectory)
        pb.environment().putAll(environment)
        val process = pb.start()
        if (process.waitFor() == 0) {
            logger.info(process.inputReader().use { it.readText() })
            logger.info("Command executed successfully")
        } else {
            logger.error("Error encountered while running command. Details:")
            logger.error(process.inputReader().use { it.readText() })
            logger.error(process.errorReader().use { it.readText() })
            throw DeploymentException("Failed to run command: $cmdLine")
        }
    }

    private fun buildLaunchers() {
        val sourceRoot = resourceMapping.getLaunchcodeSourceLocation()
        logger.debug("Launchcode source location: {}", sourceRoot.toString())
        logger.debug("Copying Launchcode source to {}", launchCodeDir.absolutePathString())
        FileUtils.copyDirectory(File(sourceRoot), launchCodeDir.toFile())
        logger.debug("Finished copying Launchcode source")
        setupLauncherConfig()
        val pb = ProcessBuilder("make", "all")
        pb.directory(File(launchCodeDir.absolutePathString()))
        val path = System.getenv("PATH")
        val userHome = System.getProperty("user.home")
        pb.environment().put("PATH", "/usr/bin:/usr/local/bin:/usr/local/go/bin:$userHome/go/bin:/bin:/sbin")
        pb.environment().put("PWD", launchCodeDir.absolutePathString())

        println("PATH: $path")
        val process = pb.start()
        if (process.waitFor() == 0) {
            logger.info(process.inputReader().use { it.readText() })
            logger.info("Launchers created successfully")
        } else {
            logger.error("Error encountered while building launchers. Details:")
            logger.error(process.inputReader().use { it.readText() })
            logger.error(process.errorReader().use { it.readText() })
            throw DeploymentException("Failed to build launchers")
        }
    }

    private fun setupLauncherConfig() {
        logger.info("Configuring launcher")
        val argsPath = launchCodeDir.resolve(argumentsFilePath)
        Files.writeString(argsPath, builderConfig.deploymentConfig.jvmConfiguration.arguments ?: "")
        val jvmOptsPath = launchCodeDir.resolve(jvmOptsFilePath)
        Files.writeString(jvmOptsPath, builderConfig.deploymentConfig.jvmConfiguration.jvmOptions ?: "")
        val propsFile = launchCodeDir.resolve(propsFilePath)
        Files.writeString(propsFile, builderConfig.deploymentConfig.jvmConfiguration.systemProperties ?: "")
        val cpFilePath = launchCodeDir.resolve(cpFilePath)
        val cpValue = String.format("%s/*", OUTPUT_CLASSPATH_DIR)
        Files.writeString(cpFilePath, cpValue)
        if (builderConfig.deploymentConfig.jvmConfiguration.majorVersion != JavaVersion.JAVA_8) {
            val modulesPathDir = launchCodeDir.resolve(modulepathFilePath)
            Files.writeString(modulesPathDir, OUTPUT_MODULES_DIR)
        }
        val addModulesFile = launchCodeDir.resolve(addModulesFilePath)
        Files.writeString(addModulesFile, builderConfig.deploymentConfig.jvmConfiguration.addModules ?: "")

        val jarPath = launchCodeDir.resolve(jarFilePath)
        Files.writeString(jarPath, builderConfig.deploymentConfig.jvmConfiguration.jar ?: "")
        if (builderConfig.deploymentConfig.applicationInfo.splashScreen != null) {
            val splashPath = launchCodeDir.resolve(splashFilePath)
            Files.writeString(splashPath, "-splash:${builderConfig.deploymentConfig.applicationInfo.splashScreen.target ?: builderConfig.deploymentConfig.applicationInfo.splashScreen.source}")
        }
        val modulePath = launchCodeDir.resolve(moduleFilePath)
        Files.writeString(modulePath, builderConfig.deploymentConfig.jvmConfiguration.moduleName ?: "")
        val mainClassPath = launchCodeDir.resolve(mainClassFilePath)
        Files.writeString(mainClassPath, builderConfig.deploymentConfig.jvmConfiguration.mainClass ?: "")
        // copy windows icon
        val winIcons = builderConfig.deploymentConfig.applicationInfo.windows.icons
        if (winIcons.size > 0) {
            val icon = winIcons[0]
            val iconTarget = launchCodeDir.resolve(winIconPath)
            logger.debug("Windows icon target: {}", iconTarget.absolutePathString())
            builderConfig.contentService?.get(icon.sha256 ?: throw BackstageException(ErrorInfo.EMPTY_CONTENT_CHECKSUM))?.input.use {
                Files.copy(it, iconTarget, StandardCopyOption.REPLACE_EXISTING)
            }
        }
    }

    private fun cacheDeploymentFiles(baseDir : Path) {
        logger.info("Caching deployment files for directory {}", baseDir.absolutePathString())
        val fileList = FileUtils.listFiles(baseDir.toFile(), null, true)
        fileList.stream().forEach {
            val hash = digester.digestAsHex(it)
            val size = it.length()
            logger.debug("Checking if {} exists", it.absolutePath)
            if(contentService.exists(hash, size)) {
                logger.debug("File is already cached")

            } else {
                logger.debug("File {} is not cached, caching it", it.absolutePath)
                contentService.save(it.inputStream(), it.canExecute())
            }
        }
    }

}