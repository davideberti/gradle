/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.integtests.tooling.r112

import org.gradle.integtests.tooling.fixture.TargetGradleVersion
import org.gradle.integtests.tooling.fixture.ToolingApiSpecification
import org.gradle.integtests.tooling.fixture.ToolingApiVersion
import org.gradle.tooling.BuildLauncher

@ToolingApiVersion(">=1.2")
@TargetGradleVersion(">=1.0-milestone-8")
class UserHomeDirCrossVersionSpec extends ToolingApiSpecification {
    def "build is executed using specified user home directory"() {
        File userHomeDir = temporaryFolder.createDir('userhomedir')
        projectDir.file('settings.gradle') << 'rootProject.name="test"'
        projectDir.file('build.gradle') << """task gradleBuild << {
    logger.lifecycle 'userHomeDir=' + gradle.gradleUserHomeDir
}
"""
        ByteArrayOutputStream baos = new ByteArrayOutputStream()

        when:
        toolingApi.withConnector { connector ->
            connector.useGradleUserHomeDir(userHomeDir)
        }
        // TODO radim: consider using smaller heap and shorter timeout when applicable to all supported versions
        toolingApi.withConnection { connection ->
            BuildLauncher build = connection.newBuild();
            build.forTasks("gradleBuild");
            build.standardOutput = baos
            build.run()
        }
        def output = baos.toString("UTF-8")

        then:
        output.contains('userHomeDir=' + userHomeDir.absolutePath)
    }
}
