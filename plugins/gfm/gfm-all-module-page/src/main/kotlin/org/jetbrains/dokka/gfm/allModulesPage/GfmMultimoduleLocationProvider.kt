package org.jetbrains.dokka.gfm.allModulesPage

import org.jetbrains.dokka.base.resolvers.local.DokkaBaseLocationProvider
import org.jetbrains.dokka.base.resolvers.local.DokkaLocationProvider
import org.jetbrains.dokka.base.resolvers.local.LocationProviderFactory
import org.jetbrains.dokka.gfm.location.MarkdownLocationProvider
import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.DisplaySourceSet
import org.jetbrains.dokka.pages.PageNode
import org.jetbrains.dokka.pages.RootPageNode
import org.jetbrains.dokka.plugability.DokkaContext

class GfmMultimoduleLocationProvider(private val root: RootPageNode, context: DokkaContext) : DokkaBaseLocationProvider(root, context, ".md") {

    private val defaultLocationProvider = MarkdownLocationProvider(root, context)

    val paths = context.configuration.modules.map {
        it.name to it.relativePathToOutputDirectory
    }.toMap()

    override fun resolve(dri: DRI, sourceSets: Set<DisplaySourceSet>, context: PageNode?) =
        dri.takeIf { it.packageName == MULTIMODULE_PACKAGE_PLACEHOLDER }?.classNames?.let { paths[it] }?.let {
            "$it/${DokkaLocationProvider.identifierToFilename(dri.classNames.orEmpty())}/index.md"
        } ?: defaultLocationProvider.resolve(dri, sourceSets, context)

    override fun resolve(node: PageNode, context: PageNode?, skipExtension: Boolean) =
        defaultLocationProvider.resolve(node, context, skipExtension)

    override fun pathToRoot(from: PageNode): String = defaultLocationProvider.pathToRoot(from)

    override fun ancestors(node: PageNode): List<PageNode> = listOf(root)

    companion object {
        const val MULTIMODULE_PACKAGE_PLACEHOLDER = ".ext"
    }

    class Factory(private val context: DokkaContext): LocationProviderFactory {
        override fun getLocationProvider(pageNode: RootPageNode) =
            GfmMultimoduleLocationProvider(pageNode, context)
    }
}