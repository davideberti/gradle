/*
 * Copyright 2014 the original author or authors.
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
package org.gradle.runtime.jvm.plugins;

import org.gradle.api.*;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.runtime.base.BinaryContainer;
import org.gradle.runtime.base.SoftwareComponentContainer;
import org.gradle.runtime.base.internal.DefaultBinaryNamingSchemeBuilder;
import org.gradle.language.base.plugins.LanguageBasePlugin;
import org.gradle.language.base.plugins.LifecycleBasePlugin;
import org.gradle.runtime.jvm.JvmLibrary;
import org.gradle.runtime.jvm.internal.DefaultJvmLibrary;
import org.gradle.runtime.jvm.internal.JvmLibraryBinaryInternal;
import org.gradle.runtime.jvm.internal.plugins.CreateJvmBinaries;
import org.gradle.runtime.jvm.internal.plugins.CreateTasksForJvmBinaries;
import org.gradle.model.ModelRule;
import org.gradle.model.ModelRules;

import javax.inject.Inject;

/**
 * Base plugin for JVM component support. Applies the {@link org.gradle.language.base.plugins.LanguageBasePlugin}.
 * Registers the {@link org.gradle.runtime.jvm.JvmLibrary} library type for the {@link org.gradle.runtime.base.SoftwareComponentContainer}.
 */
@Incubating
public class JvmComponentPlugin implements Plugin<Project> {
    private final ModelRules modelRules;

    @Inject
    public JvmComponentPlugin(ModelRules modelRules) {
        this.modelRules = modelRules;
    }

    public void apply(final Project project) {
        project.getPlugins().apply(LifecycleBasePlugin.class);
        project.getPlugins().apply(LanguageBasePlugin.class);

        SoftwareComponentContainer softwareComponents = project.getExtensions().getByType(SoftwareComponentContainer.class);
        softwareComponents.registerFactory(JvmLibrary.class, new NamedDomainObjectFactory<JvmLibrary>() {
            public JvmLibrary create(String name) {
                return new DefaultJvmLibrary(name);
            }
        });
        NamedDomainObjectContainer<JvmLibrary> jvmLibraries = softwareComponents.containerWithType(JvmLibrary.class);
        project.getExtensions().add("jvmLibraries", jvmLibraries);

        modelRules.register("jvmLibraries", jvmLibraries);
        modelRules.rule(new CreateJvmBinaries(new DefaultBinaryNamingSchemeBuilder()));
        modelRules.rule(new CreateTasksForJvmBinaries());
        modelRules.rule(new AttachBinariesToLifecycle());
    }

    // TODO:DAZ Push this down to LanguageBasePlugin (but first need to deal with ClassDirectoryBinary)
    private static class AttachBinariesToLifecycle extends ModelRule {
        void attach(TaskContainer tasks, BinaryContainer binaries) {
            Task assembleTask = tasks.getByName(LifecycleBasePlugin.ASSEMBLE_TASK_NAME);
            for (JvmLibraryBinaryInternal jvmLibraryBinary : binaries.withType(JvmLibraryBinaryInternal.class)) {
                assembleTask.dependsOn(jvmLibraryBinary);
            }
        }
    }
}
