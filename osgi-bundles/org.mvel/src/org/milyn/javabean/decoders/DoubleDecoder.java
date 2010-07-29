package org.milyn.javabean.decoders;

import org.milyn.javabean.DataDecoder;
import org.milyn.javabean.DataDecodeException;
import org.milyn.javabean.DecodeType;

/**
 * Double decoder.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@DecodeType({Double.class, double.class})
public class DoubleDecoder implements DataDecoder {

    public Object decode(String data) throws DataDecodeException {
        try {
            return Double.parseDouble(data.trim());
        } catch(NumberFormatException e) {
            throw new DataDecodeException("Failed to decode Double value '" + data + "'.", e);
        }
    }
}
