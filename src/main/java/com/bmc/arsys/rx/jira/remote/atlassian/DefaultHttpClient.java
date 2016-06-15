package com.bmc.arsys.rx.jira.remote.atlassian;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.httpclient.apache.httpcomponents.DefaultRequest;
import com.atlassian.httpclient.apache.httpcomponents.DefaultResponse;
import com.atlassian.httpclient.apache.httpcomponents.FlushableHttpCacheStorage;
import com.atlassian.httpclient.apache.httpcomponents.ProxySelectorAsyncRoutePlanner;
import com.atlassian.httpclient.api.*;
import com.atlassian.httpclient.api.factory.HttpClientOptions;
import com.atlassian.httpclient.base.AbstractHttpClient;
import com.atlassian.httpclient.base.RequestKiller;
import com.atlassian.httpclient.base.event.HttpRequestCompletedEvent;
import com.atlassian.httpclient.base.event.HttpRequestFailedEvent;
import com.atlassian.httpclient.spi.ThreadLocalContextManager;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.util.concurrent.ThreadFactories;
import com.google.common.base.Function;
import com.google.common.base.Throwables;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpAsyncClient;
import org.apache.http.impl.nio.client.DefaultHttpAsyncClient;
import org.apache.http.impl.nio.conn.AsyncSchemeRegistryFactory;
import org.apache.http.impl.nio.conn.PoolingClientAsyncConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.nio.conn.scheme.AsyncSchemeRegistry;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.nio.reactor.IOReactorExceptionHandler;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ProxySelector;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static com.atlassian.util.concurrent.Promises.rejected;
import static com.google.common.base.Preconditions.checkNotNull;

public final class DefaultHttpClient<C> extends AbstractHttpClient implements HttpClient, DisposableBean
{
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final EventPublisher eventPublisher;
    private final ApplicationProperties applicationProperties;
    private final ThreadLocalContextManager<C> threadLocalContextManager;
    private final ExecutorService callbackExecutor;
    private final HttpClientOptions httpClientOptions;

    private final RequestKiller requestKiller;
    private final HttpAsyncClient httpClient;
    private final HttpAsyncClient nonCachingHttpClient;
    private final FlushableHttpCacheStorage httpCacheStorage;

    public DefaultHttpClient(EventPublisher eventPublisher, ApplicationProperties applicationProperties, ThreadLocalContextManager<C> threadLocalContextManager)
    {
        this(eventPublisher, applicationProperties, threadLocalContextManager, new HttpClientOptions(), AsyncSchemeRegistryFactory.createDefault());
    }

    public DefaultHttpClient(EventPublisher eventPublisher,
                             ApplicationProperties applicationProperties,
                             ThreadLocalContextManager<C> threadLocalContextManager,
                             final HttpClientOptions options,
                             AsyncSchemeRegistry asyncSchemeRegistry)
    {
        this.eventPublisher = checkNotNull(eventPublisher);
        this.applicationProperties = checkNotNull(applicationProperties);
        this.threadLocalContextManager = checkNotNull(threadLocalContextManager);
        this.httpClientOptions = checkNotNull(options);
        this.requestKiller = new RequestKiller(options.getThreadPrefix());

        final DefaultHttpAsyncClient client;
        try
        {
            IOReactorConfig ioReactorConfig = new IOReactorConfig();
            ioReactorConfig.setIoThreadCount(options.getIoThreadCount());
            ioReactorConfig.setSelectInterval(options.getIoSelectInterval());
            ioReactorConfig.setInterestOpQueued(true);
            DefaultConnectingIOReactor reactor = new DefaultConnectingIOReactor(
                    ioReactorConfig,
                    ThreadFactories.namedThreadFactory(options.getThreadPrefix() + "-io", ThreadFactories.Type.DAEMON));
            reactor.setExceptionHandler(new IOReactorExceptionHandler()
            {
                @Override
                public boolean handle(IOException ex)
                {
                    log.error("IO exception in reactor", ex);
                    return false;
                }

                @Override
                public boolean handle(RuntimeException ex)
                {
                    log.error("Fatal runtime error", ex);
                    return false;
                }
            });
            final PoolingClientAsyncConnectionManager connmgr = new PoolingClientAsyncConnectionManager(reactor,
                    asyncSchemeRegistry, options.getConnectionPoolTimeToLive(), TimeUnit.MILLISECONDS)
            {
                @Override
                protected void finalize() throws Throwable
                {
                    // prevent the PoolingClientAsyncConnectionManager from logging - this causes exceptions due to
                    // the ClassLoader probably having been removed when the plugin shuts down.  Added a
                    // PluginEventListener to make sure the shutdown method is called while the plugin classloader
                    // is still active.
                }
            };

            connmgr.setDefaultMaxPerRoute(options.getMaxConnectionsPerHost());

            client = new DefaultHttpAsyncClient(connmgr);
        }
        catch (IOReactorException e)
        {
            throw new RuntimeException("Reactor " + options.getThreadPrefix() + "not set up correctly", e);
        }

        HttpParams params = client.getParams();
        // @todo add plugin version to UA string
        HttpProtocolParams.setUserAgent(params, getUserAgent(options));

        HttpConnectionParams.setConnectionTimeout(params, (int) options.getConnectionTimeout());
        HttpConnectionParams.setSoTimeout(params, (int) options.getSocketTimeout());
        HttpConnectionParams.setSocketBufferSize(params, 8 * 1024);
        HttpConnectionParams.setTcpNoDelay(params, true);

        ProxySelectorAsyncRoutePlanner routePlanner = new ProxySelectorAsyncRoutePlanner(
                client.getConnectionManager().getSchemeRegistry(),
                ProxySelector.getDefault());
        client.setRoutePlanner(routePlanner);

        CacheConfig cacheConfig = new CacheConfig();
        cacheConfig.setMaxCacheEntries(options.getMaxCacheEntries());
        cacheConfig.setSharedCache(false);
        cacheConfig.setMaxObjectSize(options.getMaxCacheObjectSize());
        cacheConfig.setNeverCache1_0ResponsesWithQueryString(false);

        this.nonCachingHttpClient = client;
        this.httpCacheStorage = new FlushableHttpCacheStorage(cacheConfig);
        httpClient = new CachingHttpAsyncClient(client, httpCacheStorage, cacheConfig);

        callbackExecutor = httpClientOptions.getCallbackExecutor();
        httpClient.start();
        requestKiller.start();
    }

    private String getUserAgent(HttpClientOptions options)
    {
        return String.format("Atlassian HttpClient %s / %s-%s (%s) / %s",
                AsynchronousHttpClientFactory.MavenUtils.getVersion("com.atlassian.jira", "jira-rest-java-client-core"),
                applicationProperties.getDisplayName(),
                applicationProperties.getVersion(),
                applicationProperties.getBuildNumber(),
                options.getUserAgent());
    }

    @Override
    public final ResponsePromise execute(final DefaultRequest request)
    {
        try
        {
            return doExecute(request);
        }
        catch (Throwable t)
        {
            return ResponsePromises.toResponsePromise(rejected(t, Response.class));
        }
    }

    private ResponsePromise doExecute(final DefaultRequest request)
    {
        httpClientOptions.getRequestPreparer().apply(request);

        // validate the request state
        request.validate();

        // freeze the request state to prevent further mutability as we go to execute the request
        freeze(request);

        final long start = System.currentTimeMillis();
        final HttpRequestBase op;
        final String uri = request.getUri().toString();
        final Request.Method method = request.getMethod();
        switch (method)
        {
            case GET:
                op = new HttpGet(uri);
                break;
            case POST:
                op = new HttpPost(uri);
                break;
            case PUT:
                op = new HttpPut(uri);
                break;
            case DELETE:
                op = new HttpDelete(uri);
                break;
            case OPTIONS:
                op = new HttpOptions(uri);
                break;
            case HEAD:
                op = new HttpHead(uri);
                break;
            case TRACE:
                op = new HttpTrace(uri);
                break;
            default:
                throw new UnsupportedOperationException(method.toString());
        }
        if (request.hasEntity())
        {
            if (op instanceof HttpEntityEnclosingRequestBase)
            {
                ((HttpEntityEnclosingRequestBase) op).setEntity(getHttpEntity(request));
            }
            else
            {
                throw new UnsupportedOperationException("HTTP method " + method + " does not support sending an entity");
            }
        }

        for (Map.Entry<String, String> entry : request.getHeaders().entrySet())
        {
            op.setHeader(entry.getKey(), entry.getValue());
        }

        requestKiller.registerRequest(op, httpClientOptions.getRequestTimeout());
        return ResponsePromises.toResponsePromise(getPromiseHttpAsyncClient(request).execute(op, new BasicHttpContext()).fold(
                new Function<Throwable, Response>()
                {
                    @Override
                    public Response apply(Throwable ex)
                    {
                        requestKiller.completedRequest(op);
                        final long requestDuration = System.currentTimeMillis() - start;
                        publishEvent(request, requestDuration, ex);
                        throw Throwables.propagate(ex);
                    }
                },
                new Function<HttpResponse, Response>()
                {
                    @Override
                    public Response apply(HttpResponse httpResponse)
                    {
                        requestKiller.completedRequest(op);
                        final long requestDuration = System.currentTimeMillis() - start;
                        publishEvent(request, requestDuration, httpResponse.getStatusLine().getStatusCode());
                        try
                        {
                            return freeze(translate(httpResponse));
                        }
                        catch (IOException e)
                        {
                            throw Throwables.propagate(e);
                        }
                    }
                }
        ));
    }

    private void publishEvent(Request request, long requestDuration, int statusCode)
    {
        if (HttpStatus.OK.code <= statusCode && statusCode < HttpStatus.MULTIPLE_CHOICES.code)
        {
            eventPublisher.publish(new HttpRequestCompletedEvent(
                    request.getUri().toString(),
                    request.getMethod().name(),
                    statusCode,
                    requestDuration,
                    request.getAttributes()));
        }
        else
        {
            eventPublisher.publish(new HttpRequestFailedEvent(
                    request.getUri().toString(),
                    request.getMethod().name(),
                    statusCode,
                    requestDuration,
                    request.getAttributes()));
        }
    }

    private void publishEvent(Request request, long requestDuration, Throwable ex)
    {
        eventPublisher.publish(new HttpRequestFailedEvent(
                request.getUri().toString(),
                request.getMethod().name(),
                ex.toString(),
                requestDuration,
                request.getAttributes()));
    }

    private PromiseHttpAsyncClient getPromiseHttpAsyncClient(DefaultRequest request)
    {
        return new SettableFuturePromiseHttpPromiseAsyncClient<C>(request.isCacheDisabled() ? nonCachingHttpClient : httpClient, threadLocalContextManager, callbackExecutor);
    }

    private DefaultResponse translate(HttpResponse httpResponse) throws IOException
    {
        StatusLine status = httpResponse.getStatusLine();
        DefaultResponse response = new DefaultResponse(httpClientOptions.getMaxEntitySize());
        response.setStatusCode(status.getStatusCode());
        response.setStatusText(status.getReasonPhrase());
        Header[] httpHeaders = httpResponse.getAllHeaders();
        for (Header httpHeader : httpHeaders)
        {
            response.setHeader(httpHeader.getName(), httpHeader.getValue());
        }
        final HttpEntity entity = httpResponse.getEntity();
        if (entity != null)
        {
            response.setEntityStream(entity.getContent());
        }
        return response;
    }

    @Override
    public void destroy() throws Exception
    {
        callbackExecutor.shutdown();
        requestKiller.stop();
        httpClient.shutdown();
    }

    @Override
    public void flushCacheByUriPattern(Pattern urlPattern)
    {
        httpCacheStorage.flushByUriPattern(urlPattern);
    }


    private static <T> T freeze(T o) {
        try {
            Method freeze = o.getClass().getDeclaredMethod("freeze");
            freeze.setAccessible(true);
            freeze.invoke(o);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return o;
    }

    private static HttpEntity getHttpEntity(DefaultRequest request) {
        try {
            Method getHttpEntity;
            try {
                getHttpEntity = request.getClass().getDeclaredMethod("getHttpEntity");
            } catch (NoSuchMethodException e) {
                getHttpEntity = request.getClass().getSuperclass().getDeclaredMethod("getHttpEntity");
            }
            getHttpEntity.setAccessible(true);
            return (HttpEntity) getHttpEntity.invoke(request);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
