/*
 * Copyright 2012-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.start.site.extension.dependency.springnative;

import java.util.function.Supplier;

import io.spring.initializr.generator.buildsystem.Dependency;
import io.spring.initializr.generator.buildsystem.gradle.GradleBuild;
import io.spring.initializr.generator.spring.build.BuildCustomizer;
import io.spring.initializr.generator.version.VersionReference;

/**
 * A {@link BuildCustomizer} that configures Spring Native for Gradle using the Groovy
 * DSL.
 *
 * @author Stephane Nicoll
 */
class SpringNativeGroovyDslGradleBuildCustomizer extends SpringNativeGradleBuildCustomizer {

	private final Supplier<VersionReference> hibernateVersion;

	SpringNativeGroovyDslGradleBuildCustomizer(Supplier<VersionReference> hibernateVersion) {
		this.hibernateVersion = hibernateVersion;
	}

	@Override
	public void customize(GradleBuild build) {
		super.customize(build);

		// Hibernate enhance plugin
		if (build.dependencies().has("data-jpa")) {
			configureHibernateEnhancePlugin(build);
		}
	}

	@Override
	protected void customizeSpringBootPlugin(GradleBuild build) {
		build.tasks().customize("bootBuildImage", (task) -> {
			task.attribute("builder", "'paketobuildpacks/builder:tiny'");
			task.attribute("environment", "['BP_NATIVE_IMAGE': 'true']");
		});
	}

	private void configureHibernateEnhancePlugin(GradleBuild build) {
		build.settings().mapPlugin("org.hibernate.orm",
				Dependency.withCoordinates("org.hibernate", "hibernate-gradle-plugin")
						.version(this.hibernateVersion.get()).build());
		build.plugins().add("org.hibernate.orm");
		build.tasks().customize("hibernate", (task) -> task.nested("enhance", (enhance) -> {
			enhance.attribute("enableLazyInitialization", "true");
			enhance.attribute("enableDirtyTracking", "true");
			enhance.attribute("enableAssociationManagement", "true");
			enhance.attribute("enableExtendedEnhancement", "false");
		}));
	}

}
