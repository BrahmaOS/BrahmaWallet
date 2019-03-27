package io.brahmaos.wallet.util;


import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * BrahmaOS URI parser.
 */
public class BrahmaOSURI {

    private static final String SCHEME = "brahmaos:";
    private static final String PARAMETER_COIN = "coin";
    private static final String PARAMETER_AMOUNT = "amount";
    private static final String PARAMETER_LABEL = "label";

    private final String address;
    private final HashMap<String, BrahmaOSURIParam> parameters;

    private BrahmaOSURI(BrahmaOSURI.Builder builder) {
        this.address = builder.address;

        parameters = new HashMap<>();

        if (builder.amount != null) {
            parameters.put(PARAMETER_AMOUNT, new BrahmaOSURIParam(String.valueOf(builder.amount), false));
        }

        if (builder.label != null) {
            parameters.put(PARAMETER_LABEL, new BrahmaOSURIParam(builder.label, false));
        }

        if (builder.coin != null) {
            parameters.put(PARAMETER_COIN, new BrahmaOSURIParam(builder.coin, false));
        }
    }

    public String getAddress() {
        return address;
    }

    public Double getAmount() {
        if (parameters.get(PARAMETER_AMOUNT) == null) {
            return null;
        }

        return Double.valueOf(parameters.get(PARAMETER_AMOUNT).getValue());
    }

    public String getLabel() {
        if (parameters.get(PARAMETER_LABEL) == null) {
            return null;
        }

        return parameters.get(PARAMETER_LABEL).getValue();
    }

    public String getCoin() {
        if (parameters.get(PARAMETER_COIN) == null) {
            return null;
        }

        return parameters.get(PARAMETER_COIN).getValue();
    }

    /**
     * Parses a string to a BrahmaOS URI.
     *
     * @param string The string to be parsed.
     *
     * @return a BrahmaOS URI if the URI is valid, or null for an invalid string.
     */
    public static BrahmaOSURI parse(String string) {

        try {
            string = URLDecoder.decode(string,  "UTF-8");
            if (string == null || string.isEmpty() || !string.toLowerCase().startsWith(SCHEME)) {
                return null;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }

        String uriWithoutScheme = string.replaceFirst(SCHEME, "");
        ArrayList<String> uriElements = new ArrayList<>(Arrays.asList(uriWithoutScheme.split("\\?")));
        if (uriElements.size() != 1 && uriElements.size() != 2) {
            return null;
        }

        if (uriElements.get(0).length() == 0) {
            return null;
        }

        List<String> queryParameters = Arrays.asList(uriElements.get(1).split("&"));
        if (queryParameters.isEmpty()) {
            return new BrahmaOSURI.Builder().address(uriElements.get(0)).build();
        }

        HashMap<String, String> queryParametersFiltered = new HashMap<>();

        for (String query : queryParameters) {
            String[] queryParameter = query.split("=");
            if (queryParameter.length == 2) {
                queryParametersFiltered.put(queryParameter[0], queryParameter[1]);
            }
        }

        BrahmaOSURI.Builder uriBuilder = new BrahmaOSURI.Builder().address(uriElements.get(0));

        if (queryParametersFiltered.containsKey(PARAMETER_AMOUNT)) {
            uriBuilder.amount(Double.valueOf(queryParametersFiltered.get(PARAMETER_AMOUNT)));
            queryParametersFiltered.remove(PARAMETER_AMOUNT);
        }

        if (queryParametersFiltered.containsKey(PARAMETER_LABEL)) {
            uriBuilder.label(queryParametersFiltered.get(PARAMETER_LABEL));
            queryParametersFiltered.remove(PARAMETER_LABEL);
        }

        if (queryParametersFiltered.containsKey(PARAMETER_COIN)) {
            uriBuilder.coin(queryParametersFiltered.get(PARAMETER_COIN));
            queryParametersFiltered.remove(PARAMETER_COIN);
        }

        return uriBuilder.build();
    }
    public String getURI() {
        String queryParameters = null;
        try {
            for (Map.Entry<String, BrahmaOSURIParam> entry : parameters.entrySet()) {
                if (queryParameters == null) {
                    if (entry.getValue().isRequired()) {
                        queryParameters = String.format("req-%s=%s", URLEncoder.encode(entry.getKey(), "UTF-8").replace("+", "%20"), URLEncoder.encode(entry.getValue().getValue(), "UTF-8").replace("+", "%20"));

                        continue;
                    }

                    queryParameters = String.format("%s=%s", URLEncoder.encode(entry.getKey(), "UTF-8").replace("+", "%20"), URLEncoder.encode(entry.getValue().getValue(), "UTF-8").replace("+", "%20"));

                    continue;
                }

                if (entry.getValue().isRequired()) {
                    queryParameters = String.format("%s&req-%s=%s", queryParameters, URLEncoder.encode(entry.getKey(), "UTF-8").replace("+", "%20"), URLEncoder.encode(entry.getValue().getValue(), "UTF-8").replace("+", "%20"));

                    continue;
                }

                queryParameters = String.format("%s&%s=%s", queryParameters, URLEncoder.encode(entry.getKey(), "UTF-8").replace("+", "%20"), URLEncoder.encode(entry.getValue().getValue(), "UTF-8").replace("+", "%20"));
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();

            return null;
        }
        return String.format("%s%s%s", SCHEME, getAddress(), queryParameters == null ? "" : String.format("?%s", queryParameters));
    }

    public static class Builder {

        private String address;
        private Double amount;
        private String label;
        private String coin;

        public Builder() {
        }

        /**
         * Adds the address to the builder.
         *
         * @param address The address.
         *
         * @return the builder with the address.
         */
        public BrahmaOSURI.Builder address(String address) {
            this.address = address;
            return this;
        }

        /**
         * Adds the amount to the builder.
         *
         * @param amount The amount.
         *
         * @return the builder with the amount.
         */

        public BrahmaOSURI.Builder amount(Double amount) {
            this.amount = amount;
            return this;
        }

        /**
         * Adds the label to the builder.
         *
         * @param label The label.
         *
         * @return the builder with the label.
         */

        public BrahmaOSURI.Builder label(String label) {
            this.label = label;
            return this;
        }

        /**
         * Adds the coin to the builder.
         *
         * @param coin The coin symbol.
         *
         * @return the builder with the coin symbol.
         */

        public BrahmaOSURI.Builder coin(String coin) {
            this.coin = coin;
            return this;
        }

        /**
         * Builds a BrahmaOS URI.
         *
         * @return a BrahmaOS URI.
         */
        public BrahmaOSURI build() {
            return new BrahmaOSURI(this);
        }
    }
}
