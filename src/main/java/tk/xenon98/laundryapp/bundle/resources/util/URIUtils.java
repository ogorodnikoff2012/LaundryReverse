
package tk.xenon98.laundryapp.bundle.resources.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.Singular;
import tk.xenon98.laundryapp.bundle.resources.util.URIUtils.URIParts.URIBuilder;

public class URIUtils {

    private URIUtils() {
    }

    public static Map<String, String> decodeQuery(final String uriQuery) {
        return Arrays.stream(uriQuery.split("&")).map(keyValueRaw -> {
            final String[] parts = keyValueRaw.split("=");
            final String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
            final String value = URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
            return new String[] {key, value};
        }).collect(Collectors.toMap(kv -> kv[0], kv -> kv[1]));
    }

    public static String encodeQuery(final Map<String, String> query) {
        return String.join("&",
                query.entrySet().stream().map(kv -> URLEncoder.encode(kv.getKey(), StandardCharsets.UTF_8) + "="
                        + URLEncoder.encode(kv.getValue(), StandardCharsets.UTF_8)).toList());
    }

    public static String encodePath(final Path path) {
        final StringBuilder sb = new StringBuilder();
        final Path normalizedPath = path.normalize();
        for (int i = 0; i < normalizedPath.getNameCount(); ++i) {
            sb.append("/").append(URLEncoder.encode(normalizedPath.getName(i).toString(), StandardCharsets.UTF_8));
        }
        return sb.toString();
    }

    public static URIBuilder uriBuilder() {
        return URIParts.builder();
    }

    public static URIBuilder uriBuilder(final URI baseUri) {
        return URIParts.builder()
                .scheme(baseUri.getScheme())
                .userinfo(baseUri.getUserInfo())
                .host(baseUri.getHost())
                .port(baseUri.getPort())
                .path(baseUri.getPath())
                .query(decodeQuery(baseUri.getQuery()))
                .fragment(baseUri.getFragment());
    }

    @Builder(buildMethodName = "_build", builderClassName = "URIBuilder")
    @Data
    static class URIParts {

        @NonNull
        private String scheme;
        @Builder.Default
        private String userinfo = null;
        @Builder.Default
        private String host = null;
        @Builder.Default
        private String port = null;
        @NonNull
        private Path path;
        @Singular("query")
        private Map<String, String> query;
        @Builder.Default
        private String fragment = null;

        public static class URIBuilder {

            public URI build() {
                return _build().toURI();
            }

            public URIBuilder port(int port) {
                if (port <= 0 || port >= 65536) {
                    throw new IllegalArgumentException("Port must be between 0 and 65536");
                }
                return port(String.valueOf(port));
            }

            private URIBuilder port(@NonNull final String port) {
                verifyHostIsSet("port");
                this.port$value = port;
                this.port$set = true;
                return this;
            }

            public URIBuilder path(@NonNull final String path) {
                return path(Paths.get(path));
            }

            public URIBuilder path(@NonNull final Path path) {
                this.path = path;
                return this;
            }

            private void verifyHostIsSet(final String fieldName) {
                if (!host$set) {
                    throw new IllegalStateException("Cannot define " + fieldName + " without host");
                }
            }

            public URIBuilder userinfo(final String userinfo) {
                verifyHostIsSet("userinfo");
                this.userinfo$set = true;
                this.userinfo$value = userinfo;
                return this;
            }
        }

        public URI toURI() {
            final StringBuilder sb = new StringBuilder();
            sb.append(URLEncoder.encode(scheme, StandardCharsets.UTF_8)).append(":");
            if (host != null && !host.isEmpty()) {
                sb.append("//");
                if (userinfo != null && !userinfo.isEmpty()) {
                    sb.append(URLEncoder.encode(userinfo, StandardCharsets.UTF_8)).append("@");
                }
                sb.append(URLEncoder.encode(host, StandardCharsets.UTF_8));
                if (port != null && !port.isEmpty()) {
                    sb.append(":").append(port);
                }
            }
            sb.append(encodePath(path));
            if (query != null && !query.isEmpty()) {
                sb.append("?").append(encodeQuery(query));
            }
            if (fragment != null && fragment.length() > 0) {
                sb.append("#").append(URLEncoder.encode(fragment, StandardCharsets.UTF_8));
            }

            try {
                return new URI(sb.toString());
            } catch (URISyntaxException e) {
                throw new IllegalStateException(e);
            }
        }

    }

}
