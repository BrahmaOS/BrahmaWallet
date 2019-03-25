package io.brahmaos.wallet.util;


/**
 * BrahmaOS Uri Spec:
 *
 * brahmaos:0x6103ab3720cd17ae9832ef37103bf832?label=smallfish&coin=btc&amount=1.89
 */
public class BrahmaOSURIParam {

    private final String value;
    private final Boolean required;

    /**
     * Constructor.
     *
     * @param value The value.
     * @param required A boolean indicating if the parameter is required.
     */

    public BrahmaOSURIParam(String value, Boolean required) {
        super();
        this.value = value;
        this.required = required;
    }

    /**
     * Gets the parameter value.
     *
     * @return the parameter value.
     */

    public String getValue() {
        return value;
    }

    /**
     * Gets a boolean indicating if the parameter is required.
     *
     * @return a boolean indicating if the parameter is required
     */

    public Boolean isRequired() {
        return required;
    }
}
