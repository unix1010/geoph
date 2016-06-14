package org.devgateway.geoph.core.export;

/**
 * Created by Sebastian Dimunzio on 6/10/2016.
 */
public interface ColumnDefinition<T> {

    RawCell<T> getCell(T value);

    Extractor<T> getExtractor();

    String getTitle();

    String getGetter();

    Formatter<T> getFormatter();

    Stylist getStylist();
}
