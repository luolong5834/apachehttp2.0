package luolong.h2c;

import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.async.methods.SimpleRequestProducer;
import org.apache.hc.client5.http.async.methods.SimpleResponseConsumer;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.async.MinimalHttpAsyncClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.nio.AsyncClientEndpoint;
import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.apache.hc.core5.http2.config.H2Config;
import org.apache.hc.core5.io.ShutdownType;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.util.Timeout;
import java.io.IOException;
import java.util.concurrent.*;


/**
 * <p></p>
 * h2c的demo的示例，详细请查看HTTP2官网
 * @author luolong
 * @date 2018/10/11
 */
public class TestH2c {
    /**
     * http1.1
     *
     * @throws IOException
     */
    static void requestForHttp1() throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet("http://localhost:8441/http2.0-1.0-SNAPSHOT/test/test");
        CloseableHttpResponse response1 = httpclient.execute(httpGet);
// The underlying HTTP connection is still held by the response object
// to allow the response content to be streamed directly from the network socket.
// In order to ensure correct deallocation of system resources
// the user MUST call CloseableHttpResponse#close() from a finally clause.
// Please note that if response content is not fully consumed the underlying
// connection cannot be safely re-used and will be shut down and discarded
// by the connection manager.
        try {
            System.out.println("http version:" + response1.getVersion());
            HttpEntity entity1 = response1.getEntity();
            // do something useful with the response body
            // and ensure it is fully consumed
            EntityUtils.consume(entity1);
        } finally {
            response1.close();
        }
    }

    /**
     * http2 示例
     * 控制台会分步打印客户端和服务端交互的信息,
     * ">>"代表是send
     * "<<"代表是receive
     * "frame"代表是帧
     * 请求的最后结果是test success,结果数据在data frame帧里面,data代表的帧的类型
     * This example demonstrates concurrent (multiplexed) execution of multiple
     * HTTP/2 message exchanges.
     */
    static void requestForHttp2() throws InterruptedException, ExecutionException, TimeoutException {
        final IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                .setSoTimeout(Timeout.ofSeconds(5))
                .build();

        final MinimalHttpAsyncClient client = HttpAsyncClients.createMinimal(
                HttpVersionPolicy.FORCE_HTTP_2, H2Config.DEFAULT, null, ioReactorConfig);

        client.start();

        final HttpHost target = new HttpHost("localhost",8441,"http");
        final Future<AsyncClientEndpoint> leaseFuture = client.lease(target, null);
        final AsyncClientEndpoint endpoint = leaseFuture.get(30, TimeUnit.SECONDS);
        try {
            //final String[] requestUris = new String[]{"/", "/ip", "/user-agent", "/headers"};
            final String[] requestUris = new String[]{"/http2.0-1.0-SNAPSHOT/test/test"};
            final CountDownLatch latch = new CountDownLatch(requestUris.length);
            for (final String requestUri : requestUris) {
                final SimpleHttpRequest request = SimpleHttpRequest.get(target, requestUri);
                endpoint.execute(
                        SimpleRequestProducer.create(request),
                        SimpleResponseConsumer.create(),
                        new FutureCallback<SimpleHttpResponse>() {

                            @Override
                            public void completed(final SimpleHttpResponse response) {
                                latch.countDown();
                                System.out.println(requestUri + "->" + response.getCode());
                                System.out.println(response.getBody());
                            }

                            @Override
                            public void failed(final Exception ex) {
                                latch.countDown();
                                System.out.println(requestUri + "->" + ex);
                            }

                            @Override
                            public void cancelled() {
                                latch.countDown();
                                System.out.println(requestUri + " cancelled");
                            }

                        });
            }
            latch.await();
        } finally {
            endpoint.releaseAndReuse();
        }

        System.out.println("Shutting down");
        client.shutdown(ShutdownType.GRACEFUL);
    }

    public static void main(String[] args) throws IOException {
        try {
            requestForHttp2();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }
}
