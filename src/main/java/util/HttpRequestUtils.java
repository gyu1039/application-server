package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

public class HttpRequestUtils {

	private static final Logger log = LoggerFactory.getLogger(HttpRequestUtils.class);

	public static String getUrl(String firstLine) {

		String[] splited = firstLine.split(" ");
		String url = splited[1];
		return url;
	}
	
	public static String getHTTPMethod(String firstLine) {

		String[] splited = firstLine.split(" ");
		String method = splited[0];
		return method;
	}
	
	public static String getRequestPath(String firstLine) {
		
		String url = getUrl(firstLine);
		
		String requestPath = url;
		
		if(url.contains("?")) {
			int index = splitRequest(url, "?");
			requestPath = url.substring(0, index);
			log.debug("request.path : {}", requestPath);	
		}
		
		return requestPath;
	}
	
	public static String getParam(String firstLine) {
	
		String url = getUrl(firstLine);
		
		int index = splitRequest(url, "?");
		String params = url.substring(index + 1);
		
		log.debug("request.params : {}", params);
		return params;
	}
	
	public static Map<String, String> getHeaders(BufferedReader br, String line) throws IOException {
		Map<String, String> headers = new HashMap<String, String>();

		while(!"".equals(line)) {
			log.debug("header : {}", line);

			line = br.readLine();
			String[] headerTokens = line.split(": ");
			if(headerTokens.length == 2) {
				headers.put(headerTokens[0], headerTokens[1]);        			
			}
		}

    	return headers;
	}
	
	

	public static int splitRequest(String url, String delimiter) {
		return url.indexOf(delimiter);
	}
	
	
	
	
    /**
     * @param queryString은
     *            URL에서 ? 이후에 전달되는 field1=value1&field2=value2 형식임
     * @return
     */
    public static Map<String, String> parseQueryString(String queryString) {
        return parseValues(queryString, "&");
    }

    /**
     * @param 쿠키
     *            값은 name1=value1; name2=value2 형식임
     * @return
     */
    public static Map<String, String> parseCookies(String cookies) {
        return parseValues(cookies, ";");
    }

    private static Map<String, String> parseValues(String values, String separator) {
        if (Strings.isNullOrEmpty(values)) {
            return Maps.newHashMap();
        }

        String[] tokens = values.split(separator);
        return Arrays.stream(tokens).map(t -> getKeyValue(t, "=")).filter(p -> p != null)
                .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
    }

    static Pair getKeyValue(String keyValue, String regex) {
        if (Strings.isNullOrEmpty(keyValue)) {
            return null;
        }

        String[] tokens = keyValue.split(regex);
        if (tokens.length != 2) {
            return null;
        }

        return new Pair(tokens[0], tokens[1]);
    }

    public static Pair parseHeader(String header) {
        return getKeyValue(header, ": ");
    }

    public static class Pair {
        String key;
        String value;

        Pair(String key, String value) {
            this.key = key.trim();
            this.value = value.trim();
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((key == null) ? 0 : key.hashCode());
            result = prime * result + ((value == null) ? 0 : value.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Pair other = (Pair) obj;
            if (key == null) {
                if (other.key != null)
                    return false;
            } else if (!key.equals(other.key))
                return false;
            if (value == null) {
                if (other.value != null)
                    return false;
            } else if (!value.equals(other.value))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "Pair [key=" + key + ", value=" + value + "]";
        }
    }
}
