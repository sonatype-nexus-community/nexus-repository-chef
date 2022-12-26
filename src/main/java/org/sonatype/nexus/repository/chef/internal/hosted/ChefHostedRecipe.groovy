package org.sonatype.nexus.repository.chef.internal.hosted

import org.sonatype.nexus.repository.Format
import org.sonatype.nexus.repository.RecipeSupport
import org.sonatype.nexus.repository.Repository
import org.sonatype.nexus.repository.Type
import org.sonatype.nexus.repository.attributes.AttributesFacet
import org.sonatype.nexus.repository.cache.NegativeCacheFacet
import org.sonatype.nexus.repository.cache.NegativeCacheHandler
import org.sonatype.nexus.repository.chef.internal.AssetKind
import org.sonatype.nexus.repository.chef.internal.ChefFormat
import org.sonatype.nexus.repository.chef.internal.security.ChefSecurityFacet
import org.sonatype.nexus.repository.chef.internal.supermarket.SupermarketJsonArtifactsFacet
import org.sonatype.nexus.repository.content.search.SearchFacet
import org.sonatype.nexus.repository.http.HttpHandlers
import org.sonatype.nexus.repository.http.PartialFetchHandler
import org.sonatype.nexus.repository.httpclient.HttpClientFacet
import org.sonatype.nexus.repository.purge.PurgeUnusedFacet
import org.sonatype.nexus.repository.security.SecurityHandler
import org.sonatype.nexus.repository.storage.DefaultComponentMaintenanceImpl
import org.sonatype.nexus.repository.storage.StorageFacet
import org.sonatype.nexus.repository.storage.UnitOfWorkHandler
import org.sonatype.nexus.repository.types.HostedType
import org.sonatype.nexus.repository.view.ConfigurableViewFacet
import org.sonatype.nexus.repository.view.ViewFacet
import org.sonatype.nexus.repository.view.Router
import org.sonatype.nexus.repository.view.Route
import org.sonatype.nexus.repository.view.Context
import org.sonatype.nexus.repository.view.handlers.ExceptionHandler
import org.sonatype.nexus.repository.view.handlers.TimingHandler
import org.sonatype.nexus.repository.view.handlers.ConditionalRequestHandler
import org.sonatype.nexus.repository.view.handlers.ContentHeadersHandler
import org.sonatype.nexus.repository.view.handlers.HandlerContributor
import org.sonatype.nexus.repository.view.matchers.ActionMatcher
import org.sonatype.nexus.repository.view.matchers.RegexMatcher
import org.sonatype.nexus.repository.view.matchers.logic.LogicMatchers
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher

import javax.annotation.Nonnull
import javax.annotation.Priority
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider
import javax.inject.Singleton

import static org.sonatype.nexus.repository.http.HttpMethods.PUT
import static org.sonatype.nexus.repository.http.HttpMethods.GET
import static org.sonatype.nexus.repository.http.HttpMethods.HEAD

@Named(ChefHostedRecipe.NAME)
@Priority(Integer.MAX_VALUE)
@Singleton
class ChefHostedRecipe
        extends RecipeSupport {
    public static final String NAME = 'chef-hosted'

    public static final String VERSION_TOKEN = 'version'

    public static final String NAME_TOKEN = 'name'

    @Inject
    Provider<ChefHostedFacet> hostedFacet

    @Inject
    Provider<ChefContentFacet> contentFacet

    @Inject
    Provider<SupermarketJsonArtifactsFacet> supermarketJsonArtifactsFacet

    @Inject
    Provider<ChefSecurityFacet> securityFacet

    @Inject
    Provider<ConfigurableViewFacet> viewFacet

    @Inject
    Provider<StorageFacet> storageFacet

    @Inject
    Provider<SearchFacet> searchFacet

    @Inject
    Provider<AttributesFacet> attributesFacet

    @Inject
    ExceptionHandler exceptionHandler

    @Inject
    TimingHandler timingHandler

    @Inject
    SecurityHandler securityHandler

    @Inject
    PartialFetchHandler partialFetchHandler

    @Inject
    ConditionalRequestHandler conditionalRequestHandler

    @Inject
    ContentHeadersHandler contentHeadersHandler

    @Inject
    UnitOfWorkHandler unitOfWorkHandler

    @Inject
    HandlerContributor handlerContributor

    @Inject
    Provider<DefaultComponentMaintenanceImpl> componentMaintenanceFacet

    @Inject
    Provider<HttpClientFacet> httpClientFacet

    @Inject
    Provider<PurgeUnusedFacet> purgeUnusedFacet

    @Inject
    Provider<NegativeCacheFacet> negativeCacheFacet

    @Inject
    NegativeCacheHandler negativeCacheHandler

    @Inject
    ChefHostedDownloadHandler downloadHandler

    @Inject
    ChefHostedRecipe(@Named(HostedType.NAME) final Type type,
                     @Named(ChefFormat.NAME) final Format format) {
        super(type, format)
    }

    @Override
    void apply(@Nonnull final Repository repository) throws Exception {
        repository.attach(storageFacet.get())
        repository.attach(contentFacet.get())
        repository.attach(supermarketJsonArtifactsFacet.get())
        repository.attach(securityFacet.get())
        repository.attach(configure(viewFacet.get()))
        repository.attach(componentMaintenanceFacet.get())
        repository.attach(hostedFacet.get())
        repository.attach(searchFacet.get())
        repository.attach(attributesFacet.get())
    }

    /**
     * Configure {@link ViewFacet}.
     */
    private ViewFacet configure(final ConfigurableViewFacet facet) {
        Router.Builder builder = new Router.Builder()

        builder.route(tarballMatcher()
                .handler(timingHandler)
                .handler(assetKindHandler.rcurry(AssetKind.COOKBOOK))
                .handler(securityHandler)
                .handler(exceptionHandler)
                .handler(handlerContributor)
                .handler(conditionalRequestHandler)
                .handler(partialFetchHandler)
                .handler(contentHeadersHandler)
                .handler(unitOfWorkHandler)
                .handler(downloadHandler)
                .create())

        builder.route(supermarketUniverseMatcher()
                .handler(timingHandler)
                .handler(assetKindHandler.rcurry(AssetKind.UNIVERSE_JSON))
                .handler(securityHandler)
                .handler(exceptionHandler)
                .handler(handlerContributor)
                .handler(conditionalRequestHandler)
                .handler(partialFetchHandler)
                .handler(contentHeadersHandler)
                .handler(unitOfWorkHandler)
                .handler(downloadHandler)
                .create())

        builder.route(supermarketCookbookVersionMatcher()
                .handler(timingHandler)
                .handler(assetKindHandler.rcurry(AssetKind.COOKBOOK_VERSION_INFO))
                .handler(securityHandler)
                .handler(exceptionHandler)
                .handler(handlerContributor)
                .handler(conditionalRequestHandler)
                .handler(partialFetchHandler)
                .handler(contentHeadersHandler)
                .handler(unitOfWorkHandler)
                .handler(downloadHandler)
                .create())

        builder.route(supermarketCookbookInfoMatcher()
                .handler(timingHandler)
                .handler(assetKindHandler.rcurry(AssetKind.COOKBOOK_INFO))
                .handler(securityHandler)
                .handler(exceptionHandler)
                .handler(handlerContributor)
                .handler(conditionalRequestHandler)
                .handler(partialFetchHandler)
                .handler(contentHeadersHandler)
                .handler(unitOfWorkHandler)
                .handler(downloadHandler)
                .create())

        builder.route(uploadMatcher()
                .handler(timingHandler)
                .handler(assetKindHandler.rcurry(AssetKind.COOKBOOK))
                .handler(securityHandler)
                .handler(exceptionHandler)
                .handler(handlerContributor)
                .handler(conditionalRequestHandler)
                .handler(partialFetchHandler)
                .handler(contentHeadersHandler)
                .handler(unitOfWorkHandler)
                .create())

        addBrowseUnsupportedRoute(builder)

        builder.defaultHandlers(HttpHandlers.notFound())

        facet.configure(builder.create())

        return facet
    }

    Closure assetKindHandler = { Context context, AssetKind value ->
        context.attributes.set(AssetKind, value)
        return context.proceed()
    }

    static Route.Builder tarballMatcher() {
        new Route.Builder().matcher(
                LogicMatchers.and(
                        new ActionMatcher(GET, HEAD),
                        LogicMatchers.or(
                                new TokenMatcher(String.format("/{%s:.+}/{%s:.+}/{filename:.+}.tgz", NAME_TOKEN, VERSION_TOKEN)),
                                new TokenMatcher("/api/v1/cookbooks/{name:.+}/versions/{version:.+}/download")
                        )
                ))
    }

    static Route.Builder supermarketUniverseMatcher() {
        new Route.Builder().matcher(
                LogicMatchers.and(
                        new ActionMatcher(GET, HEAD),
                        new RegexMatcher('/universe[.json]*')
                ))
    }

    static Route.Builder supermarketCookbookVersionMatcher() {
        new Route.Builder().matcher(
                LogicMatchers.and(
                        new ActionMatcher(GET, HEAD),
                        LogicMatchers.or(
                                new TokenMatcher("/api/v1/cookbooks/{name:.+}/versions/{version:.+}"),
                                new TokenMatcher("/{name:.+}/{version:.+}/cookbook_version_info.json")
                        )
                ))
    }

    static Route.Builder supermarketCookbookInfoMatcher() {
        new Route.Builder().matcher(
                LogicMatchers.and(
                        new ActionMatcher(GET, HEAD),
                        LogicMatchers.or(
                                new TokenMatcher("/api/v1/cookbooks/{name:.+}"),
                                new TokenMatcher("/{name:.+}/cookbook_info.json")
                        )
                ))
    }

    static Route.Builder uploadMatcher() {
        new Route.Builder().matcher(
                LogicMatchers.and(
                        new ActionMatcher(PUT),
                        new TokenMatcher(String.format("/cookbooks/upload/{%s:.+}/{%s:.+}", NAME_TOKEN, VERSION_TOKEN))
                ))
    }
}
